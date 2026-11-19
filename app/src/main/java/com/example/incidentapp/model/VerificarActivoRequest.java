package com.example.incidentapp.model;
import com.google.gson.annotations.SerializedName;

public class VerificarActivoRequest {
    @SerializedName("codigo_qr")
    private final String codigoQr;
    public VerificarActivoRequest(String codigoQr) {
        this.codigoQr = codigoQr;
    }
}