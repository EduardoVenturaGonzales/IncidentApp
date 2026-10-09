package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;

public class Sucursal {
    private int id;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;

    @SerializedName("id_empresa")
    private int idEmpresa;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public int getIdEmpresa() { return idEmpresa; }

    @Override
    public String toString() { return nombre; }
}
