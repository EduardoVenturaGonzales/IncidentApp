package com.example.incidentapp.local.dao;
import androidx.room.*;
import com.example.incidentapp.local.entity.IncidenteEntity;
import java.util.List;

@Dao
public interface IncidenteDao {
    @Query("SELECT * FROM incidentes_cache WHERE idUsuarioCache=:uid ORDER BY fechaRegistro DESC")
    List<IncidenteEntity> listar(int uid);

    @Insert(onConflict=OnConflictStrategy.REPLACE)
    void insertar(List<IncidenteEntity> items);

    @Insert(onConflict=OnConflictStrategy.REPLACE)
    void insertarUno(IncidenteEntity item);

    @Query("DELETE FROM incidentes_cache WHERE idUsuarioCache=:uid")
    void limpiar(int uid);

    @Query("SELECT * FROM incidentes_cache WHERE idUsuarioCache=:uid AND id=:id LIMIT 1")
    IncidenteEntity obtener(int uid,int id);

    @Transaction
    default void reemplazarCache(int uid, List<IncidenteEntity> items) {
        limpiar(uid);
        if (items != null && !items.isEmpty()) insertar(items);
    }
}