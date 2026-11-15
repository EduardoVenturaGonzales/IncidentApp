package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.Empresa;
import com.example.incidentapp.model.EmpresasResponse;
import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.RegistroSucursalRequest;
import com.example.incidentapp.model.Sucursal;
import com.example.incidentapp.model.SucursalesResponse;
import com.example.incidentapp.repository.GestionRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarSucursalViewModel extends ViewModel {

    private final GestionRepository repository = new GestionRepository();
    private final MutableLiveData<List<Empresa>> empresas = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<Sucursal>> sucursales = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> registrado = new MutableLiveData<>();

    public LiveData<List<Empresa>> getEmpresas() { return empresas; }
    public LiveData<List<Sucursal>> getSucursales() { return sucursales; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getMensaje() { return mensaje; }
    public LiveData<Event<Boolean>> getRegistrado() { return registrado; }

    public void cargarTodo() {
        cargarEmpresas();
        cargarSucursales();
    }

    public void cargarEmpresas() {
        repository.obtenerEmpresas().enqueue(new Callback<EmpresasResponse>() {
            @Override
            public void onResponse(Call<EmpresasResponse> call, Response<EmpresasResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getEmpresas() != null) {
                    empresas.setValue(response.body().getEmpresas());
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudieron cargar las empresas")));
                }
            }
            @Override public void onFailure(Call<EmpresasResponse> call, Throwable t) {
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void cargarSucursales() {
        repository.obtenerSucursales(true).enqueue(new Callback<SucursalesResponse>() {
            @Override
            public void onResponse(Call<SucursalesResponse> call, Response<SucursalesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSucursales() != null) {
                    sucursales.setValue(response.body().getSucursales());
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudieron cargar las sucursales")));
                }
            }
            @Override public void onFailure(Call<SucursalesResponse> call, Throwable t) {
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void registrarSucursal(String nombre, String direccion, double latitud, double longitud, int idEmpresa) {
        loading.setValue(true);
        RegistroSucursalRequest request = new RegistroSucursalRequest(nombre, direccion, latitud, longitud, idEmpresa);
        repository.registrarSucursal(request).enqueue(new Callback<MensajeResponse>() {
            @Override
            public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    mensaje.setValue(new Event<>(response.body() == null ? "Sucursal registrada correctamente" : response.body().getMensaje()));
                    registrado.setValue(new Event<>(true));
                    cargarSucursales();
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudo registrar la sucursal")));
                }
            }
            @Override public void onFailure(Call<MensajeResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void eliminarSucursal(int idSucursal) {
        loading.setValue(true);
        repository.eliminarSucursal(idSucursal).enqueue(new Callback<MensajeResponse>() {
            @Override
            public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    mensaje.setValue(new Event<>(response.body() == null ? "Sucursal eliminada" : response.body().getMensaje()));
                    cargarSucursales();
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudo eliminar la sucursal")));
                }
            }
            @Override public void onFailure(Call<MensajeResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }
}
