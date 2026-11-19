package com.example.incidentapp.network;

import com.example.incidentapp.model.*;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("auth/login") Call<LoginResponse> login(@Body LoginRequest request);

    @GET("inicio/resumen") Call<InicioResumenResponse> obtenerInicioResumen();

    @GET("incidentes")
    Call<IncidentesResponse> obtenerIncidentes(@Query("estado") String estado, @Query("prioridad") String prioridad);

    @GET("incidentes/{id}") Call<DetalleIncidenteResponse> obtenerIncidente(@Path("id") int idIncidente);

    @Multipart
    @POST("incidentes")
    Call<RegistroIncidenteResponse> registrarIncidente(
            @Part("client_request_id") RequestBody clientRequestId,
            @Part("titulo") RequestBody titulo,
            @Part("descripcion") RequestBody descripcion,
            @Part("categoria") RequestBody categoria,
            @Part("prioridad") RequestBody prioridad,
            @Part("id_sucursal") RequestBody idSucursal,
            @Part("id_activo") RequestBody idActivo,
            @Part List<MultipartBody.Part> fotos
    );

    @PUT("incidentes/{id}/estado") Call<MensajeResponse> cambiarEstado(@Path("id") int idIncidente, @Body CambioEstadoRequest request);
    @PUT("incidentes/{id}/vencimiento-sla") Call<MensajeResponse> marcarVencimientoSla(@Path("id") int idIncidente);
    @PUT("incidentes/{id}/prioridad") Call<MensajeResponse> cambiarPrioridad(@Path("id") int idIncidente, @Body CambioPrioridadRequest request);
    @PUT("incidentes/{id}/asignar") Call<MensajeResponse> asignarTecnico(@Path("id") int idIncidente, @Body AsignarTecnicoRequest request);
    @POST("incidentes/{id}/verificar-activo") Call<VerificarActivoResponse> verificarActivo(@Path("id") int idIncidente, @Body VerificarActivoRequest request);
    @POST("incidentes/{id}/solicitar-anulacion") Call<MensajeResponse> solicitarAnulacion(@Path("id") int idIncidente, @Body MotivoAnulacionRequest request);
    @GET("incidentes/solicitudes-anulacion") Call<SolicitudesAnulacionResponse> obtenerSolicitudesAnulacion(@Query("estado") String estado);
    @PUT("incidentes/{id}/anulacion") Call<MensajeResponse> responderAnulacion(@Path("id") int idIncidente, @Body ResponderAnulacionRequest request);
    @HTTP(method="DELETE", path="incidentes/{id}", hasBody=true) Call<MensajeResponse> eliminarIncidente(@Path("id") int idIncidente, @Body EliminarIncidenteRequest request);

    @GET("sucursales") Call<SucursalesResponse> obtenerSucursales(@Query("todas") Integer todas);
    @POST("sucursales") Call<MensajeResponse> registrarSucursal(@Body RegistroSucursalRequest request);
    @DELETE("sucursales/{id}") Call<MensajeResponse> eliminarSucursal(@Path("id") int idSucursal);

    @GET("empresas") Call<EmpresasResponse> obtenerEmpresas();
    @POST("empresas") Call<MensajeResponse> registrarEmpresa(@Body RegistroEmpresaRequest request);
    @DELETE("empresas/{id}") Call<MensajeResponse> eliminarEmpresa(@Path("id") int idEmpresa);

    @GET("usuarios") Call<UsuariosResponse> obtenerUsuarios();
    @POST("usuarios") Call<MensajeResponse> registrarUsuario(@Body RegistroUsuarioRequest request);
    @DELETE("usuarios/{id}") Call<MensajeResponse> eliminarUsuario(@Path("id") int idUsuario);
    @GET("usuarios/tecnicos") Call<TecnicosResponse> obtenerTecnicos();

    @GET("activos") Call<ActivosResponse> obtenerActivos();
    @Multipart @POST("activos") Call<RegistroActivoResponse> registrarActivo(
            @Part("nombre") RequestBody nombre,
            @Part("tipo") RequestBody tipo,
            @Part("estado") RequestBody estado,
            @Part List<MultipartBody.Part> fotos
    );
    @GET("activos/{codigo_qr}") Call<ActivoResponse> obtenerActivo(@Path("codigo_qr") String codigoQr);
    @DELETE("activos/{id}") Call<MensajeResponse> eliminarActivo(@Path("id") int idActivo);

    @POST("tecnicos/ubicacion") Call<MensajeResponse> actualizarUbicacion(@Body UbicacionTecnicoRequest request);
    @DELETE("tecnicos/ubicacion") Call<MensajeResponse> detenerUbicacion();
    @GET("tecnicos/ubicaciones") Call<UbicacionesTecnicosResponse> obtenerUbicacionesTecnicos();
    @GET("reportes/resumen") Call<ReporteResumen> obtenerReporteResumen();

    @GET("notificaciones") Call<NotificacionesResponse> obtenerNotificaciones(@Query("solo_no_leidas") Integer soloNoLeidas, @Query("limite") Integer limite);
    @GET("notificaciones/pendientes-android") Call<NotificacionesResponse> obtenerNotificacionesPendientesAndroid(@Query("limite") Integer limite);
    @PUT("notificaciones/{id}/entregada") Call<MensajeResponse> marcarNotificacionEntregada(@Path("id") int idNotificacion);
    @PUT("notificaciones/{id}/leer") Call<MensajeResponse> marcarNotificacionLeida(@Path("id") int idNotificacion);
    @PUT("notificaciones/leer-todas") Call<MensajeResponse> marcarTodasLeidas();
}
