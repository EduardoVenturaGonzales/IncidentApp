package com.example.incidentapp.model;
import com.google.gson.annotations.SerializedName;

public class SolicitudAnulacion {
    private int id;
    @SerializedName("id_incidente")
    private int idIncidente;
    private String titulo;
    @SerializedName("estado_incidente")
    private String estadoIncidente;
    @SerializedName("id_solicitante")
    private int idSolicitante;
    private String solicitante;
    private String motivo;
    private String estado;
    @SerializedName("id_administrador")
    private Integer idAdministrador;
    @SerializedName("comentario_respuesta")
    private String comentarioRespuesta;
    @SerializedName("fecha_solicitud")
    private String fechaSolicitud;
    @SerializedName("fecha_respuesta")
    private String fechaRespuesta;

    public int getId() {
        return id;
    }
    public int getIdIncidente() {
        return idIncidente;
    }
    public String getTitulo() {
        return titulo;
    }
    public String getEstadoIncidente() {
        return estadoIncidente;
    }
    public int getIdSolicitante() {
        return idSolicitante;
    }
    public String getSolicitante() {
        return solicitante;
    }
    public String getMotivo() {
        return motivo;
    }
    public String getEstado() {
        return estado;
    }
    public Integer getIdAdministrador() {
        return idAdministrador;
    }
    public String getComentarioRespuesta() {
        return comentarioRespuesta;
    }
    public String getFechaSolicitud() {
        return fechaSolicitud;
    }
    public String getFechaRespuesta() {
        return fechaRespuesta;
    }
}