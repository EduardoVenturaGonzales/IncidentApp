package com.example.incidentapp.model;

import com.google.gson.annotations.SerializedName;

public class AsignarTecnicoRequest {
    @SerializedName("id_tecnico")
    private final int idTecnico;

    public AsignarTecnicoRequest(int idTecnico) {
        this.idTecnico = idTecnico;
    }
}
