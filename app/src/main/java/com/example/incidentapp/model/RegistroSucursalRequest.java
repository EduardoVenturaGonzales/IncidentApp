package com.example.incidentapp.model;
import com.google.gson.annotations.SerializedName;

public class RegistroSucursalRequest {
    private final String nombre;
    private final String direccion;
    private final Double latitud;
    private final Double longitud;

    @SerializedName("id_empresa")
    private final int idEmpresa;

    public RegistroSucursalRequest(
            String nombre,
            String direccion,
            Double latitud,
            Double longitud,
            int idEmpresa
    ) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.idEmpresa = idEmpresa;
    }
}