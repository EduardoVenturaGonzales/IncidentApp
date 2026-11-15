package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.Activo;
import com.example.incidentapp.model.RegistroActivoResponse;
import com.example.incidentapp.repository.ActivoRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarActivoViewModel extends ViewModel {
    private final ActivoRepository repository = new ActivoRepository();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Event<Activo>> registrado = new MutableLiveData<>();

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getMensaje() { return mensaje; }
    public LiveData<Event<Activo>> getRegistrado() { return registrado; }

    public void registrar(String nombre, String tipo, String estado, List<MultipartBody.Part> fotos) {
        loading.setValue(true);
        repository.registrarActivo(nombre, tipo, estado, fotos).enqueue(new Callback<RegistroActivoResponse>() {
            @Override
            public void onResponse(Call<RegistroActivoResponse> call, Response<RegistroActivoResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getActivo() != null) {
                    mensaje.setValue(new Event<>(response.body().getMensaje() == null ? "Activo registrado" : response.body().getMensaje()));
                    registrado.setValue(new Event<>(response.body().getActivo()));
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudo registrar el activo")));
                }
            }
            @Override
            public void onFailure(Call<RegistroActivoResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }
}
