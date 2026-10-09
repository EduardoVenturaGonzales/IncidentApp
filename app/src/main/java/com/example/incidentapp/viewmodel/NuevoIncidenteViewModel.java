package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.Activo;
import com.example.incidentapp.model.ActivoResponse;
import com.example.incidentapp.model.RegistroIncidenteResponse;
import com.example.incidentapp.model.Sucursal;
import com.example.incidentapp.model.SucursalesResponse;
import com.example.incidentapp.repository.CatalogoRepository;
import com.example.incidentapp.repository.IncidenteRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import java.util.Collections;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuevoIncidenteViewModel extends ViewModel {

    private final CatalogoRepository catalogoRepository = new CatalogoRepository();
    private final IncidenteRepository incidenteRepository = new IncidenteRepository();

    private final MutableLiveData<List<Sucursal>> sucursales = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Activo> activo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> registrado = new MutableLiveData<>();

    public LiveData<List<Sucursal>> getSucursales() { return sucursales; }
    public LiveData<Activo> getActivo() { return activo; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getMensaje() { return mensaje; }
    public LiveData<Event<Integer>> getRegistrado() { return registrado; }

    public void cargarSucursales() {
        catalogoRepository.obtenerSucursales().enqueue(new Callback<SucursalesResponse>() {
            @Override
            public void onResponse(Call<SucursalesResponse> call, Response<SucursalesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSucursales() != null) {
                    sucursales.setValue(response.body().getSucursales());
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(
                            response,
                            "No se pudieron cargar las sucursales"
                    )));
                }
            }

            @Override
            public void onFailure(Call<SucursalesResponse> call, Throwable t) {
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void buscarActivo(String codigoQr) {
        if (codigoQr == null || codigoQr.trim().isEmpty()) {
            mensaje.setValue(new Event<>("Ingresa el código QR del activo"));
            return;
        }

        catalogoRepository.obtenerActivo(codigoQr.trim()).enqueue(new Callback<ActivoResponse>() {
            @Override
            public void onResponse(Call<ActivoResponse> call, Response<ActivoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activo.setValue(response.body().getActivo());
                } else {
                    activo.setValue(null);
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(
                            response,
                            "No se encontró el activo"
                    )));
                }
            }

            @Override
            public void onFailure(Call<ActivoResponse> call, Throwable t) {
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void registrar(
            String titulo,
            String descripcion,
            String categoria,
            String prioridad,
            int idSucursal,
            Integer idActivo,
            List<MultipartBody.Part> fotos
    ) {
        loading.setValue(true);

        incidenteRepository.registrarIncidente(
                titulo,
                descripcion,
                categoria,
                prioridad,
                idSucursal,
                idActivo,
                fotos
        ).enqueue(new Callback<RegistroIncidenteResponse>() {
            @Override
            public void onResponse(Call<RegistroIncidenteResponse> call, Response<RegistroIncidenteResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    mensaje.setValue(new Event<>(response.body().getMensaje()));
                    registrado.setValue(new Event<>(response.body().getIdIncidente()));
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(
                            response,
                            "No se pudo registrar el incidente"
                    )));
                }
            }

            @Override
            public void onFailure(Call<RegistroIncidenteResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }
}
