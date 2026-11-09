package com.example.incidentapp.ui.gestion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.incidentapp.databinding.FragmentRegistrarSucursalBinding;
import com.example.incidentapp.model.Empresa;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.RegistrarSucursalViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class RegistrarSucursalFragment extends Fragment {

    private FragmentRegistrarSucursalBinding binding;
    private RegistrarSucursalViewModel viewModel;
    private final List<Empresa> empresas = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRegistrarSucursalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!"Desarrollador".equalsIgnoreCase(SessionManager.getInstance().getRol())) {
            Snackbar.make(binding.getRoot(), "Solo el desarrollador puede registrar sucursales", Snackbar.LENGTH_LONG).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        viewModel = new ViewModelProvider(this).get(RegistrarSucursalViewModel.class);

        binding.btnVolver.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );
        binding.btnRegistrar.setOnClickListener(v -> registrar());

        viewModel.getEmpresas().observe(getViewLifecycleOwner(), lista -> {
            empresas.clear();
            if (lista != null) {
                empresas.addAll(lista);
            }

            List<String> nombres = new ArrayList<>();
            for (Empresa empresa : empresas) {
                nombres.add(empresa.getNombre());
            }

            binding.actEmpresa.setAdapter(new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    nombres
            ));
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean activo = Boolean.TRUE.equals(loading);
            binding.progressRegistro.setVisibility(activo ? View.VISIBLE : View.GONE);
            binding.btnRegistrar.setEnabled(!activo);
        });

        viewModel.getMensaje().observe(getViewLifecycleOwner(), evento -> {
            String mensaje = evento.getContentIfNotHandled();
            if (mensaje != null) {
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getRegistrado().observe(getViewLifecycleOwner(), evento -> {
            Boolean registrado = evento.getContentIfNotHandled();
            if (Boolean.TRUE.equals(registrado)) {
                limpiarFormulario();
            }
        });

        viewModel.cargarEmpresas();
    }

    private void registrar() {
        String empresaNombre = binding.actEmpresa.getText().toString().trim();
        String nombre = texto(binding.etNombre);
        String direccion = texto(binding.etDireccion);
        String latitudTexto = texto(binding.etLatitud);
        String longitudTexto = texto(binding.etLongitud);

        binding.tilEmpresa.setError(null);
        binding.tilNombre.setError(null);
        binding.tilDireccion.setError(null);
        binding.tilLatitud.setError(null);
        binding.tilLongitud.setError(null);

        Empresa empresa = buscarEmpresa(empresaNombre);
        if (empresa == null) {
            binding.tilEmpresa.setError("Selecciona una empresa");
            return;
        }
        if (nombre.isEmpty()) {
            binding.tilNombre.setError("El nombre es obligatorio");
            return;
        }
        if (direccion.isEmpty()) {
            binding.tilDireccion.setError("La dirección es obligatoria");
            return;
        }
        if (latitudTexto.isEmpty()) {
            binding.tilLatitud.setError("La latitud es obligatoria");
            return;
        }
        if (longitudTexto.isEmpty()) {
            binding.tilLongitud.setError("La longitud es obligatoria");
            return;
        }

        double latitud;
        double longitud;
        try {
            latitud = Double.parseDouble(latitudTexto);
            longitud = Double.parseDouble(longitudTexto);
        } catch (NumberFormatException e) {
            Snackbar.make(binding.getRoot(), "Las coordenadas no son válidas", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (latitud < -90 || latitud > 90) {
            binding.tilLatitud.setError("La latitud debe estar entre -90 y 90");
            return;
        }
        if (longitud < -180 || longitud > 180) {
            binding.tilLongitud.setError("La longitud debe estar entre -180 y 180");
            return;
        }

        viewModel.registrarSucursal(
                nombre,
                direccion,
                latitud,
                longitud,
                empresa.getId()
        );
    }

    private Empresa buscarEmpresa(String nombre) {
        for (Empresa empresa : empresas) {
            if (empresa.getNombre().equals(nombre)) {
                return empresa;
            }
        }
        return null;
    }

    private String texto(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void limpiarFormulario() {
        binding.actEmpresa.setText("", false);
        binding.etNombre.setText("");
        binding.etDireccion.setText("");
        binding.etLatitud.setText("");
        binding.etLongitud.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
