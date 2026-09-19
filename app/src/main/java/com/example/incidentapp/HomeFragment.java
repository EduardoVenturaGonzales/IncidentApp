package com.example.incidentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.incidentapp.databinding.FragmentHomeBinding;
import com.google.android.material.snackbar.Snackbar;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

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

        String correo = "Usuario";
        if (getArguments() != null) {
            correo = getArguments().getString("correo", "Usuario");
        }

        binding.tvUsuario.setText(correo);

        binding.btnVerIncidentes.setOnClickListener(v ->
                Snackbar.make(v, "El listado de incidentes será el siguiente módulo", Snackbar.LENGTH_SHORT).show()
        );

        binding.btnNuevoIncidente.setOnClickListener(v ->
                Snackbar.make(v, "El registro de incidentes se implementará después", Snackbar.LENGTH_SHORT).show()
        );

        binding.btnCerrarSesion.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_homeFragment_to_loginFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
