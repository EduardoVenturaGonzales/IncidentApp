package com.example.incidentapp.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.incidentapp.local.entity.SolicitudPendienteEntity;

import java.util.List;

@Dao
public interface SolicitudPendienteDao {
    @Query("SELECT * FROM solicitudes_pendientes WHERE idUsuario=:uid ORDER BY id")
    List<SolicitudPendienteEntity> listar(int uid);

    @Query("SELECT * FROM solicitudes_pendientes ORDER BY id")
    List<SolicitudPendienteEntity> listarTodas();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertar(SolicitudPendienteEntity item);

    @Update
    void actualizar(SolicitudPendienteEntity item);

    @Delete
    void eliminar(SolicitudPendienteEntity item);
}
