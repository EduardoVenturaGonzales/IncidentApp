package com.example.incidentapp.util;

import com.example.incidentapp.model.MensajeResponse;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Response;

public final class ApiErrorUtils {

    private ApiErrorUtils() {
    }

    public static String obtenerMensaje(Response<?> response, String mensajePorDefecto) {
        if (response.errorBody() == null) {
            return mensajePorDefecto;
        }

        try {
            String json = response.errorBody().string();
            MensajeResponse error = new Gson().fromJson(json, MensajeResponse.class);
            if (error != null && error.getMensaje() != null && !error.getMensaje().trim().isEmpty()) {
                return error.getMensaje();
            }
        } catch (IOException | RuntimeException ignored) {
        }

        return mensajePorDefecto;
    }
}
