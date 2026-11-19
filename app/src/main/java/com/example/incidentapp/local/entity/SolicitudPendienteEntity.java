package com.example.incidentapp.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "solicitudes_pendientes",
        indices = {@Index(value = {"idUsuario", "clientRequestId"}, unique = true)}
)
public class SolicitudPendienteEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public int idUsuario;
    public int idSucursal;
    public Integer idActivo;
    public String titulo;
    public String descripcion;
    public String categoria;
    public String prioridad;
    public String foto1Path;
    public String foto2Path;
    public String fechaCreacion;
    public String clientRequestId;
}
