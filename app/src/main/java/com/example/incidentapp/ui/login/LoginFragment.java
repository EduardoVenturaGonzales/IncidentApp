package com.example.incidentapp.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.incidentapp.R;
import com.example.incidentapp.databinding.FragmentLoginBinding;
import com.example.incidentapp.model.LoginResponse;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.LoginViewModel;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        if (SessionManager.getInstance().haySesion()) {
            irAlInicio();
            return;
        }

        binding.btnIngresar.setOnClickListener(v -> validarEIniciarSesion());

        viewModel.getLoading().observe(getViewLifecycleOwner(), cargando -> {
            boolean activo = Boolean.TRUE.equals(cargando);
            binding.progressLogin.setVisibility(activo ? View.VISIBLE : View.GONE);
            binding.btnIngresar.setEnabled(!activo);
            binding.etCorreo.setEnabled(!activo);
            binding.etContrasena.setEnabled(!activo);
        });

        viewModel.getLoginExitoso().observe(getViewLifecycleOwner(), evento -> {
            LoginResponse respuesta = evento.getContentIfNotHandled();
            if (respuesta == null) {
                return;
            }

            SessionManager.getInstance().guardarSesion(respuesta);
            irAlInicio();
        });

        viewModel.getError().observe(getViewLifecycleOwner(), evento -> {
            String mensaje = evento.getContentIfNotHandled();
            if (mensaje != null) {
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void validarEIniciarSesion() {
        String correo = binding.etCorreo.getText() == null
                ? ""
                : binding.etCorreo.getText().toString().trim();

        String contrasena = binding.etContrasena.getText() == null
                ? ""
                : binding.etContrasena.getText().toString().trim();

        binding.tilCorreo.setError(null);
        binding.tilContrasena.setError(null);

        if (correo.isEmpty()) {
            binding.tilCorreo.setError("Ingresa tu correo electrónico");
            binding.etCorreo.requestFocus();
            return;
        }

        if (contrasena.isEmpty()) {
            binding.tilContrasena.setError("Ingresa tu contraseña");
            binding.etContrasena.requestFocus();
            return;
        }

        viewModel.iniciarSesion(correo, contrasena);
    }

    private void irAlInicio() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_loginFragment_to_homeFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
