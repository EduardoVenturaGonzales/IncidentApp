package com.example.incidentapp.model;

public class ResponderAnulacionRequest {
    private final String accion;
    private final String comentario;

    public ResponderAnulacionRequest(String accion, String comentario) {
        this.accion = accion;
        this.comentario = comentario;
    }
}
