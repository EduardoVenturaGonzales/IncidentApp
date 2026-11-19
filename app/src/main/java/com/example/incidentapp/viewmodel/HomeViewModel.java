package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.InicioResumenResponse;
import com.example.incidentapp.model.Incidente;
import com.example.incidentapp.repository.InicioRepository;
import com.example.incidentapp.repository.LocalCacheRepository;
import com.example.incidentapp.session.SessionManager;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    private final InicioRepository inicioRepository = new InicioRepository();
    private final LocalCacheRepository local = new LocalCacheRepository();
    private final MutableLiveData<List<Incidente>> incidentes = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> noLeidas = new MutableLiveData<>(0);
    private final MutableLiveData<InicioResumenResponse.IncidentesResumen> resumen = new MutableLiveData<>();

    public LiveData<List<Incidente>> getIncidentes(){ return incidentes; }
    public LiveData<Boolean> getLoading(){ return loading; }
    public LiveData<Integer> getNoLeidas(){ return noLeidas; }
    public LiveData<InicioResumenResponse.IncidentesResumen> getResumen(){ return resumen; }

    public void cargarResumen() {
        int uid = SessionManager.getInstance().getIdUsuario();
        local.cargarIncidentes(uid, null, null, l -> {
            if (l != null && !l.isEmpty()) incidentes.setValue(l);
        });
        cargarResumenServidor(true);
    }

    public void cargarResumenServidor(boolean mostrarCarga) {
        if (mostrarCarga) loading.setValue(true);
        inicioRepository.obtenerResumen().enqueue(new Callback<InicioResumenResponse>() {
            @Override public void onResponse(Call<InicioResumenResponse> call, Response<InicioResumenResponse> r) {
                loading.setValue(false);
                if (r.isSuccessful() && r.body() != null) {
                    noLeidas.setValue(r.body().getNotificacionesNoLeidas());
                    if (r.body().getIncidentes() != null) resumen.setValue(r.body().getIncidentes());
                }
            }
            @Override public void onFailure(Call<InicioResumenResponse> call, Throwable t) {
                loading.setValue(false);
            }
        });
    }

    // Se conserva para compatibilidad con llamadas existentes.
    public void cargarNotificaciones() { cargarResumenServidor(false); }
}
