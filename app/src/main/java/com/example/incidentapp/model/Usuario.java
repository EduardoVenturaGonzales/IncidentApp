package com.example.incidentapp.model;
import com.google.gson.annotations.SerializedName;

public class Usuario {
    private int id;
    private String nombre;
    private String correo;
    private String rol;
    private int activo;
    private String sucursal;
    private String empresa;

    @SerializedName("id_empresa")
    private int idEmpresa;

    @SerializedName("id_sucursal")
    private int idSucursal;

    public int getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getCorreo() {
        return correo;
    }
    public String getRol() {
        return rol;
    }
    public int getActivo() {
        return activo;
    }
    public String getSucursal() {
        return sucursal;
    }
    public String getEmpresa() {
        return empresa;
    }
    public int getIdEmpresa() {
        return idEmpresa;
    }
    public int getIdSucursal() {
        return idSucursal;
    }
}