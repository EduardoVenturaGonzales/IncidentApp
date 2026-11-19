package com.example.incidentapp.worker;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.incidentapp.local.AppDatabase;
import com.example.incidentapp.local.entity.SolicitudPendienteEntity;
import com.example.incidentapp.model.RegistroIncidenteResponse;
import com.example.incidentapp.repository.IncidenteRepository;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.util.AppEvents;
import com.example.incidentapp.util.MultipartUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MultipartBody;
import retrofit2.Response;

public class SincronizarPendientesWorker extends Worker {
    public SincronizarPendientesWorker(@NonNull Context context, @NonNull WorkerParameters params) { super(context, params); }

    @NonNull
    @Override
    public Result doWork() {
        SessionManager.init(getApplicationContext());
        AppDatabase.init(getApplicationContext());
        if (!SessionManager.getInstance().haySesion()) return Result.success();

        boolean huboErrorTemporal = false;
        boolean sincronizoAlgo = false;
        try {
            List<SolicitudPendienteEntity> items = AppDatabase.get().solicitudPendienteDao()
                    .listar(SessionManager.getInstance().getIdUsuario());
            IncidenteRepository repo = new IncidenteRepository();

            for (SolicitudPendienteEntity e : items) {
                try {
                    if (e.clientRequestId == null || e.clientRequestId.trim().isEmpty()) {
                        e.clientRequestId = UUID.randomUUID().toString();
                        AppDatabase.get().solicitudPendienteDao().actualizar(e);
                    }

                    List<MultipartBody.Part> fotos = new ArrayList<>();
                    if (e.foto1Path != null && new File(e.foto1Path).isFile())
                        fotos.add(MultipartUtils.crearParteFotoDesdeArchivo(new File(e.foto1Path)));
                    if (e.foto2Path != null && new File(e.foto2Path).isFile())
                        fotos.add(MultipartUtils.crearParteFotoDesdeArchivo(new File(e.foto2Path)));

                    Response<RegistroIncidenteResponse> r = repo.registrarIncidente(
                            e.clientRequestId, e.titulo, e.descripcion, e.categoria, e.prioridad,
                            e.idSucursal, e.idActivo, fotos
                    ).execute();

                    if (r.isSuccessful()) {
                        AppDatabase.get().solicitudPendienteDao().eliminar(e);
                        borrarSiExiste(e.foto1Path);
                        borrarSiExiste(e.foto2Path);
                        sincronizoAlgo = true;
                    } else if (r.code() >= 500 || r.code() == 408 || r.code() == 429) {
                        // Error temporal: no bloquea los demás, pero WorkManager volverá a intentar.
                        huboErrorTemporal = true;
                    }
                    // 4xx permanente: se conserva para revisión, pero NO detiene los demás pendientes.
                } catch (Exception itemError) {
                    huboErrorTemporal = true;
                }
            }

            if (sincronizoAlgo) {
                Intent i = new Intent(AppEvents.ACTION_SYNC_COMPLETADA);
                i.setPackage(getApplicationContext().getPackageName());
                getApplicationContext().sendBroadcast(i);
            }
            return huboErrorTemporal ? Result.retry() : Result.success();
        } catch (Exception ex) {
            return Result.retry();
        }
    }

    private void borrarSiExiste(String path) {
        if (path != null) {
            File f = new File(path);
            if (f.isFile()) f.delete();
        }
    }
}
