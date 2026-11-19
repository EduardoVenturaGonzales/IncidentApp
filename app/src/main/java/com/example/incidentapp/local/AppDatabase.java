package com.example.incidentapp.local;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.incidentapp.local.dao.HistorialDao;
import com.example.incidentapp.local.dao.IncidenteDao;
import com.example.incidentapp.local.dao.SolicitudPendienteDao;
import com.example.incidentapp.local.entity.HistorialEntity;
import com.example.incidentapp.local.entity.IncidenteEntity;
import com.example.incidentapp.local.entity.SolicitudPendienteEntity;

@Database(
        entities = {IncidenteEntity.class, HistorialEntity.class, SolicitudPendienteEntity.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract IncidenteDao incidenteDao();
    public abstract HistorialDao historialDao();
    public abstract SolicitudPendienteDao solicitudPendienteDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE solicitudes_pendientes ADD COLUMN clientRequestId TEXT");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_solicitudes_pendientes_idUsuario_clientRequestId ON solicitudes_pendientes(idUsuario, clientRequestId)");
        }
    };

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "incidentapp_local.db").addMigrations(MIGRATION_1_2).build();
        }
    }

    public static AppDatabase get() {
        if (instance == null) throw new IllegalStateException("AppDatabase no inicializada");
        return instance;
    }
}