package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;

public class Notificacion {
    private int id;

    @SerializedName("id_usuario")
    private int idUsuario;

    @SerializedName("id_incidente")
    private Integer idIncidente;

    private String tipo;
    private String titulo;
    private String mensaje;
    private int leida;
    private String fecha;

    public int getId() { return id; }
    public int getIdUsuario() { return idUsuario; }
    public Integer getIdIncidente() { return idIncidente; }
    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getMensaje() { return mensaje; }
    public int getLeida() { return leida; }
    public boolean isLeida() { return leida == 1; }
    public String getFecha() { return fecha; }
}
