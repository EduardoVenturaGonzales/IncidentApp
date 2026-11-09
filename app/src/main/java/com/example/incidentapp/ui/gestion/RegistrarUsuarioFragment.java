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

import com.example.incidentapp.databinding.FragmentRegistrarUsuarioBinding;
import com.example.incidentapp.model.Sucursal;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.RegistrarUsuarioViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegistrarUsuarioFragment extends Fragment {

    private FragmentRegistrarUsuarioBinding binding;
    private RegistrarUsuarioViewModel viewModel;
    private final List<Sucursal> sucursales = new ArrayList<>();
    private boolean esDesarrollador;
    private boolean esAdministrador;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRegistrarUsuarioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String rolSesion = SessionManager.getInstance().getRol();
        esDesarrollador = "Desarrollador".equalsIgnoreCase(rolSesion);
        esAdministrador = "Administrador".equalsIgnoreCase(rolSesion);

        if (!esDesarrollador && !esAdministrador) {
            Snackbar.make(binding.getRoot(), "No tienes permiso para registrar usuarios", Snackbar.LENGTH_LONG).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        viewModel = new ViewModelProvider(this).get(RegistrarUsuarioViewModel.class);

        binding.btnVolver.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );
        binding.btnRegistrar.setOnClickListener(v -> registrar());

        configurarPorRol();

        viewModel.getSucursales().observe(getViewLifecycleOwner(), lista -> {
            sucursales.clear();
            if (lista != null) {
                sucursales.addAll(lista);
            }

            List<String> nombres = new ArrayList<>();
            for (Sucursal sucursal : sucursales) {
                String texto = sucursal.getEmpresa() == null || sucursal.getEmpresa().trim().isEmpty()
                        ? sucursal.getNombre()
                        : sucursal.getEmpresa() + " · " + sucursal.getNombre();
                nombres.add(texto);
            }

            binding.actSucursal.setAdapter(new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    nombres
            ));

            if (esAdministrador && sucursales.size() == 1) {
                binding.actSucursal.setText(nombres.get(0), false);
            }
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

        viewModel.cargarSucursales(esDesarrollador);
    }

    private void configurarPorRol() {
        if (esDesarrollador) {
            binding.tvTitulo.setText("Registrar administrador");
            binding.tvDescripcion.setText("Elige la sucursal donde trabajará el nuevo administrador.");
            binding.actRol.setAdapter(new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    new String[]{"Administrador"}
            ));
            binding.actRol.setText("Administrador", false);
            binding.actRol.setEnabled(false);
            binding.tilRol.setHelperText("El desarrollador solo registra administradores");
            binding.tilSucursal.setHelperText("Selecciona una sucursal de cualquier empresa");
        } else {
            binding.tvTitulo.setText("Registrar usuario");
            binding.tvDescripcion.setText("Registra Técnicos o Solicitantes únicamente en tu sucursal.");
            binding.actRol.setAdapter(new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    Arrays.asList("Tecnico", "Solicitante")
            ));
            binding.tilRol.setHelperText("El administrador puede registrar Técnicos y Solicitantes");
            binding.actSucursal.setEnabled(false);
            binding.tilSucursal.setHelperText("Se usará automáticamente tu sucursal");
        }
    }

    private void registrar() {
        String nombre = texto(binding.etNombre);
        String correo = texto(binding.etCorreo);
        String contrasena = texto(binding.etContrasena);
        String rol = binding.actRol.getText().toString().trim();
        String sucursalTexto = binding.actSucursal.getText().toString().trim();

        binding.tilNombre.setError(null);
        binding.tilCorreo.setError(null);
        binding.tilContrasena.setError(null);
        binding.tilRol.setError(null);
        binding.tilSucursal.setError(null);

        if (nombre.isEmpty()) {
            binding.tilNombre.setError("El nombre es obligatorio");
            return;
        }
        if (correo.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilCorreo.setError("Ingresa un correo válido");
            return;
        }
        if (contrasena.length() < 6) {
            binding.tilContrasena.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }
        if (rol.isEmpty()) {
            binding.tilRol.setError("Selecciona un rol");
            return;
        }

        Integer idSucursal = null;
        if (esDesarrollador) {
            Sucursal sucursal = buscarSucursal(sucursalTexto);
            if (sucursal == null) {
                binding.tilSucursal.setError("Selecciona una sucursal");
                return;
            }
            idSucursal = sucursal.getId();
        }

        viewModel.registrarUsuario(
                nombre,
                correo,
                contrasena,
                rol,
                idSucursal
        );
    }

    private Sucursal buscarSucursal(String texto) {
        for (Sucursal sucursal : sucursales) {
            String etiqueta = sucursal.getEmpresa() == null || sucursal.getEmpresa().trim().isEmpty()
                    ? sucursal.getNombre()
                    : sucursal.getEmpresa() + " · " + sucursal.getNombre();
            if (etiqueta.equals(texto)) {
                return sucursal;
            }
        }
        return null;
    }

    private String texto(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void limpiarFormulario() {
        binding.etNombre.setText("");
        binding.etCorreo.setText("");
        binding.etContrasena.setText("");

        if (esAdministrador) {
            binding.actRol.setText("", false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
