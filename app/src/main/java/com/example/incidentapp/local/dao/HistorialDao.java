package com.example.incidentapp.local.dao;
import androidx.room.*; import com.example.incidentapp.local.entity.HistorialEntity; import java.util.List;
@Dao public interface HistorialDao { @Query("SELECT * FROM historial_cache WHERE idUsuarioCache=:uid AND idIncidente=:incidente ORDER BY fechaCambio") List<HistorialEntity> listar(int uid,int incidente); @Insert(onConflict=OnConflictStrategy.REPLACE) void insertar(List<HistorialEntity> items); @Query("DELETE FROM historial_cache WHERE idUsuarioCache=:uid AND idIncidente=:incidente") void limpiar(int uid,int incidente); }
