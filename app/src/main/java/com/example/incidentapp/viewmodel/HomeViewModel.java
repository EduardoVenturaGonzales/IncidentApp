package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.Incidente;
import com.example.incidentapp.model.IncidentesResponse;
import com.example.incidentapp.model.NotificacionesResponse;
import com.example.incidentapp.repository.IncidenteRepository;
import com.example.incidentapp.repository.NotificacionRepository;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private final IncidenteRepository repository = new IncidenteRepository();
    private final NotificacionRepository notificacionRepository = new NotificacionRepository();
    private final MutableLiveData<List<Incidente>> incidentes = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> noLeidas = new MutableLiveData<>(0);

    public LiveData<List<Incidente>> getIncidentes() { return incidentes; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Integer> getNoLeidas() { return noLeidas; }

    public void cargarResumen() {
        loading.setValue(true);
        repository.obtenerIncidentes(null, null).enqueue(new Callback<IncidentesResponse>() {
            @Override
            public void onResponse(Call<IncidentesResponse> call, Response<IncidentesResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getIncidentes() != null) {
                    incidentes.setValue(response.body().getIncidentes());
                }
            }
            @Override public void onFailure(Call<IncidentesResponse> call, Throwable t) { loading.setValue(false); }
        });
    }

    public void cargarNotificaciones() {
        notificacionRepository.obtenerNotificaciones(true, 20).enqueue(new Callback<NotificacionesResponse>() {
            @Override
            public void onResponse(Call<NotificacionesResponse> call, Response<NotificacionesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    noLeidas.setValue(response.body().getNoLeidas());
                }
            }
            @Override public void onFailure(Call<NotificacionesResponse> call, Throwable t) { }
        });
    }
}
