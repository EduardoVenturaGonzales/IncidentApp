package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;

public class Activo {
    private int id;

    @SerializedName("codigo_qr")
    private String codigoQr;

    private String nombre;
    private String tipo;
    private String estado;

    @SerializedName("id_sucursal")
    private int idSucursal;

    private String sucursal;
    private String direccion;

    @SerializedName("id_empresa")
    private int idEmpresa;

    public int getId() { return id; }
    public String getCodigoQr() { return codigoQr; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getEstado() { return estado; }
    public int getIdSucursal() { return idSucursal; }
    public String getSucursal() { return sucursal; }
    public String getDireccion() { return direccion; }
    public int getIdEmpresa() { return idEmpresa; }
}
