package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.RegistroUsuarioRequest;
import com.example.incidentapp.model.Sucursal;
import com.example.incidentapp.model.SucursalesResponse;
import com.example.incidentapp.model.Usuario;
import com.example.incidentapp.model.UsuariosResponse;
import com.example.incidentapp.repository.CatalogoRepository;
import com.example.incidentapp.repository.GestionRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarUsuarioViewModel extends ViewModel {

    private final GestionRepository gestionRepository = new GestionRepository();
    private final CatalogoRepository catalogoRepository = new CatalogoRepository();

    private final MutableLiveData<List<Sucursal>> sucursales = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<Usuario>> usuarios = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> registrado = new MutableLiveData<>();

    public LiveData<List<Sucursal>> getSucursales() { return sucursales; }
    public LiveData<List<Usuario>> getUsuarios() { return usuarios; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getMensaje() { return mensaje; }
    public LiveData<Event<Boolean>> getRegistrado() { return registrado; }

    public void cargarSucursales(boolean todas) {
        catalogoRepository.obtenerSucursales(todas).enqueue(new Callback<SucursalesResponse>() {
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

    public void cargarUsuarios() {
        gestionRepository.obtenerUsuarios().enqueue(new Callback<UsuariosResponse>() {
            @Override
            public void onResponse(Call<UsuariosResponse> call, Response<UsuariosResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUsuarios() != null) {
                    usuarios.setValue(response.body().getUsuarios());
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudieron cargar los usuarios")));
                }
            }
            @Override public void onFailure(Call<UsuariosResponse> call, Throwable t) {
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void registrarUsuario(String nombre, String correo, String contrasena, String rol, Integer idSucursal) {
        loading.setValue(true);
        RegistroUsuarioRequest request = new RegistroUsuarioRequest(nombre, correo, contrasena, rol, idSucursal);
        gestionRepository.registrarUsuario(request).enqueue(new Callback<MensajeResponse>() {
            @Override
            public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    mensaje.setValue(new Event<>(response.body() == null ? "Usuario registrado correctamente" : response.body().getMensaje()));
                    registrado.setValue(new Event<>(true));
                    cargarUsuarios();
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudo registrar el usuario")));
                }
            }
            @Override public void onFailure(Call<MensajeResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }

    public void eliminarUsuario(int idUsuario) {
        loading.setValue(true);
        gestionRepository.eliminarUsuario(idUsuario).enqueue(new Callback<MensajeResponse>() {
            @Override
            public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    mensaje.setValue(new Event<>(response.body() == null ? "Usuario eliminado" : response.body().getMensaje()));
                    cargarUsuarios();
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(response, "No se pudo eliminar el usuario")));
                }
            }
            @Override public void onFailure(Call<MensajeResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }
}
