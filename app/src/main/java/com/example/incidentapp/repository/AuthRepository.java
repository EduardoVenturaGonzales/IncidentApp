package com.example.incidentapp.repository;

import com.example.incidentapp.model.LoginRequest;
import com.example.incidentapp.model.LoginResponse;
import com.example.incidentapp.network.RetrofitClient;

import retrofit2.Call;

public class AuthRepository {
    public Call<LoginResponse> login(String correo, String contrasena) {
        return RetrofitClient.getApiService().login(new LoginRequest(correo, contrasena));
    }
}
