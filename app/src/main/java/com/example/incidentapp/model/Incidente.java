package com.example.incidentapp.model;
import com.google.gson.annotations.SerializedName;

public class Incidente {
    private int id;
    private String titulo;
    private String descripcion;
    private String categoria;
    private String prioridad;
    private String estado;

    @SerializedName("fecha_registro")
    private String fechaRegistro;

    @SerializedName("fecha_resolucion")
    private String fechaResolucion;

    @SerializedName("fecha_no_resuelto")
    private String fechaNoResuelto;

    @SerializedName("id_solicitante")
    private int idSolicitante;

    private String solicitante;

    @SerializedName("correo_solicitante")
    private String correoSolicitante;

    @SerializedName("id_tecnico")
    private Integer idTecnico;
    private String tecnico;

    @SerializedName("id_sucursal")
    private int idSucursal;
    private String sucursal;

    @SerializedName("direccion_sucursal")
    private String direccionSucursal;
    private Double latitud;
    private Double longitud;

    @SerializedName("id_empresa")
    private Integer idEmpresa;

    @SerializedName("id_activo")
    private Integer idActivo;

    @SerializedName("codigo_qr")
    private String codigoQr;
    private String activo;

    @SerializedName("tipo_activo")
    private String tipoActivo;

    public int getId() {
        return id;
    }
    public String getTitulo() {
        return titulo;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public String getCategoria() {
        return categoria;
    }
    public String getPrioridad() {
        return prioridad;
    }
    public String getEstado() {
        return estado;
    }
    public String getFechaRegistro() {
        return fechaRegistro;
    }
    public String getFechaResolucion() {
        return fechaResolucion;
    }
    public String getFechaNoResuelto() {
        return fechaNoResuelto;
    }
    public int getIdSolicitante() {
        return idSolicitante;
    }
    public String getSolicitante() {
        return solicitante;
    }
    public String getCorreoSolicitante() {
        return correoSolicitante;
    }
    public Integer getIdTecnico() {
        return idTecnico;
    }
    public String getTecnico() {
        return tecnico;
    }
    public int getIdSucursal() {
        return idSucursal;
    }
    public String getSucursal() {
        return sucursal;
    }
    public String getDireccionSucursal() {
        return direccionSucursal;
    }
    public Double getLatitud() {
        return latitud;
    }
    public Double getLongitud() {
        return longitud;
    }
    public Integer getIdEmpresa() {
        return idEmpresa;
    }
    public Integer getIdActivo() {
        return idActivo;
    }
    public String getCodigoQr() {
        return codigoQr;
    }
    public String getActivo() {
        return activo;
    }
    public String getTipoActivo() {
        return tipoActivo;
    }

    public void setId(int id){
        this.id=id;
    }
    public void setTitulo(String v){
        titulo=v;
    }
    public void setDescripcion(String v){
        descripcion=v;
    }
    public void setCategoria(String v){
        categoria=v;
    }
    public void setPrioridad(String v){
        prioridad=v;
    }
    public void setEstado(String v){
        estado=v;
    }
    public void setFechaRegistro(String v){
        fechaRegistro=v;
    }
    public void setFechaResolucion(String v){
        fechaResolucion=v;
    }
    public void setFechaNoResuelto(String v){
        fechaNoResuelto=v;
    }
    public void setIdSolicitante(int v){
        idSolicitante=v;
    }
    public void setSolicitante(String v){
        solicitante=v;
    }
    public void setCorreoSolicitante(String v){
        correoSolicitante=v;
    }
    public void setIdTecnico(Integer v){
        idTecnico=v;
    }
    public void setTecnico(String v){
        tecnico=v;
    }
    public void setIdSucursal(int v){
        idSucursal=v;
    }
    public void setSucursal(String v){
        sucursal=v;
    }
    public void setDireccionSucursal(String v){
        direccionSucursal=v;
    }
    public void setLatitud(Double v){
        latitud=v;
    }
    public void setLongitud(Double v){
        longitud=v;
    }
    public void setIdEmpresa(Integer v){
        idEmpresa=v;
    }
    public void setIdActivo(Integer v){
        idActivo=v;
    }
    public void setCodigoQr(String v){
        codigoQr=v;
    }
    public void setActivo(String v){
        activo=v;
    } public void setTipoActivo(String v){
        tipoActivo=v;
    }
}