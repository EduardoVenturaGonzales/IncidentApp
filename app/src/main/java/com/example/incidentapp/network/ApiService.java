package com.example.incidentapp.network;

import com.example.incidentapp.model.ActivoResponse;
import com.example.incidentapp.model.ActivosResponse;
import com.example.incidentapp.model.AsignarTecnicoRequest;
import com.example.incidentapp.model.CambioEstadoRequest;
import com.example.incidentapp.model.DetalleIncidenteResponse;
import com.example.incidentapp.model.EliminarIncidenteRequest;
import com.example.incidentapp.model.EmpresasResponse;
import com.example.incidentapp.model.IncidentesResponse;
import com.example.incidentapp.model.LoginRequest;
import com.example.incidentapp.model.LoginResponse;
import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.MotivoAnulacionRequest;
import com.example.incidentapp.model.NotificacionesResponse;
import com.example.incidentapp.model.RegistroActivoResponse;
import com.example.incidentapp.model.RegistroEmpresaRequest;
import com.example.incidentapp.model.RegistroIncidenteResponse;
import com.example.incidentapp.model.RegistroSucursalRequest;
import com.example.incidentapp.model.RegistroUsuarioRequest;
import com.example.incidentapp.model.ResponderAnulacionRequest;
import com.example.incidentapp.model.SolicitudesAnulacionResponse;
import com.example.incidentapp.model.SucursalesResponse;
import com.example.incidentapp.model.TecnicosResponse;
import com.example.incidentapp.model.UsuariosResponse;
import com.example.incidentapp.model.VerificarActivoRequest;
import com.example.incidentapp.model.VerificarActivoResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // INCIDENTES
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

    @POST("incidentes/{id}/verificar-activo")
    Call<VerificarActivoResponse> verificarActivo(
            @Path("id") int idIncidente,
            @Body VerificarActivoRequest request
    );

    @POST("incidentes/{id}/solicitar-anulacion")
    Call<MensajeResponse> solicitarAnulacion(
            @Path("id") int idIncidente,
            @Body MotivoAnulacionRequest request
    );

    @GET("incidentes/solicitudes-anulacion")
    Call<SolicitudesAnulacionResponse> obtenerSolicitudesAnulacion(
            @Query("estado") String estado
    );

    @PUT("incidentes/{id}/anulacion")
    Call<MensajeResponse> responderAnulacion(
            @Path("id") int idIncidente,
            @Body ResponderAnulacionRequest request
    );

    @HTTP(method = "DELETE", path = "incidentes/{id}", hasBody = true)
    Call<MensajeResponse> eliminarIncidente(
            @Path("id") int idIncidente,
            @Body EliminarIncidenteRequest request
    );

    // SUCURSALES / EMPRESAS
    @GET("sucursales")
    Call<SucursalesResponse> obtenerSucursales(@Query("todas") Integer todas);

    @POST("sucursales")
    Call<MensajeResponse> registrarSucursal(@Body RegistroSucursalRequest request);

    @DELETE("sucursales/{id}")
    Call<MensajeResponse> eliminarSucursal(@Path("id") int idSucursal);

    @GET("empresas")
    Call<EmpresasResponse> obtenerEmpresas();

    @POST("empresas")
    Call<MensajeResponse> registrarEmpresa(@Body RegistroEmpresaRequest request);

    @DELETE("empresas/{id}")
    Call<MensajeResponse> eliminarEmpresa(@Path("id") int idEmpresa);

    // USUARIOS
    @GET("usuarios")
    Call<UsuariosResponse> obtenerUsuarios();

    @POST("usuarios")
    Call<MensajeResponse> registrarUsuario(@Body RegistroUsuarioRequest request);

    @DELETE("usuarios/{id}")
    Call<MensajeResponse> eliminarUsuario(@Path("id") int idUsuario);

    @GET("usuarios/tecnicos")
    Call<TecnicosResponse> obtenerTecnicos();

    // ACTIVOS TI
    @GET("activos")
    Call<ActivosResponse> obtenerActivos();

    @Multipart
    @POST("activos")
    Call<RegistroActivoResponse> registrarActivo(
            @Part("nombre") RequestBody nombre,
            @Part("tipo") RequestBody tipo,
            @Part("estado") RequestBody estado,
            @Part List<MultipartBody.Part> fotos
    );

    @GET("activos/{codigo_qr}")
    Call<ActivoResponse> obtenerActivo(@Path("codigo_qr") String codigoQr);

    @DELETE("activos/{id}")
    Call<MensajeResponse> eliminarActivo(@Path("id") int idActivo);

    // NOTIFICACIONES INTERNAS
    @GET("notificaciones")
    Call<NotificacionesResponse> obtenerNotificaciones(
            @Query("solo_no_leidas") Integer soloNoLeidas,
            @Query("limite") Integer limite
    );

    @PUT("notificaciones/{id}/leer")
    Call<MensajeResponse> marcarNotificacionLeida(@Path("id") int idNotificacion);

    @PUT("notificaciones/leer-todas")
    Call<MensajeResponse> marcarTodasLeidas();
}
