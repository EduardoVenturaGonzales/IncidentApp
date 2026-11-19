package com.example.incidentapp.repository;
import com.example.incidentapp.model.ReporteResumen;
import com.example.incidentapp.network.RetrofitClient;
import retrofit2.Call;

public class ReporteRepository {
    public Call<ReporteResumen> obtener(){
        return RetrofitClient.getApiService().obtenerReporteResumen();
    }
}