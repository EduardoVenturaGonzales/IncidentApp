package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificacionesResponse {
    private List<Notificacion> notificaciones;

    @SerializedName("no_leidas")
    private int noLeidas;

    public List<Notificacion> getNotificaciones() { return notificaciones; }
    public int getNoLeidas() { return noLeidas; }
}
