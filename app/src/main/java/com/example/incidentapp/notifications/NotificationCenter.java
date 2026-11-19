package com.example.incidentapp.notifications;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.incidentapp.MainActivity;
import com.example.incidentapp.R;
import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.Notificacion;
import com.example.incidentapp.model.NotificacionesResponse;
import com.example.incidentapp.repository.NotificacionRepository;
import com.example.incidentapp.session.SessionManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class NotificationCenter {
    private static final String CHANNEL = "incidentapp_alertas_v2";
    private NotificationCenter() {}

    public static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Alertas de incidentes", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Avisos de nuevos incidentes, asignaciones y cambios de estado");
            ch.enableVibration(true);
            context.getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    public static void sincronizar(Context context) {
        if (!SessionManager.getInstance().haySesion()) return;
        new NotificacionRepository().obtenerPendientesAndroid(30).enqueue(new Callback<NotificacionesResponse>() {
            @Override public void onResponse(Call<NotificacionesResponse> call, Response<NotificacionesResponse> r) {
                if (!r.isSuccessful() || r.body() == null || r.body().getNotificaciones() == null) return;
                mostrarPendientes(context.getApplicationContext(), r.body().getNotificaciones());
            }
            @Override public void onFailure(Call<NotificacionesResponse> call, Throwable t) { }
        });
    }

    private static void mostrarPendientes(Context context, List<Notificacion> lista) {
        for (Notificacion n : lista) {
            if (mostrar(context, n)) {
                new NotificacionRepository().marcarEntregada(n.getId()).enqueue(new Callback<MensajeResponse>() {
                    @Override public void onResponse(Call<MensajeResponse> call, Response<MensajeResponse> response) { }
                    @Override public void onFailure(Call<MensajeResponse> call, Throwable t) { }
                });
            }
        }
    }

    private static boolean mostrar(Context context, Notificacion n) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("id_incidente", n.getIdIncidente() == null ? 0 : n.getIdIncidente());
        i.putExtra("tipo_notificacion", n.getTipo());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, n.getId(), i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_2d)
                .setContentTitle(n.getTitulo())
                .setContentText(n.getMensaje())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(n.getMensaje()))
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
        try {
            NotificationManagerCompat.from(context).notify(n.getId(), b.build());
            return true;
        } catch (SecurityException ex) {
            return false;
        }
    }
}