package com.example.incidentapp.repository;

import com.example.incidentapp.model.AsignarTecnicoRequest;
import com.example.incidentapp.model.CambioEstadoRequest;
import com.example.incidentapp.model.DetalleIncidenteResponse;
import com.example.incidentapp.model.IncidentesResponse;
import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.RegistroIncidenteResponse;
import com.example.incidentapp.network.RetrofitClient;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class IncidenteRepository {

    public Call<IncidentesResponse> obtenerIncidentes(String estado, String prioridad) {
        return RetrofitClient.getApiService().obtenerIncidentes(estado, prioridad);
    }

    public Call<DetalleIncidenteResponse> obtenerIncidente(int idIncidente) {
        return RetrofitClient.getApiService().obtenerIncidente(idIncidente);
    }

    public Call<RegistroIncidenteResponse> registrarIncidente(
            String titulo,
            String descripcion,
            String categoria,
            String prioridad,
            int idSucursal,
            Integer idActivo,
            List<MultipartBody.Part> fotos
    ) {
        MediaType texto = MediaType.parse("text/plain");

        return RetrofitClient.getApiService().registrarIncidente(
                RequestBody.create(texto, titulo),
                RequestBody.create(texto, descripcion),
                RequestBody.create(texto, categoria),
                RequestBody.create(texto, prioridad),
                RequestBody.create(texto, String.valueOf(idSucursal)),
                RequestBody.create(texto, idActivo == null ? "" : String.valueOf(idActivo)),
                fotos
        );
    }

    public Call<MensajeResponse> cambiarEstado(int idIncidente, String estado, String comentario) {
        return RetrofitClient.getApiService().cambiarEstado(
                idIncidente,
                new CambioEstadoRequest(estado, comentario)
        );
    }

    public Call<MensajeResponse> asignarTecnico(int idIncidente, int idTecnico) {
        return RetrofitClient.getApiService().asignarTecnico(
                idIncidente,
                new AsignarTecnicoRequest(idTecnico)
        );
    }
}
