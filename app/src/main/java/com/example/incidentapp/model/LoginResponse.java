package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("access_token")
    private String accessToken;
    private Usuario usuario;

    public String getAccessToken() { return accessToken; }
    public Usuario getUsuario() { return usuario; }
}
