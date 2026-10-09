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
        binding.tvUsuario.setText(sesion.getNombre());
        binding.tvRol.setText(sesion.getRol());

        boolean solicitante = "Solicitante".equalsIgnoreCase(sesion.getRol());
        binding.btnNuevoIncidente.setVisibility(solicitante ? View.VISIBLE : View.GONE);

        binding.btnVerIncidentes.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_homeFragment_to_listaIncidentesFragment)
        );

        binding.btnNuevoIncidente.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_homeFragment_to_nuevoIncidenteFragment)
        );

        binding.btnCerrarSesion.setOnClickListener(v -> {
            SessionManager.getInstance().cerrarSesion();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeFragment_to_loginFragment);
        });

        viewModel.getIncidentes().observe(getViewLifecycleOwner(), this::actualizarResumen);
        viewModel.getLoading().observe(getViewLifecycleOwner(), cargando ->
                binding.progressHome.setVisibility(Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE)
        );

        viewModel.cargarResumen();
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
        if (viewModel != null) {
            viewModel.cargarResumen();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
