package com.example.incidentapp.repository;

import com.example.incidentapp.model.EmpresasResponse;
import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.RegistroSucursalRequest;
import com.example.incidentapp.model.RegistroUsuarioRequest;
import com.example.incidentapp.network.RetrofitClient;

import retrofit2.Call;

public class GestionRepository {

    public Call<EmpresasResponse> obtenerEmpresas() {
        return RetrofitClient.getApiService().obtenerEmpresas();
    }

    public Call<MensajeResponse> registrarSucursal(RegistroSucursalRequest request) {
        return RetrofitClient.getApiService().registrarSucursal(request);
    }

    public Call<MensajeResponse> registrarUsuario(RegistroUsuarioRequest request) {
        return RetrofitClient.getApiService().registrarUsuario(request);
    }
}
