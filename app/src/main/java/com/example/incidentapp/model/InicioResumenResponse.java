package com.example.incidentapp.model;
import com.google.gson.annotations.SerializedName;

public class InicioResumenResponse {
    private Usuario usuario;
    @SerializedName("notificaciones_no_leidas")
    private int notificacionesNoLeidas;

    @SerializedName("ultima_notificacion_id")
    private int ultimaNotificacionId;

    private IncidentesResumen incidentes;
    private Integer empresas;
    private Integer sucursales;
    private Integer administradores;

    public Usuario getUsuario() {
        return usuario;
    }
    public int getNotificacionesNoLeidas() {
        return notificacionesNoLeidas;
    }
    public int getUltimaNotificacionId() {
        return ultimaNotificacionId;
    }
    public IncidentesResumen getIncidentes() {
        return incidentes;
    }
    public Integer getEmpresas() {
        return empresas;
    }
    public Integer getSucursales() {
        return sucursales;
    }
    public Integer getAdministradores() {
        return administradores;
    }

    public static class IncidentesResumen {
        private int total;
        @SerializedName("ultimo_incidente_id") private int ultimoIncidenteId;
        @SerializedName("Abierto") private int abiertos;
        @SerializedName("En proceso") private int enProceso;
        @SerializedName("Resuelto") private int resueltos;
        @SerializedName("No Resuelto") private int noResueltos;
        @SerializedName("Anulado") private int anulados;

        public int getTotal() {
            return total;
        }
        public int getUltimoIncidenteId() {
            return ultimoIncidenteId;
        }
        public int getAbiertos() {
            return abiertos;
        }
        public int getEnProceso() {
            return enProceso;
        }
        public int getResueltos() {
            return resueltos;
        }
        public int getNoResueltos() {
            return noResueltos;
        }
        public int getAnulados() {
            return anulados;
        }
    }
}