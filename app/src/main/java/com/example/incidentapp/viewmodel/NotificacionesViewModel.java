package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.Notificacion;
import com.example.incidentapp.model.NotificacionesResponse;
import com.example.incidentapp.repository.NotificacionRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificacionesViewModel extends ViewModel {
    private final NotificacionRepository repository = new NotificacionRepository();
    private final MutableLiveData<List<Notificacion>> notificaciones = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> noLeidas = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> mensaje = new MutableLiveData<>();

    public LiveData<List<Notificacion>> getNotificaciones() { return notificaciones; }
    public LiveData<Integer> getNoLeidas() { return noLeidas; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getMensaje() { return mensaje; }

    public void cargar() {
        loading.setValue(true);
        repository.obtenerNotificaciones(false, 100).enqueue(new Callback<NotificacionesResponse>() {
            @Override
            public void onResponse(Call<NotificacionesResponse> call, Response<NotificacionesResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    notificaciones.setValue(response.body().getNotificaciones() == null ? Collections.emptyList() : response.body().getNotificaciones());
                    noLeidas.setValue(response.body().getNoLeidas());
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudieron cargar las notificaciones")));
                }
            }
            @Override
            public void onFailure(Call<NotificacionesResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void marcarLeida(int id) {
        repository.marcarLeida(id).enqueue(new Callback<MensajeResponse>() {
            @Override public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) { cargar(); }
            @Override public void onFailure(Call<MensajeResponse> call, Throwable t) { }
        });
    }

    public void marcarTodas() {
        repository.marcarTodasLeidas().enqueue(new Callback<MensajeResponse>() {
            @Override
            public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                if (response.isSuccessful()) {
                    mensaje.setValue(new Event<>(response.body() == null ? "Notificaciones actualizadas" : response.body().getMensaje()));
                    cargar();
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudieron actualizar las notificaciones")));
                }
            }
            @Override
            public void onFailure(Call<MensajeResponse> call, Throwable t) {
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }
}
