package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.Empresa;
import com.example.incidentapp.model.EmpresasResponse;
import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.RegistroEmpresaRequest;
import com.example.incidentapp.repository.GestionRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionEmpresasViewModel extends ViewModel {

    private final GestionRepository repository = new GestionRepository();
    private final MutableLiveData<List<Empresa>> empresas = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> mensaje = new MutableLiveData<>();

    public LiveData<List<Empresa>> getEmpresas() { return empresas; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getMensaje() { return mensaje; }

    public void cargar() {
        loading.setValue(true);
        repository.obtenerEmpresas().enqueue(new Callback<EmpresasResponse>() {
            @Override
            public void onResponse(Call<EmpresasResponse> call, Response<EmpresasResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getEmpresas() != null) {
                    empresas.setValue(response.body().getEmpresas());
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudieron cargar las empresas")));
                }
            }

            @Override
            public void onFailure(Call<EmpresasResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void registrar(String nombre, String ruc) {
        loading.setValue(true);
        repository.registrarEmpresa(new RegistroEmpresaRequest(nombre, ruc)).enqueue(new Callback<MensajeResponse>() {
            @Override
            public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    mensaje.setValue(new Event<>(response.body() == null ? "Empresa registrada" : response.body().getMensaje()));
                    cargar();
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudo registrar la empresa")));
                }
            }

            @Override
            public void onFailure(Call<MensajeResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void eliminar(int idEmpresa) {
        loading.setValue(true);
        repository.eliminarEmpresa(idEmpresa).enqueue(new Callback<MensajeResponse>() {
            @Override
            public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    mensaje.setValue(new Event<>(response.body() == null ? "Empresa eliminada" : response.body().getMensaje()));
                    cargar();
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudo eliminar la empresa")));
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
