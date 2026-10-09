package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.Incidente;
import com.example.incidentapp.model.IncidentesResponse;
import com.example.incidentapp.repository.IncidenteRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidentesViewModel extends ViewModel {

    private final IncidenteRepository repository = new IncidenteRepository();
    private final MutableLiveData<List<Incidente>> incidentes = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> error = new MutableLiveData<>();

    public LiveData<List<Incidente>> getIncidentes() { return incidentes; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getError() { return error; }

    public void cargar(String estado, String prioridad) {
        loading.setValue(true);

        repository.obtenerIncidentes(limpiarFiltro(estado), limpiarFiltro(prioridad))
                .enqueue(new Callback<IncidentesResponse>() {
                    @Override
                    public void onResponse(Call<IncidentesResponse> call, Response<IncidentesResponse> response) {
                        loading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Incidente> lista = response.body().getIncidentes();
                            incidentes.setValue(lista == null ? Collections.emptyList() : lista);
                        } else {
                            error.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(
                                    response,
                                    "No se pudieron obtener los incidentes"
                            )));
                        }
                    }

                    @Override
                    public void onFailure(Call<IncidentesResponse> call, Throwable t) {
                        loading.setValue(false);
                        error.setValue(new Event<>("No se pudo conectar con la API"));
                    }
                });
    }

    private String limpiarFiltro(String valor) {
        if (valor == null || valor.trim().isEmpty() || "Todos".equalsIgnoreCase(valor)) {
            return null;
        }
        return valor;
    }
}
