package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.SolicitudAnulacion;
import com.example.incidentapp.model.SolicitudesAnulacionResponse;
import com.example.incidentapp.repository.IncidenteRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolicitudesAnulacionViewModel extends ViewModel {
    private final IncidenteRepository repository = new IncidenteRepository();
    private final MutableLiveData<List<SolicitudAnulacion>> solicitudes = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> mensaje = new MutableLiveData<>();

    public LiveData<List<SolicitudAnulacion>> getSolicitudes() { return solicitudes; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getMensaje() { return mensaje; }

    public void cargar(String estado) {
        loading.setValue(true);
        repository.obtenerSolicitudesAnulacion(estado).enqueue(new Callback<SolicitudesAnulacionResponse>() {
            @Override
            public void onResponse(Call<SolicitudesAnulacionResponse> call, Response<SolicitudesAnulacionResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getSolicitudes() != null) {
                    solicitudes.setValue(response.body().getSolicitudes());
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudieron cargar las solicitudes")));
                }
            }
            @Override
            public void onFailure(Call<SolicitudesAnulacionResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void responder(int idIncidente, String accion, String comentario) {
        loading.setValue(true);
        repository.responderAnulacion(idIncidente, accion, comentario).enqueue(new Callback<MensajeResponse>() {
            @Override
            public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    mensaje.setValue(new Event<>(response.body() == null ? "Solicitud actualizada" : response.body().getMensaje()));
                    cargar("Pendiente");
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudo responder la solicitud")));
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
