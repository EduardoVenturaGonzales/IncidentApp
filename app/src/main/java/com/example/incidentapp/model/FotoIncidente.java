package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;

public class FotoIncidente {
    private int id;

    @SerializedName("id_incidente")
    private int idIncidente;

    @SerializedName("ruta_archivo")
    private String rutaArchivo;

    @SerializedName("fecha_subida")
    private String fechaSubida;

    private String url;

    public int getId() { return id; }
    public int getIdIncidente() { return idIncidente; }
    public String getRutaArchivo() { return rutaArchivo; }
    public String getFechaSubida() { return fechaSubida; }
    public String getUrl() { return url; }
}
