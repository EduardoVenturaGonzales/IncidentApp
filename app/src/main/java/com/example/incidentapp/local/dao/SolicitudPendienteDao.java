package com.example.incidentapp.local.dao;
import androidx.room.*; import com.example.incidentapp.local.entity.SolicitudPendienteEntity; import java.util.List;
@Dao public interface SolicitudPendienteDao { @Query("SELECT * FROM solicitudes_pendientes WHERE idUsuario=:uid ORDER BY id") List<SolicitudPendienteEntity> listar(int uid); @Query("SELECT * FROM solicitudes_pendientes ORDER BY id") List<SolicitudPendienteEntity> listarTodas(); @Insert long insertar(SolicitudPendienteEntity item); @Delete void eliminar(SolicitudPendienteEntity item); }
