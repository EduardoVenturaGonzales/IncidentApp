package com.example.incidentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.incidentapp.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

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

        binding.btnIngresar.setOnClickListener(v -> iniciarSesion());
    }

    private void iniciarSesion() {
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

        /*
         * Por ahora hacemos solo la validación local de campos vacíos.
         * Cuando se conecte la API, aquí irá POST /auth/login con Retrofit.
         * Si la API responde correctamente, se guardará el JWT y recién
         * se navegará a la pantalla principal según el rol del usuario.
         */
        Bundle datos = new Bundle();
        datos.putString("correo", correo);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_loginFragment_to_homeFragment, datos);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
