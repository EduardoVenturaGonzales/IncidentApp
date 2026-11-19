package com.example.incidentapp;
import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.*;
import android.os.*;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.*;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.incidentapp.databinding.ActivityMainBinding;
import com.example.incidentapp.notifications.NotificationCenter;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.util.NetworkUtils;
import com.example.incidentapp.worker.SyncScheduler;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ConnectivityManager.NetworkCallback netCb;
    private final Handler notifHandler = new Handler(Looper.getMainLooper());
    private int incidentePendienteAbrir = 0;

    private final Runnable notifPoll = new Runnable() {
        @Override public void run() {
            NotificationCenter.sincronizar(MainActivity.this);
            notifHandler.postDelayed(this, 5000);
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main,(v,insets)->{
            Insets x=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(x.left,x.top,x.right,x.bottom); return insets;
        });

        procesarIntent(getIntent());
        configurarNavegacionNotificacion();
        actualizarRed();
        ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        netCb=new ConnectivityManager.NetworkCallback(){
            @Override public void onAvailable(Network n){
                runOnUiThread(()->{
                    actualizarRed();
                    SyncScheduler.sincronizarAhora(MainActivity.this);
                    NotificationCenter.sincronizar(MainActivity.this);
                });
            }
            @Override public void onLost(Network n){
                runOnUiThread(()->
                        actualizarRed());
            }
        };

        try{
            cm.registerDefaultNetworkCallback(netCb);
        }catch(Exception ignored){}

        if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), ok->{
                if(Boolean.TRUE.equals(ok)) NotificationCenter.sincronizar(this);
            }).launch(Manifest.permission.POST_NOTIFICATIONS);
        }
        NotificationCenter.sincronizar(this);
    }

    @Override protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);
        procesarIntent(intent);
        intentarAbrirIncidente();
    }

    private void procesarIntent(Intent intent){
        if(intent!=null) incidentePendienteAbrir=intent.getIntExtra("id_incidente",0);
    }

    private void configurarNavegacionNotificacion(){
        NavHostFragment host=(NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if(host==null)return;
        host.getNavController().addOnDestinationChangedListener((controller,destination,args)->intentarAbrirIncidente());
    }

    private void intentarAbrirIncidente(){
        if(incidentePendienteAbrir<=0 || !SessionManager.getInstance().haySesion())return;
        NavHostFragment host=(NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if(host==null)return;
        NavController nav=host.getNavController();
        int destino=nav.getCurrentDestination()==null?0:nav.getCurrentDestination().getId();
        if(destino==R.id.loginFragment)return;
        if(destino==R.id.detalleIncidenteFragment)return;
        Bundle b=new Bundle(); b.putInt("id_incidente",incidentePendienteAbrir);
        incidentePendienteAbrir=0;
        try{
            nav.navigate(R.id.detalleIncidenteFragment,b);
        }catch(Exception ignored){}
    }

    @Override protected void onResume(){
        super.onResume();
        notifHandler.removeCallbacks(notifPoll);
        notifHandler.post(notifPoll);
        intentarAbrirIncidente();
    }
    @Override protected void onPause(){
        super.onPause();
        notifHandler.removeCallbacks(notifPoll);
    }
    private void actualizarRed(){
        binding.tvOfflineBanner.setVisibility(NetworkUtils.conectado(this)?android.view.View.GONE:android.view.View.VISIBLE);
    }
    @Override protected void onDestroy(){
        super.onDestroy();
        try{
            ((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE)).unregisterNetworkCallback(netCb);
        }catch(Exception ignored){}}
}