package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DetalleIncidenteResponse {
    private Incidente incidente;
    private List<Historial> historial;
    private List<FotoIncidente> fotos;

    @SerializedName("solicitud_anulacion")
    private SolicitudAnulacion solicitudAnulacion;

    public Incidente getIncidente() { return incidente; }
    public List<Historial> getHistorial() { return historial; }
    public List<FotoIncidente> getFotos() { return fotos; }
    public SolicitudAnulacion getSolicitudAnulacion() { return solicitudAnulacion; }
}
