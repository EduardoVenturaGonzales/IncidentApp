package com.example.incidentapp.repository;
import android.os.Handler;
import android.os.Looper;
import com.example.incidentapp.local.AppDatabase;
import com.example.incidentapp.local.entity.*;
import com.example.incidentapp.model.*;
import java.util.*;
import java.util.concurrent.*;
public class LocalCacheRepository {
    private static final ExecutorService IO=Executors.newSingleThreadExecutor();
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    public interface Callback<T>{
        void onResult(T value);
    }
    public void guardarIncidentes(int uid,List<Incidente> lista){
        IO.execute(()->{
            List<IncidenteEntity> es=new ArrayList<>();
            if(lista!=null)for(Incidente i:lista)es.add(IncidenteEntity.from(uid,i));
            AppDatabase.get().incidenteDao().reemplazarCache(uid,es);
        });
    }
    public void guardarDetalle(int uid,DetalleIncidenteResponse d){
        if(d==null||d.getIncidente()==null)return;IO.execute(()->{
            AppDatabase.get().incidenteDao().insertarUno(IncidenteEntity.from(uid,d.getIncidente()));
            AppDatabase.get().historialDao().limpiar(uid,d.getIncidente().getId());if(d.getHistorial()!=null){
                List<HistorialEntity> hs=new ArrayList<>();
                for(Historial h:d.getHistorial())hs.add(HistorialEntity.from(uid,h));
                if(!hs.isEmpty())AppDatabase.get().historialDao().insertar(hs);
            }
        });
    }
    public void cargarIncidentes(int uid,String estado,String prioridad,Callback<List<Incidente>> cb){
        IO.execute(()->{
            List<Incidente> out=new ArrayList<>();
            for(IncidenteEntity e:AppDatabase.get().incidenteDao().listar(uid)){
                Incidente i=e.toModel();
                if(estado!=null&&!estado.equalsIgnoreCase(i.getEstado()))continue;
                if(prioridad!=null&&!prioridad.equalsIgnoreCase(i.getPrioridad()))continue;
                out.add(i);
            }
            MAIN.post(()->cb.onResult(out));
        });
    }
    public void cargarDetalle(int uid,int id,Callback<DetalleIncidenteResponse> cb){
        IO.execute(()->{
            IncidenteEntity e=AppDatabase.get().incidenteDao().obtener(uid,id);
            if(e==null){
                MAIN.post(()->cb.onResult(null));
                return;
            }
            List<Historial> hs=new ArrayList<>();
            for(HistorialEntity h:AppDatabase.get().historialDao().listar(uid,id))hs.add(h.toModel());DetalleIncidenteResponse d=new DetalleIncidenteResponse(e.toModel(),hs,Collections.emptyList(),null);
            MAIN.post(()->cb.onResult(d));
        });
    }
}