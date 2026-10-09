package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.DetalleIncidenteResponse;
import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.Tecnico;
import com.example.incidentapp.model.TecnicosResponse;
import com.example.incidentapp.repository.CatalogoRepository;
import com.example.incidentapp.repository.IncidenteRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleIncidenteViewModel extends ViewModel {

    private final IncidenteRepository incidenteRepository = new IncidenteRepository();
    private final CatalogoRepository catalogoRepository = new CatalogoRepository();

    private final MutableLiveData<DetalleIncidenteResponse> detalle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Event<List<Tecnico>>> tecnicos = new MutableLiveData<>();

    public LiveData<DetalleIncidenteResponse> getDetalle() { return detalle; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getMensaje() { return mensaje; }
    public LiveData<Event<List<Tecnico>>> getTecnicos() { return tecnicos; }

    public void cargarDetalle(int idIncidente) {
        loading.setValue(true);

        incidenteRepository.obtenerIncidente(idIncidente).enqueue(new Callback<DetalleIncidenteResponse>() {
            @Override
            public void onResponse(Call<DetalleIncidenteResponse> call, Response<DetalleIncidenteResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    detalle.setValue(response.body());
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(
                            response,
                            "No se pudo obtener el detalle"
                    )));
                }
            }

            @Override
            public void onFailure(Call<DetalleIncidenteResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void cambiarEstado(int idIncidente, String estado, String comentario) {
        loading.setValue(true);
        incidenteRepository.cambiarEstado(idIncidente, estado, comentario)
                .enqueue(new Callback<MensajeResponse>() {
                    @Override
                    public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                        loading.setValue(false);
                        if (response.isSuccessful()) {
                            String texto = response.body() == null ? "Estado actualizado" : response.body().getMensaje();
                            mensaje.setValue(new Event<>(texto));
                            cargarDetalle(idIncidente);
                        } else {
                            mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(
                                    response,
                                    "No se pudo cambiar el estado"
                            )));
                        }
                    }

                    @Override
                    public void onFailure(Call<MensajeResponse> call, Throwable t) {
                        loading.setValue(false);
                        mensaje.setValue(new Event<>("No se pudo conectar con la API"));
                    }
                });
    }

    public void cargarTecnicos() {
        catalogoRepository.obtenerTecnicos().enqueue(new Callback<TecnicosResponse>() {
            @Override
            public void onResponse(Call<TecnicosResponse> call, Response<TecnicosResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Tecnico> lista = response.body().getTecnicos();
                    tecnicos.setValue(new Event<>(lista == null ? Collections.emptyList() : lista));
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(
                            response,
                            "No se pudieron obtener los técnicos"
                    )));
                }
            }

            @Override
            public void onFailure(Call<TecnicosResponse> call, Throwable t) {
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void asignarTecnico(int idIncidente, int idTecnico) {
        loading.setValue(true);
        incidenteRepository.asignarTecnico(idIncidente, idTecnico)
                .enqueue(new Callback<MensajeResponse>() {
                    @Override
                    public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                        loading.setValue(false);
                        if (response.isSuccessful()) {
                            String texto = response.body() == null ? "Técnico asignado" : response.body().getMensaje();
                            mensaje.setValue(new Event<>(texto));
                            cargarDetalle(idIncidente);
                        } else {
                            mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(
                                    response,
                                    "No se pudo asignar el técnico"
                            )));
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
