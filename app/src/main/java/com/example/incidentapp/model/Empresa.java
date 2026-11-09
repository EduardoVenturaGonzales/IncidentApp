package com.example.incidentapp.model;

public class Empresa {
    private int id;
    private String nombre;
    private String ruc;
    private int activo;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRuc() { return ruc; }
    public int getActivo() { return activo; }

    @Override
    public String toString() {
        return nombre;
    }
}
