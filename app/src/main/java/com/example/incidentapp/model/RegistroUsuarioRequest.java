package com.example.incidentapp.model;
import com.google.gson.annotations.SerializedName;

public class RegistroUsuarioRequest {
    private final String nombre;
    private final String correo;
    private final String contrasena;
    private final String rol;

    @SerializedName("id_sucursal")
    private final Integer idSucursal;

    public RegistroUsuarioRequest(
            String nombre,
            String correo,
            String contrasena,
            String rol,
            Integer idSucursal
    ) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
        this.idSucursal = idSucursal;
    }
}