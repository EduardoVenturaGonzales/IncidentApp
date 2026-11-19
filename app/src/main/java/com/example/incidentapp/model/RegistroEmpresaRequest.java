package com.example.incidentapp.model;

public class RegistroEmpresaRequest {
    private final String nombre;
    private final String ruc;
    public RegistroEmpresaRequest(String nombre, String ruc) {
        this.nombre = nombre;
        this.ruc = ruc;
    }
}