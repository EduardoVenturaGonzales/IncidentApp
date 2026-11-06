package com.example.incidentapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.Sucursal;
import com.example.incidentapp.model.SucursalesResponse;
import com.example.incidentapp.repository.CatalogoRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaViewModel extends ViewModel {

    private final CatalogoRepository repository = new CatalogoRepository();

    private final MutableLiveData<List<Sucursal>> sucursales =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> mensaje = new MutableLiveData<>();

    public LiveData<List<Sucursal>> getSucursales() {
        return sucursales;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Event<String>> getMensaje() {
        return mensaje;
    }

    public void cargarSucursales() {
        loading.setValue(true);

        repository.obtenerSucursales().enqueue(new Callback<SucursalesResponse>() {
            @Override
            public void onResponse(
                    Call<SucursalesResponse> call,
                    Response<SucursalesResponse> response
            ) {
                loading.setValue(false);

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getSucursales() != null) {
                    sucursales.setValue(response.body().getSucursales());
                } else {
                    mensaje.setValue(new Event<>(ApiErrorUtils.obtenerMensaje(
                            response,
                            "No se pudo cargar la sucursal"
                    )));
                }
            }

            @Override
            public void onFailure(Call<SucursalesResponse> call, Throwable t) {
                loading.setValue(false);
                mensaje.setValue(new Event<>("No se pudo conectar con la API"));
            }
        });
    }
}
