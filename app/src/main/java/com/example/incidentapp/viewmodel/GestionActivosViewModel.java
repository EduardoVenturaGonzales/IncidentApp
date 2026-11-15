package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.Activo;
import com.example.incidentapp.model.ActivosResponse;
import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.repository.ActivoRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionActivosViewModel extends ViewModel {
    private final ActivoRepository repository = new ActivoRepository();
    private final MutableLiveData<List<Activo>> activos = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> mensaje = new MutableLiveData<>();

    public LiveData<List<Activo>> getActivos() { return activos; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getMensaje() { return mensaje; }

    public void cargar() {
        loading.setValue(true);
        repository.obtenerActivos().enqueue(new Callback<ActivosResponse>() {
            @Override
            public void onResponse(Call<ActivosResponse> call, Response<ActivosResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getActivos() != null) {
                    activos.setValue(response.body().getActivos());
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudieron cargar los activos")));
                }
            }
            @Override
            public void onFailure(Call<ActivosResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void eliminar(int idActivo) {
        loading.setValue(true);
        repository.eliminarActivo(idActivo).enqueue(new Callback<MensajeResponse>() {
            @Override
            public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    mensaje.setValue(new Event<>(response.body() == null ? "Activo eliminado" : response.body().getMensaje()));
                    cargar();
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudo eliminar el activo")));
                }
            }
            @Override
            public void onFailure(Call<MensajeResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }
}
