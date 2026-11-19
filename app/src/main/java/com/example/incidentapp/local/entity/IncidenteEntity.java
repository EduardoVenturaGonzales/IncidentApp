package com.example.incidentapp.local.entity;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import com.example.incidentapp.model.Incidente;

@Entity(tableName="incidentes_cache", primaryKeys={"idUsuarioCache","id"})
public class IncidenteEntity {
 public int idUsuarioCache;
 public int id;
 public String titulo,descripcion,categoria,prioridad,estado,fechaRegistro,fechaResolucion,fechaNoResuelto,tecnico,sucursal,codigoQr,activo;
 public Integer idTecnico,idActivo;
 public int idSucursal;
 public static IncidenteEntity from(int usuario, Incidente i){
  IncidenteEntity e=new IncidenteEntity();
  e.idUsuarioCache=usuario;
  e.id=i.getId();
  e.titulo=i.getTitulo();
  e.descripcion=i.getDescripcion();
  e.categoria=i.getCategoria();
  e.prioridad=i.getPrioridad();
  e.estado=i.getEstado();
  e.fechaRegistro=i.getFechaRegistro();
  e.fechaResolucion=i.getFechaResolucion();
  e.fechaNoResuelto=i.getFechaNoResuelto();
  e.tecnico=i.getTecnico();
  e.sucursal=i.getSucursal();
  e.codigoQr=i.getCodigoQr();
  e.activo=i.getActivo();
  e.idTecnico=i.getIdTecnico();
  e.idActivo=i.getIdActivo();
  e.idSucursal=i.getIdSucursal();
  return e;
 }
 public Incidente toModel(){
  Incidente i=new Incidente();
  i.setId(id);
  i.setTitulo(titulo);
  i.setDescripcion(descripcion);
  i.setCategoria(categoria);
  i.setPrioridad(prioridad);
  i.setEstado(estado);
  i.setFechaRegistro(fechaRegistro);
  i.setFechaResolucion(fechaResolucion);
  i.setFechaNoResuelto(fechaNoResuelto);
  i.setTecnico(tecnico);
  i.setSucursal(sucursal);
  i.setCodigoQr(codigoQr);
  i.setActivo(activo);
  i.setIdTecnico(idTecnico);
  i.setIdActivo(idActivo);
  i.setIdSucursal(idSucursal);
  return i;
 }
}