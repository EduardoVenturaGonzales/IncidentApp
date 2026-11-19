package com.example.incidentapp.model;
import com.google.gson.annotations.SerializedName;

public class RegistroIncidenteResponse {
    private String mensaje;
    @SerializedName("id_incidente") private int idIncidente;
    @SerializedName("cantidad_fotos") private int cantidadFotos;
    @SerializedName("client_request_id") private String clientRequestId;
    @SerializedName("ya_existia") private boolean yaExistia;

    public String getMensaje() {
        return mensaje;
    }
    public int getIdIncidente() {
        return idIncidente;
    }
    public int getCantidadFotos() {
        return cantidadFotos;
    }
    public String getClientRequestId() {
        return clientRequestId;
    }
    public boolean isYaExistia() {
        return yaExistia;
    }
}