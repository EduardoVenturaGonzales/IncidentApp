package com.example.incidentapp.network;

import com.example.incidentapp.model.ActivoResponse;
import com.example.incidentapp.model.AsignarTecnicoRequest;
import com.example.incidentapp.model.CambioEstadoRequest;
import com.example.incidentapp.model.DetalleIncidenteResponse;
import com.example.incidentapp.model.IncidentesResponse;
import com.example.incidentapp.model.LoginRequest;
import com.example.incidentapp.model.LoginResponse;
import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.RegistroIncidenteResponse;
import com.example.incidentapp.model.SucursalesResponse;
import com.example.incidentapp.model.TecnicosResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/fcm-token")
    Call<MensajeResponse> registrarFcmToken(@Body java.util.Map<String, Object> body);

    @GET("incidentes")
    Call<IncidentesResponse> obtenerIncidentes(
            @Query("estado") String estado,
            @Query("prioridad") String prioridad
    );

    @GET("incidentes/{id}")
    Call<DetalleIncidenteResponse> obtenerIncidente(@Path("id") int idIncidente);

    @Multipart
    @POST("incidentes")
    Call<RegistroIncidenteResponse> registrarIncidente(
            @Part("titulo") RequestBody titulo,
            @Part("descripcion") RequestBody descripcion,
            @Part("categoria") RequestBody categoria,
            @Part("prioridad") RequestBody prioridad,
            @Part("id_sucursal") RequestBody idSucursal,
            @Part("id_activo") RequestBody idActivo,
            @Part List<MultipartBody.Part> fotos
    );

    @PUT("incidentes/{id}/estado")
    Call<MensajeResponse> cambiarEstado(
            @Path("id") int idIncidente,
            @Body CambioEstadoRequest request
    );

    @PUT("incidentes/{id}/asignar")
    Call<MensajeResponse> asignarTecnico(
            @Path("id") int idIncidente,
            @Body AsignarTecnicoRequest request
    );

    @GET("sucursales")
    Call<SucursalesResponse> obtenerSucursales();

    @GET("activos/{codigo_qr}")
    Call<ActivoResponse> obtenerActivo(@Path("codigo_qr") String codigoQr);

    @GET("usuarios/tecnicos")
    Call<TecnicosResponse> obtenerTecnicos();
}
