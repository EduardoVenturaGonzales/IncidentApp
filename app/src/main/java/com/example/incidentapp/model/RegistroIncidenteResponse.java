package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;

public class RegistroIncidenteResponse {
    private String mensaje;

    @SerializedName("id_incidente")
    private int idIncidente;

    @SerializedName("cantidad_fotos")
    private int cantidadFotos;

    public String getMensaje() { return mensaje; }
    public int getIdIncidente() { return idIncidente; }
    public int getCantidadFotos() { return cantidadFotos; }
}
