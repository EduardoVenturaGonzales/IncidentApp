package com.example.incidentapp.local.entity;
import androidx.room.Entity; import androidx.room.PrimaryKey;
@Entity(tableName="solicitudes_pendientes") public class SolicitudPendienteEntity { @PrimaryKey(autoGenerate=true) public long id; public int idUsuario,idSucursal; public Integer idActivo; public String titulo,descripcion,categoria,prioridad,foto1Path,foto2Path,fechaCreacion; }
