package com.example.incidentapp.model;

import java.util.List;

public class DetalleIncidenteResponse {
    private Incidente incidente;
    private List<Historial> historial;
    private List<FotoIncidente> fotos;

    public Incidente getIncidente() { return incidente; }
    public List<Historial> getHistorial() { return historial; }
    public List<FotoIncidente> getFotos() { return fotos; }
}
