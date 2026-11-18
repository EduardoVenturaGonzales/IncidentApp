package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;

public class Sucursal {
    private int id;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private String empresa;

    @SerializedName("id_empresa")
    private int idEmpresa;

    @SerializedName("incidentes_abiertos")
    private int incidentesAbiertos;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public String getEmpresa() { return empresa; }
    public int getIdEmpresa() { return idEmpresa; }
    public int getIncidentesAbiertos() { return incidentesAbiertos; }

    public void setId(int v) { id = v; }
    public void setNombre(String v) { nombre = v; }
    public void setDireccion(String v) { direccion = v; }
    public void setIdEmpresa(int v) { idEmpresa = v; }

    @Override
    public String toString() {
        return nombre;
    }
}
