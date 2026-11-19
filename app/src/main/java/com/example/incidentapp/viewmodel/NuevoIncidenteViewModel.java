package com.example.incidentapp.viewmodel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.local.AppDatabase;
import com.example.incidentapp.local.entity.SolicitudPendienteEntity;
import com.example.incidentapp.model.*;
import com.example.incidentapp.repository.CatalogoRepository;
import com.example.incidentapp.repository.IncidenteRepository;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.util.*;
import com.example.incidentapp.worker.SyncScheduler;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoIncidenteViewModel extends ViewModel {
    private final CatalogoRepository cat = new CatalogoRepository();
    private final IncidenteRepository repo = new IncidenteRepository();
    private final MutableLiveData<List<Sucursal>> sucursales = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Activo> activo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> registrado = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> pendiente = new MutableLiveData<>();

    public LiveData<List<Sucursal>> getSucursales(){ return sucursales; }
    public LiveData<Activo> getActivo(){ return activo; }
    public LiveData<Boolean> getLoading(){ return loading; }
    public LiveData<Event<String>> getMensaje(){ return mensaje; }
    public LiveData<Event<Integer>> getRegistrado(){ return registrado; }
    public LiveData<Event<Boolean>> getPendiente(){ return pendiente; }

    public void cargarSucursales(){
        cat.obtenerSucursales().enqueue(new Callback<SucursalesResponse>() {
            @Override public void onResponse(Call<SucursalesResponse> c, Response<SucursalesResponse> r) {
                if(r.isSuccessful() && r.body()!=null && r.body().getSucursales()!=null) sucursales.setValue(r.body().getSucursales());
                else mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(r,"No se pudieron cargar las sucursales")));
            }
            @Override public void onFailure(Call<SucursalesResponse> c, Throwable t) {
                Sucursal s = new Sucursal();
                s.setId(SessionManager.getInstance().getIdSucursal());
                s.setNombre(SessionManager.getInstance().getSucursal());
                s.setIdEmpresa(SessionManager.getInstance().getIdEmpresa());
                sucursales.setValue(Collections.singletonList(s));
                mensaje.setValue(new Event<>("Sin conexión: se usará tu sucursal guardada"));
            }
        });
    }

    public void buscarActivo(String qr){
        if(qr==null || qr.trim().isEmpty()) { mensaje.setValue(new Event<>("Ingresa el código QR del activo")); return; }
        // Evita conservar el activo anterior si falla una nueva búsqueda.
        activo.setValue(null);
        cat.obtenerActivo(qr.trim()).enqueue(new Callback<ActivoResponse>() {
            @Override public void onResponse(Call<ActivoResponse> c, Response<ActivoResponse> r) {
                if(r.isSuccessful() && r.body()!=null) activo.setValue(r.body().getActivo());
                else { activo.setValue(null); mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(r,"No se encontró el activo"))); }
            }
            @Override public void onFailure(Call<ActivoResponse> c, Throwable t) {
                activo.setValue(null);
                mensaje.setValue(new Event<>("Necesitas conexión para validar el QR del activo"));
            }
        });
    }

    public void registrar(String titulo, String descripcion, String categoria, String prioridad,
                          int idSucursal, Integer idActivo, List<MultipartBody.Part> fotos,
                          List<String> paths, Context context) {
        loading.setValue(true);
        // Se crea ANTES de la primera petición y se reutiliza si la respuesta se pierde.
        final String clientRequestId = UUID.randomUUID().toString();
        repo.registrarIncidente(clientRequestId, titulo, descripcion, categoria, prioridad, idSucursal, idActivo, fotos)
                .enqueue(new Callback<RegistroIncidenteResponse>() {
                    @Override public void onResponse(Call<RegistroIncidenteResponse> call, Response<RegistroIncidenteResponse> r) {
                        loading.setValue(false);
                        if(r.isSuccessful() && r.body()!=null) {
                            OfflineFileUtils.borrar(paths);
                            registrado.setValue(new Event<>(r.body().getIdIncidente()));
                        } else {
                            // Un 4xx/5xx es respuesta real del servidor: NO se guarda como si fuera falta de conexión.
                            OfflineFileUtils.borrar(paths);
                            mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(r,"No se pudo registrar el incidente")));
                        }
                    }

                    @Override public void onFailure(Call<RegistroIncidenteResponse> call, Throwable th) {
                        loading.setValue(false);
                        guardarPendiente(clientRequestId, titulo, descripcion, categoria, prioridad, idSucursal, idActivo, paths, context);
                    }
                });
    }

    private void guardarPendiente(String clientRequestId, String titulo, String descripcion, String categoria,
                                  String prioridad, int idSucursal, Integer idActivo, List<String> paths, Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            SolicitudPendienteEntity e = new SolicitudPendienteEntity();
            e.idUsuario = SessionManager.getInstance().getIdUsuario();
            e.clientRequestId = clientRequestId;
            e.titulo = titulo;
            e.descripcion = descripcion;
            e.categoria = categoria;
            e.prioridad = prioridad;
            e.idSucursal = idSucursal;
            e.idActivo = idActivo;
            e.foto1Path = paths.size()>0 ? paths.get(0) : null;
            e.foto2Path = paths.size()>1 ? paths.get(1) : null;
            e.fechaCreacion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            AppDatabase.get().solicitudPendienteDao().insertar(e);
            SyncScheduler.sincronizarAhora(context);
            new Handler(Looper.getMainLooper()).post(() -> {
                mensaje.setValue(new Event<>("Sin conexión · incidente guardado para sincronizar"));
                pendiente.setValue(new Event<>(true));
            });
        });
    }
}
