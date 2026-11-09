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

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        SessionManager sesion = SessionManager.getInstance();
        String rol = sesion.getRol();
        binding.tvUsuario.setText(sesion.getNombre());
        binding.tvRol.setText(rol);

        esDesarrollador = "Desarrollador".equalsIgnoreCase(rol);
        boolean administrador = "Administrador".equalsIgnoreCase(rol);
        boolean tecnico = "Tecnico".equalsIgnoreCase(rol);
        boolean solicitante = "Solicitante".equalsIgnoreCase(rol);

        binding.tvTituloResumen.setVisibility(esDesarrollador ? View.GONE : View.VISIBLE);
        binding.contenedorResumenIncidentes.setVisibility(esDesarrollador ? View.GONE : View.VISIBLE);
        binding.progressHome.setVisibility(View.GONE);

        binding.btnVerIncidentes.setVisibility(esDesarrollador ? View.GONE : View.VISIBLE);
        binding.btnNuevoIncidente.setVisibility(solicitante ? View.VISIBLE : View.GONE);
        binding.btnVerMapa.setVisibility(
                esDesarrollador || administrador || tecnico ? View.VISIBLE : View.GONE
        );
        binding.btnRegistrarSucursal.setVisibility(esDesarrollador ? View.VISIBLE : View.GONE);
        binding.btnRegistrarUsuario.setVisibility(
                esDesarrollador || administrador ? View.VISIBLE : View.GONE
        );

        if (esDesarrollador) {
            binding.btnVerMapa.setText("Ver todas las sucursales en mapa");
            binding.btnRegistrarUsuario.setText("Registrar administrador");
        } else if (administrador) {
            binding.btnVerMapa.setText("Ver sucursales en mapa");
            binding.btnRegistrarUsuario.setText("Registrar técnico o solicitante");
        } else if (tecnico) {
            binding.btnVerMapa.setText("Ver sucursal en mapa");
        }

        binding.btnVerIncidentes.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_homeFragment_to_listaIncidentesFragment)
        );

        binding.btnVerMapa.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_homeFragment_to_mapaFragment)
        );

        binding.btnNuevoIncidente.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_homeFragment_to_nuevoIncidenteFragment)
        );

        binding.btnRegistrarSucursal.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_homeFragment_to_registrarSucursalFragment)
        );

        binding.btnRegistrarUsuario.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_homeFragment_to_registrarUsuarioFragment)
        );

        binding.btnCerrarSesion.setOnClickListener(v -> {
            SessionManager.getInstance().cerrarSesion();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeFragment_to_loginFragment);
        });

        viewModel.getIncidentes().observe(getViewLifecycleOwner(), this::actualizarResumen);
        viewModel.getLoading().observe(getViewLifecycleOwner(), cargando -> {
            if (!esDesarrollador) {
                binding.progressHome.setVisibility(
                        Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE
                );
            }
        });

        if (!esDesarrollador) {
            viewModel.cargarResumen();
        }
    }

    private void actualizarResumen(List<Incidente> lista) {
        int abiertos = 0;
        int proceso = 0;
        int resueltos = 0;

        if (lista != null) {
            for (Incidente incidente : lista) {
                if ("Abierto".equalsIgnoreCase(incidente.getEstado())) {
                    abiertos++;
                } else if ("En proceso".equalsIgnoreCase(incidente.getEstado())) {
                    proceso++;
                } else if ("Resuelto".equalsIgnoreCase(incidente.getEstado())
                        || "Cerrado".equalsIgnoreCase(incidente.getEstado())) {
                    resueltos++;
                }
            }
        }

        binding.tvCantidadAbiertos.setText(String.valueOf(abiertos));
        binding.tvCantidadProceso.setText(String.valueOf(proceso));
        binding.tvCantidadResueltos.setText(String.valueOf(resueltos));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null && !esDesarrollador) {
            viewModel.cargarResumen();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
