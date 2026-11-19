package com.example.incidentapp.repository;
import com.example.incidentapp.model.ActivoResponse;
import com.example.incidentapp.model.SucursalesResponse;
import com.example.incidentapp.model.TecnicosResponse;
import com.example.incidentapp.network.RetrofitClient;
import retrofit2.Call;

public class CatalogoRepository {
    public Call<SucursalesResponse> obtenerSucursales() {
        return RetrofitClient.getApiService().obtenerSucursales(null);
    }
    public Call<SucursalesResponse> obtenerSucursales(boolean todas) {
        return RetrofitClient.getApiService().obtenerSucursales(todas ? 1 : null);
    }
    public Call<ActivoResponse> obtenerActivo(String codigoQr) {
        return RetrofitClient.getApiService().obtenerActivo(codigoQr);
    }
    public Call<TecnicosResponse> obtenerTecnicos() {
        return RetrofitClient.getApiService().obtenerTecnicos();
    }
}