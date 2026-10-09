package com.example.incidentapp.model;

public class LoginRequest {
    private final String correo;
    private final String contrasena;

    public LoginRequest(String correo, String contrasena) {
        this.correo = correo;
        this.contrasena = contrasena;
    }
}
