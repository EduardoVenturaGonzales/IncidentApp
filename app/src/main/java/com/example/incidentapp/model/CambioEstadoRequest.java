package com.example.incidentapp.model;

public class CambioEstadoRequest {
    private final String estado;
    private final String comentario;

    public CambioEstadoRequest(String estado, String comentario) {
        this.estado = estado;
        this.comentario = comentario;
    }
}
