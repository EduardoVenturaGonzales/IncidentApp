package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;

public class Usuario {
    private int id;
    private String nombre;
    private String correo;
    private String rol;

    @SerializedName("id_empresa")
    private int idEmpresa;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getRol() { return rol; }
    public int getIdEmpresa() { return idEmpresa; }
}
