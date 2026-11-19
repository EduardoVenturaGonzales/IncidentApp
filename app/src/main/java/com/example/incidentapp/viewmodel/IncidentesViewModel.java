package com.example.incidentapp.viewmodel;

import androidx.lifecycle.*;

import com.example.incidentapp.model.*;
import com.example.incidentapp.repository.*;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.util.*;

import java.util.*;

import retrofit2.*;

public class IncidentesViewModel extends ViewModel {
    private final IncidenteRepository repo = new IncidenteRepository();
    private final LocalCacheRepository local = new LocalCacheRepository();
    private final MutableLiveData<List<Incidente>> incidentes = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> error = new MutableLiveData<>();
    private final Set<Integer> marcando = new HashSet<>();

    public LiveData<List<Incidente>> getIncidentes(){ return incidentes; }
    public LiveData<Boolean> getLoading(){ return loading; }
    public LiveData<Event<String>> getError(){ return error; }

    public void cargar(String estadoTexto, String prioridadTexto) {
        String estado = limpiar(estadoTexto), prioridad = limpiar(prioridadTexto);
        int uid = SessionManager.getInstance().getIdUsuario();
        local.cargarIncidentes(uid, estado, prioridad, l -> { if(l!=null && !l.isEmpty()) incidentes.setValue(l); });
        loading.setValue(true);
        repo.obtenerIncidentes(estado, prioridad).enqueue(new Callback<IncidentesResponse>() {
            @Override public void onResponse(Call<IncidentesResponse> c, Response<IncidentesResponse> r) {
                loading.setValue(false);
                if(r.isSuccessful() && r.body()!=null) {
                    List<Incidente> l = r.body().getIncidentes();
                    if(l==null) l=Collections.emptyList();
                    incidentes.setValue(l);
                    // Solo una consulta SIN filtros reemplaza la caché completa.
                    if (estado == null && prioridad == null) local.guardarIncidentes(uid, l);
                    revisarSla(l);
                } else error.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(r,"No se pudieron obtener los incidentes")));
            }
            @Override public void onFailure(Call<IncidentesResponse> c, Throwable t) {
                loading.setValue(false);
                local.cargarIncidentes(uid, estado, prioridad, l -> {
                    incidentes.setValue(l);
                    error.setValue(new Event<>(l==null||l.isEmpty()?"Sin conexión y sin datos guardados":"Sin conexión · mostrando datos guardados"));
                });
            }
        });
    }

    private void revisarSla(List<Incidente> l){
        for(Incidente i:l) if(SlaUtils.estaVencidoActivo(i) && marcando.add(i.getId()))
            repo.marcarVencimientoSla(i.getId()).enqueue(new Callback<MensajeResponse>() {
                @Override public void onResponse(Call<MensajeResponse> c, Response<MensajeResponse> r) {
                    marcando.remove(i.getId());
                    if(r.isSuccessful()) { i.setEstado("No Resuelto"); incidentes.setValue(new ArrayList<>(l)); }
                }
                @Override public void onFailure(Call<MensajeResponse> c, Throwable t){ marcando.remove(i.getId()); }
            });
    }
    private String limpiar(String v){ return v==null||v.trim().isEmpty()||"Todos".equalsIgnoreCase(v)?null:v; }
}
