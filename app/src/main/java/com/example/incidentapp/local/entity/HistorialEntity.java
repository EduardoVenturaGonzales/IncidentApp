package com.example.incidentapp.local.entity;
import androidx.room.Entity;
import com.example.incidentapp.model.Historial;

@Entity(tableName="historial_cache", primaryKeys={"idUsuarioCache","id"})
public class HistorialEntity {
    public int idUsuarioCache,id,idIncidente,idUsuario;
    public String estadoAnterior,estadoNuevo,comentario,usuario,rol,fechaCambio;
    public static HistorialEntity from(int uid,Historial h){
        HistorialEntity e=new HistorialEntity();
        e.idUsuarioCache=uid;
        e.id=h.getId();
        e.idIncidente=h.getIdIncidente();
        e.idUsuario=h.getIdUsuario();
        e.estadoAnterior=h.getEstadoAnterior();
        e.estadoNuevo=h.getEstadoNuevo();
        e.comentario=h.getComentario();
        e.usuario=h.getUsuario();
        e.rol=h.getRol();
        e.fechaCambio=h.getFechaCambio();
        return e;
    }
    public Historial toModel(){
        Historial h=new Historial();
        h.setId(id);
        h.setIdIncidente(idIncidente);
        h.setIdUsuario(idUsuario);
        h.setEstadoAnterior(estadoAnterior);
        h.setEstadoNuevo(estadoNuevo);
        h.setComentario(comentario);
        h.setUsuario(usuario);
        h.setRol(rol);
        h.setFechaCambio(fechaCambio);
        return h;
    }
}