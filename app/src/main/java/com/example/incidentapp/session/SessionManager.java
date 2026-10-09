package com.example.incidentapp.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.incidentapp.model.LoginResponse;
import com.example.incidentapp.model.Usuario;

public class SessionManager {

    private static final String PREFS = "incidentapp_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ID = "id_usuario";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_CORREO = "correo";
    private static final String KEY_ROL = "rol";
    private static final String KEY_ID_EMPRESA = "id_empresa";

    private static SessionManager instance;
    private final SharedPreferences preferences;

    private SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SessionManager no fue inicializado");
        }
        return instance;
    }

    public void guardarSesion(LoginResponse respuesta) {
        Usuario usuario = respuesta.getUsuario();
        preferences.edit()
                .putString(KEY_TOKEN, respuesta.getAccessToken())
                .putInt(KEY_ID, usuario == null ? 0 : usuario.getId())
                .putString(KEY_NOMBRE, usuario == null ? "" : usuario.getNombre())
                .putString(KEY_CORREO, usuario == null ? "" : usuario.getCorreo())
                .putString(KEY_ROL, usuario == null ? "" : usuario.getRol())
                .putInt(KEY_ID_EMPRESA, usuario == null ? 0 : usuario.getIdEmpresa())
                .apply();
    }

    public void cerrarSesion() {
        preferences.edit().clear().apply();
    }

    public boolean haySesion() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public int getIdUsuario() {
        return preferences.getInt(KEY_ID, 0);
    }

    public String getNombre() {
        return preferences.getString(KEY_NOMBRE, "Usuario");
    }

    public String getCorreo() {
        return preferences.getString(KEY_CORREO, "");
    }

    public String getRol() {
        return preferences.getString(KEY_ROL, "");
    }

    public int getIdEmpresa() {
        return preferences.getInt(KEY_ID_EMPRESA, 0);
    }
}
