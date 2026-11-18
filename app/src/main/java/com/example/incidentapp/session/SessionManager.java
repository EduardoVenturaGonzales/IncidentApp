package com.example.incidentapp.session;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.incidentapp.model.LoginResponse;
import com.example.incidentapp.model.Usuario;

public class SessionManager {
    private static final String PREFS="incidentapp_session";
    private static final String KEY_TOKEN="token", KEY_ID="id_usuario", KEY_NOMBRE="nombre", KEY_CORREO="correo", KEY_ROL="rol", KEY_ID_EMPRESA="id_empresa", KEY_ID_SUCURSAL="id_sucursal", KEY_SUCURSAL="sucursal", KEY_EMPRESA="empresa";
    private static SessionManager instance;
    private final SharedPreferences preferences;
    private SessionManager(Context context){preferences=context.getApplicationContext().getSharedPreferences(PREFS,Context.MODE_PRIVATE);}
    public static synchronized void init(Context context){if(instance==null)instance=new SessionManager(context);}
    public static SessionManager getInstance(){if(instance==null)throw new IllegalStateException("SessionManager no fue inicializado");return instance;}
    public void guardarSesion(LoginResponse r){Usuario u=r.getUsuario();preferences.edit().putString(KEY_TOKEN,r.getAccessToken()).putInt(KEY_ID,u==null?0:u.getId()).putString(KEY_NOMBRE,u==null?"":u.getNombre()).putString(KEY_CORREO,u==null?"":u.getCorreo()).putString(KEY_ROL,u==null?"":u.getRol()).putInt(KEY_ID_EMPRESA,u==null?0:u.getIdEmpresa()).putInt(KEY_ID_SUCURSAL,u==null?0:u.getIdSucursal()).putString(KEY_SUCURSAL,u==null?"":u.getSucursal()).putString(KEY_EMPRESA,u==null?"":u.getEmpresa()).apply();}
    public void cerrarSesion(){preferences.edit().clear().apply();}
    public boolean haySesion(){String t=getToken();if(t==null||t.trim().isEmpty())return false;return "Desarrollador".equalsIgnoreCase(getRol())||getIdSucursal()>0;}
    public String getToken(){return preferences.getString(KEY_TOKEN,null);} public int getIdUsuario(){return preferences.getInt(KEY_ID,0);} public String getNombre(){return preferences.getString(KEY_NOMBRE,"Usuario");} public String getCorreo(){return preferences.getString(KEY_CORREO,"");} public String getRol(){return preferences.getString(KEY_ROL,"");} public int getIdEmpresa(){return preferences.getInt(KEY_ID_EMPRESA,0);} public int getIdSucursal(){return preferences.getInt(KEY_ID_SUCURSAL,0);} public String getSucursal(){return preferences.getString(KEY_SUCURSAL,"");} public String getEmpresa(){return preferences.getString(KEY_EMPRESA,"");}
}
