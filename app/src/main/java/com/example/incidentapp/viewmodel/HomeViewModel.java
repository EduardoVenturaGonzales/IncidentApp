package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.Incidente;
import com.example.incidentapp.model.IncidentesResponse;
import com.example.incidentapp.repository.IncidenteRepository;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private final IncidenteRepository repository = new IncidenteRepository();
    private final MutableLiveData<List<Incidente>> incidentes = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<Incidente>> getIncidentes() { return incidentes; }
    public LiveData<Boolean> getLoading() { return loading; }

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

            @Override
            public void onFailure(Call<IncidentesResponse> call, Throwable t) {
                loading.setValue(false);
            }
        });
    }
}
