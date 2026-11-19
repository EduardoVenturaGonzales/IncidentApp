package com.example.incidentapp.ui.home;
import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.incidentapp.R;
import com.example.incidentapp.databinding.FragmentHomeBinding;
import com.example.incidentapp.model.Incidente;
import com.example.incidentapp.model.InicioResumenResponse;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.util.AppEvents;
import com.example.incidentapp.util.TecnicoLocationManager;
import com.example.incidentapp.viewmodel.HomeViewModel;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private boolean esDesarrollador, esTecnico;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable poll = new Runnable() {
        @Override public void run() {
            if (isAdded() && binding != null) {
                if (!esDesarrollador) {
                    viewModel.cargarResumenServidor(false);
                }
                handler.postDelayed(this, 5000);
            }
        }
    };

    private final BroadcastReceiver syncReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (viewModel != null && !esDesarrollador) {
                viewModel.cargarResumenServidor(false);
            }
        }
    };
    private boolean receiverRegistrado = false;
    private final ActivityResultLauncher<String[]> permisoUbicacion = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), r -> {
                boolean ok = Boolean.TRUE.equals(r.get(Manifest.permission.ACCESS_FINE_LOCATION)) || Boolean.TRUE.equals(r.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (ok) iniciarUbicacion();
                else if (binding != null) Snackbar.make(binding.getRoot(), "La ubicación necesita permiso", Snackbar.LENGTH_LONG).show();
            });

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, Bundle state) {
        super.onViewCreated(view, state);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        SessionManager se = SessionManager.getInstance();
        String rol = se.getRol();
        boolean admin = "Administrador".equalsIgnoreCase(rol);
        boolean solicitante = "Solicitante".equalsIgnoreCase(rol);
        esTecnico = "Tecnico".equalsIgnoreCase(rol);
        esDesarrollador = "Desarrollador".equalsIgnoreCase(rol);

        binding.tvBienvenida.setText("Bienvenido " + se.getNombre());
        String lugar = esDesarrollador ? (!se.getEmpresa().isEmpty() ? se.getEmpresa() : "Plataforma IncidentApp") : se.getSucursal();
        binding.tvUsuario.setText(lugar == null || lugar.trim().isEmpty() ? "IncidentApp" : lugar);
        binding.tvRol.setText(rol);

        binding.tvTituloResumen.setVisibility(esDesarrollador ? View.GONE : View.VISIBLE);
        binding.contenedorResumenIncidentes.setVisibility(esDesarrollador ? View.GONE : View.VISIBLE);
        binding.btnVerIncidentes.setVisibility(esDesarrollador ? View.GONE : View.VISIBLE);
        binding.btnNuevoIncidente.setVisibility(solicitante ? View.VISIBLE : View.GONE);
        binding.btnVerMapa.setVisibility((esDesarrollador || admin || esTecnico) ? View.VISIBLE : View.GONE);
        binding.btnCompartirUbicacion.setVisibility(esTecnico ? View.VISIBLE : View.GONE);
        binding.btnReportes.setVisibility(admin ? View.VISIBLE : View.GONE);
        binding.btnGestionEmpresas.setVisibility(esDesarrollador ? View.VISIBLE : View.GONE);
        binding.btnRegistrarSucursal.setVisibility(esDesarrollador ? View.VISIBLE : View.GONE);
        binding.btnRegistrarUsuario.setVisibility((esDesarrollador || admin) ? View.VISIBLE : View.GONE);
        binding.btnGestionActivos.setVisibility(admin ? View.VISIBLE : View.GONE);
        binding.btnSolicitudesAnulacion.setVisibility(admin ? View.VISIBLE : View.GONE);
        binding.contenedorNotificaciones.setVisibility(esDesarrollador ? View.GONE : View.VISIBLE);

        if (esDesarrollador) {
            binding.btnVerMapa.setText("Ver todas las sucursales en mapa");
            binding.btnRegistrarUsuario.setText("Gestionar administradores");
        } else if (admin) {
            binding.btnVerMapa.setText("Ver sucursales y técnicos en mapa");
            binding.btnRegistrarUsuario.setText("Gestionar técnicos y solicitantes");
        } else if (esTecnico) {
            binding.btnVerMapa.setText("Ver sucursal en mapa");
        }

        actualizarBotonUbicacion();
        binding.btnVerIncidentes.setOnClickListener(x -> nav(R.id.action_homeFragment_to_listaIncidentesFragment));
        binding.btnNuevoIncidente.setOnClickListener(x -> nav(R.id.action_homeFragment_to_nuevoIncidenteFragment));
        binding.btnVerMapa.setOnClickListener(x -> nav(R.id.action_homeFragment_to_mapaFragment));
        binding.btnGestionEmpresas.setOnClickListener(x -> nav(R.id.action_homeFragment_to_gestionEmpresasFragment));
        binding.btnRegistrarSucursal.setOnClickListener(x -> nav(R.id.action_homeFragment_to_registrarSucursalFragment));
        binding.btnRegistrarUsuario.setOnClickListener(x -> nav(R.id.action_homeFragment_to_registrarUsuarioFragment));
        binding.btnGestionActivos.setOnClickListener(x -> nav(R.id.action_homeFragment_to_gestionActivosFragment));
        binding.btnSolicitudesAnulacion.setOnClickListener(x -> nav(R.id.action_homeFragment_to_solicitudesAnulacionFragment));
        binding.btnNotificaciones.setOnClickListener(x -> nav(R.id.action_homeFragment_to_notificacionesFragment));
        binding.btnReportes.setOnClickListener(x -> nav(R.id.action_homeFragment_to_reportesFragment));
        binding.btnCompartirUbicacion.setOnClickListener(x -> alternarUbicacion());
        binding.btnCerrarSesion.setOnClickListener(x -> {
            if (esTecnico && TecnicoLocationManager.get(requireContext()).isCompartiendo())
                TecnicoLocationManager.get(requireContext()).detener(listenerUbicacion());
            SessionManager.getInstance().cerrarSesion();
            nav(R.id.action_homeFragment_to_loginFragment);
        });

        viewModel.getIncidentes().observe(getViewLifecycleOwner(), this::actualizarResumenListaLocal);
        viewModel.getResumen().observe(getViewLifecycleOwner(), this::actualizarResumenServidor);
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            if (!esDesarrollador) binding.progressHome.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });
        viewModel.getNoLeidas().observe(getViewLifecycleOwner(), n -> {
            int z = n == null ? 0 : n;
            binding.tvBadgeNotificaciones.setText(z > 99 ? "99+" : String.valueOf(z));
            binding.tvBadgeNotificaciones.setVisibility(z > 0 ? View.VISIBLE : View.GONE);
        });

        if (!esDesarrollador) viewModel.cargarResumen();
    }

    private void actualizarResumenServidor(InicioResumenResponse.IncidentesResumen r) {
        if (binding == null || r == null) return;
        binding.tvCantidadAbiertos.setText(String.valueOf(r.getAbiertos()));
        binding.tvCantidadProceso.setText(String.valueOf(r.getEnProceso()));
        binding.tvCantidadResueltos.setText(String.valueOf(r.getResueltos()));
    }

    private void actualizarResumenListaLocal(List<Incidente> lista) {
        if (binding == null || lista == null) return;
        int abiertos=0, proceso=0, resueltos=0;
        for (Incidente i : lista) {
            if ("Abierto".equalsIgnoreCase(i.getEstado())) abiertos++;
            else if ("En proceso".equalsIgnoreCase(i.getEstado())) proceso++;
            else if ("Resuelto".equalsIgnoreCase(i.getEstado())) resueltos++;
        }
        if (viewModel.getResumen().getValue() == null) {
            binding.tvCantidadAbiertos.setText(String.valueOf(abiertos));
            binding.tvCantidadProceso.setText(String.valueOf(proceso));
            binding.tvCantidadResueltos.setText(String.valueOf(resueltos));
        }
    }

    private TecnicoLocationManager.Listener listenerUbicacion(){
        return new TecnicoLocationManager.Listener(){
            public void onMensaje(String m){
                if(binding!=null) Snackbar.make(binding.getRoot(),m,Snackbar.LENGTH_SHORT).show();
            }
            public void onEstado(boolean a){
                if(binding!=null) actualizarBotonUbicacion();
            }
        };
    }

    private void alternarUbicacion(){
        TecnicoLocationManager m=TecnicoLocationManager.get(requireContext());
        if(m.isCompartiendo()){
            m.detener(listenerUbicacion());
            return;
        }
        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permisoUbicacion.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION});
        } else iniciarUbicacion();
    }

    private void iniciarUbicacion(){
        TecnicoLocationManager.get(requireContext()).iniciar(listenerUbicacion());
    }
    private void actualizarBotonUbicacion(){
        if(binding!=null && esTecnico)
            binding.btnCompartirUbicacion.setText(TecnicoLocationManager.get(requireContext()).isCompartiendo()?"Dejar de compartir ubicación":"Compartir mi ubicación");
    }
    private void nav(int id){
        NavHostFragment.findNavController(this).navigate(id);
    }

    @Override public void onResume(){
        super.onResume();
        handler.removeCallbacks(poll);
        handler.post(poll);
        if (!receiverRegistrado) {
            IntentFilter f = new IntentFilter(AppEvents.ACTION_SYNC_COMPLETADA);
            if (Build.VERSION.SDK_INT >= 33) requireContext().registerReceiver(syncReceiver, f, Context.RECEIVER_NOT_EXPORTED);
            else
                ContextCompat.registerReceiver(requireContext(), syncReceiver, f, ContextCompat.RECEIVER_NOT_EXPORTED);
            receiverRegistrado = true;
        }
    }

    @Override public void onPause(){
        super.onPause();
        handler.removeCallbacks(poll);
        if (receiverRegistrado) {
            try {
                requireContext().unregisterReceiver(syncReceiver);
            } catch (Exception ignored) {}
            receiverRegistrado = false;
        }
    }

    @Override public void onDestroyView(){
        super.onDestroyView();
        binding=null;
    }
}