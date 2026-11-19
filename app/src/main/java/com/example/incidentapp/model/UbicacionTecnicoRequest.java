package com.example.incidentapp.model;
import com.google.gson.annotations.SerializedName;

public class UbicacionTecnicoRequest {
    private final double latitud,longitud;
    @SerializedName("precision_m")
    private final Float precisionM;
    @SerializedName("id_incidente")
    private final Integer idIncidente;
    public UbicacionTecnicoRequest(double latitud,double longitud,Float precisionM,Integer idIncidente){
        this.latitud=latitud;
        this.longitud=longitud;
        this.precisionM=precisionM;
        this.idIncidente=idIncidente;
    }
}