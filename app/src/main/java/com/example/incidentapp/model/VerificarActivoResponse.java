package com.example.incidentapp.model;
import com.google.gson.annotations.SerializedName;

public class VerificarActivoResponse {
    private boolean coincide;
    private String mensaje;
    @SerializedName("codigo_esperado")
    private String codigoEsperado;
    @SerializedName("codigo_escaneado")
    private String codigoEscaneado;

    public boolean isCoincide() {
        return coincide;
    }
    public String getMensaje() {
        return mensaje;
    }
    public String getCodigoEsperado() {
        return codigoEsperado;
    }
    public String getCodigoEscaneado() {
        return codigoEscaneado;
    }
}