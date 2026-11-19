package com.example.incidentapp.repository;

import com.example.incidentapp.model.InicioResumenResponse;
import com.example.incidentapp.network.RetrofitClient;

import retrofit2.Call;

public class InicioRepository {
    public Call<InicioResumenResponse> obtenerResumen() {
        return RetrofitClient.getApiService().obtenerInicioResumen();
    }
}
