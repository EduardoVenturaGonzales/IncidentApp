package com.example.incidentapp.repository;

import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.NotificacionesResponse;
import com.example.incidentapp.network.RetrofitClient;

import retrofit2.Call;

public class NotificacionRepository {

    public Call<NotificacionesResponse> obtenerNotificaciones(boolean soloNoLeidas, int limite) {
        return RetrofitClient.getApiService().obtenerNotificaciones(soloNoLeidas ? 1 : 0, limite);
    }

    public Call<MensajeResponse> marcarLeida(int id) {
        return RetrofitClient.getApiService().marcarNotificacionLeida(id);
    }

    public Call<MensajeResponse> marcarTodasLeidas() {
        return RetrofitClient.getApiService().marcarTodasLeidas();
    }
}
