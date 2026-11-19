package com.example.incidentapp.repository;
import com.example.incidentapp.model.ActivosResponse;
import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.RegistroActivoResponse;
import com.example.incidentapp.network.RetrofitClient;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class ActivoRepository {
    public Call<ActivosResponse> obtenerActivos() {
        return RetrofitClient.getApiService().obtenerActivos();
    }

    public Call<RegistroActivoResponse> registrarActivo(String nombre, String tipo, String estado, List<MultipartBody.Part> fotos) {
        MediaType texto = MediaType.parse("text/plain");
        return RetrofitClient.getApiService().registrarActivo(
                RequestBody.create(texto, nombre),
                RequestBody.create(texto, tipo),
                RequestBody.create(texto, estado),
                fotos
        );
    }

    public Call<MensajeResponse> eliminarActivo(int idActivo) {
        return RetrofitClient.getApiService().eliminarActivo(idActivo);
    }
}