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
    private static final String KEY_ID_SUCURSAL = "id_sucursal";

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
                .putInt(KEY_ID_SUCURSAL, usuario == null ? 0 : usuario.getIdSucursal())
                .apply();
    }

    public void cerrarSesion() {
        preferences.edit().clear().apply();
    }

    public boolean haySesion() {
        String token = getToken();

        // Desde la actualización por sucursales, una sesión válida también debe
        // conocer la sucursal del usuario. Esto obliga a iniciar sesión nuevamente
        // si la app conservaba una sesión antigua creada antes de este cambio.
        return token != null
                && !token.trim().isEmpty()
                && getIdSucursal() > 0;
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

    public int getIdSucursal() {
        return preferences.getInt(KEY_ID_SUCURSAL, 0);
    }
}
