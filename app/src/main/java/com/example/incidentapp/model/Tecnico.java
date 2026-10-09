package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;

public class Tecnico {
    private int id;
    private String nombre;
    private String correo;
    private String rol;
    private int activo;

    @SerializedName("id_empresa")
    private int idEmpresa;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getRol() { return rol; }
    public int getActivo() { return activo; }
    public int getIdEmpresa() { return idEmpresa; }

    @Override
    public String toString() { return nombre; }
}
