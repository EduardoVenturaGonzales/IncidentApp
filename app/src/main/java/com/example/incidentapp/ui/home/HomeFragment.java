package com.example.incidentapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.incidentapp.R;
import com.example.incidentapp.databinding.FragmentHomeBinding;
import com.example.incidentapp.model.Incidente;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.HomeViewModel;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private boolean esDesarrollador;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = FragmentHomeBinding.inflate(inflater, container, false); return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, Bundle state) {
        super.onViewCreated(view, state);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        SessionManager sesion = SessionManager.getInstance();
        String rol = sesion.getRol();
        boolean admin = "Administrador".equalsIgnoreCase(rol);
        boolean tecnico = "Tecnico".equalsIgnoreCase(rol);
        boolean solicitante = "Solicitante".equalsIgnoreCase(rol);
        esDesarrollador = "Desarrollador".equalsIgnoreCase(rol);

        binding.tvUsuario.setText(sesion.getNombre());
        binding.tvRol.setText(rol);
        binding.tvTituloResumen.setVisibility(esDesarrollador ? View.GONE : View.VISIBLE);
        binding.contenedorResumenIncidentes.setVisibility(esDesarrollador ? View.GONE : View.VISIBLE);
        binding.btnVerIncidentes.setVisibility(esDesarrollador ? View.GONE : View.VISIBLE);
        binding.btnNuevoIncidente.setVisibility(solicitante ? View.VISIBLE : View.GONE);
        binding.btnVerMapa.setVisibility((esDesarrollador || admin || tecnico) ? View.VISIBLE : View.GONE);
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
            binding.btnVerMapa.setText("Ver sucursales en mapa");
            binding.btnRegistrarUsuario.setText("Gestionar técnicos y solicitantes");
        } else if (tecnico) {
            binding.btnVerMapa.setText("Ver sucursal en mapa");
        }

        binding.btnVerIncidentes.setOnClickListener(v -> nav(R.id.action_homeFragment_to_listaIncidentesFragment));
        binding.btnNuevoIncidente.setOnClickListener(v -> nav(R.id.action_homeFragment_to_nuevoIncidenteFragment));
        binding.btnVerMapa.setOnClickListener(v -> nav(R.id.action_homeFragment_to_mapaFragment));
        binding.btnGestionEmpresas.setOnClickListener(v -> nav(R.id.action_homeFragment_to_gestionEmpresasFragment));
        binding.btnRegistrarSucursal.setOnClickListener(v -> nav(R.id.action_homeFragment_to_registrarSucursalFragment));
        binding.btnRegistrarUsuario.setOnClickListener(v -> nav(R.id.action_homeFragment_to_registrarUsuarioFragment));
        binding.btnGestionActivos.setOnClickListener(v -> nav(R.id.action_homeFragment_to_gestionActivosFragment));
        binding.btnSolicitudesAnulacion.setOnClickListener(v -> nav(R.id.action_homeFragment_to_solicitudesAnulacionFragment));
        binding.btnNotificaciones.setOnClickListener(v -> nav(R.id.action_homeFragment_to_notificacionesFragment));
        binding.btnCerrarSesion.setOnClickListener(v -> {
            SessionManager.getInstance().cerrarSesion();
            nav(R.id.action_homeFragment_to_loginFragment);
        });

        viewModel.getIncidentes().observe(getViewLifecycleOwner(), this::actualizarResumen);
        viewModel.getLoading().observe(getViewLifecycleOwner(), l -> {
            if (!esDesarrollador) binding.progressHome.setVisibility(Boolean.TRUE.equals(l) ? View.VISIBLE : View.GONE);
        });
        viewModel.getNoLeidas().observe(getViewLifecycleOwner(), total -> {
            int n = total == null ? 0 : total;
            binding.tvBadgeNotificaciones.setText(n > 99 ? "99+" : String.valueOf(n));
            binding.tvBadgeNotificaciones.setVisibility(n > 0 ? View.VISIBLE : View.GONE);
        });

        if (!esDesarrollador) {
            viewModel.cargarResumen();
            viewModel.cargarNotificaciones();
        }
    }

    private void nav(int id) { NavHostFragment.findNavController(this).navigate(id); }

    private void actualizarResumen(List<Incidente> lista) {
        int abiertos=0, proceso=0, resueltos=0;
        if(lista!=null) for(Incidente i:lista) {
            if("Abierto".equalsIgnoreCase(i.getEstado())) abiertos++;
            else if("En proceso".equalsIgnoreCase(i.getEstado())) proceso++;
            else if("Resuelto".equalsIgnoreCase(i.getEstado()) || "Cerrado".equalsIgnoreCase(i.getEstado())) resueltos++;
        }
        binding.tvCantidadAbiertos.setText(String.valueOf(abiertos));
        binding.tvCantidadProceso.setText(String.valueOf(proceso));
        binding.tvCantidadResueltos.setText(String.valueOf(resueltos));
    }

    @Override public void onResume() {
        super.onResume();
        if(viewModel!=null && !esDesarrollador){ viewModel.cargarResumen(); viewModel.cargarNotificaciones(); }
    }
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}
