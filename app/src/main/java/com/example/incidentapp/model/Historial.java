package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;

public class Historial {
    private int id;

    @SerializedName("id_incidente")
    private int idIncidente;

    @SerializedName("estado_anterior")
    private String estadoAnterior;

    @SerializedName("estado_nuevo")
    private String estadoNuevo;

    private String comentario;

    @SerializedName("id_usuario")
    private int idUsuario;

    private String usuario;
    private String rol;

    @SerializedName("fecha_cambio")
    private String fechaCambio;

    public int getId() { return id; }
    public int getIdIncidente() { return idIncidente; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public String getComentario() { return comentario; }
    public int getIdUsuario() { return idUsuario; }
    public String getUsuario() { return usuario; }
    public String getRol() { return rol; }
    public String getFechaCambio() { return fechaCambio; }
}
