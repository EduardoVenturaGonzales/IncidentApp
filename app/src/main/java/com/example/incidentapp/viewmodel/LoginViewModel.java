package com.example.incidentapp.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.incidentapp.model.LoginResponse;
import com.example.incidentapp.repository.AuthRepository;
import com.example.incidentapp.util.ApiErrorUtils;
import com.example.incidentapp.util.Event;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {

    private static final String TAG = "LOGIN_API";

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    private final MutableLiveData<Event<LoginResponse>> loginExitoso =
            new MutableLiveData<>();

    private final MutableLiveData<Event<String>> error =
            new MutableLiveData<>();


    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Event<LoginResponse>> getLoginExitoso() {
        return loginExitoso;
    }

    public LiveData<Event<String>> getError() {
        return error;
    }


    public void iniciarSesion(
            String correo,
            String contrasena
    ) {

        loading.setValue(true);

        repository.login(
                correo,
                contrasena
        ).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(
                    Call<LoginResponse> call,
                    Response<LoginResponse> response
            ) {

                loading.setValue(false);

                Log.d(
                        TAG,
                        "Codigo HTTP: " + response.code()
                );


                if (
                        response.isSuccessful()
                                && response.body() != null
                ) {

                    Log.d(
                            TAG,
                            "Login correcto"
                    );

                    loginExitoso.setValue(
                            new Event<>(
                                    response.body()
                            )
                    );

                } else {

                    String mensaje =
                            ApiErrorUtils.obtenerMensaje(
                                    response,
                                    "No se pudo iniciar sesion. Codigo: "
                                            + response.code()
                            );

                    Log.e(
                            TAG,
                            mensaje
                    );

                    error.setValue(
                            new Event<>(
                                    mensaje
                            )
                    );
                }
            }


            @Override
            public void onFailure(
                    Call<LoginResponse> call,
                    Throwable t
            ) {

                loading.setValue(false);

                Log.e(
                        TAG,
                        "ERROR RETROFIT",
                        t
                );


                String tipoError =
                        t.getClass().getSimpleName();

                String detalle =
                        t.getMessage();


                String mensaje =
                        "Error: "
                                + tipoError
                                + "\n"
                                + (
                                detalle == null
                                        ? "Sin detalle"
                                        : detalle
                        );


                error.setValue(
                        new Event<>(
                                mensaje
                        )
                );
            }
        });
    }
}