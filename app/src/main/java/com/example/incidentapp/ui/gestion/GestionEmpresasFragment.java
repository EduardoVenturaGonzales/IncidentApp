package com.example.incidentapp.ui.gestion;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.incidentapp.adapter.EmpresaGestionAdapter;
import com.example.incidentapp.databinding.FragmentGestionEmpresasBinding;
import com.example.incidentapp.model.Empresa;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.GestionEmpresasViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class GestionEmpresasFragment extends Fragment {
    private FragmentGestionEmpresasBinding binding;
    private GestionEmpresasViewModel viewModel;
    private EmpresaGestionAdapter adapter;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = FragmentGestionEmpresasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, Bundle state) {
        super.onViewCreated(view, state);
        if (!"Desarrollador".equalsIgnoreCase(SessionManager.getInstance().getRol())) {
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }
        viewModel = new ViewModelProvider(this).get(GestionEmpresasViewModel.class);
        adapter = new EmpresaGestionAdapter(this::confirmarEliminar);
        binding.recyclerEmpresas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerEmpresas.setAdapter(adapter);
        binding.recyclerEmpresas.setNestedScrollingEnabled(false);

        binding.btnVolver.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnRegistrar.setOnClickListener(v -> registrar());

        viewModel.getEmpresas().observe(getViewLifecycleOwner(), adapter::setEmpresas);
        viewModel.getLoading().observe(getViewLifecycleOwner(), l -> binding.progress.setVisibility(Boolean.TRUE.equals(l) ? View.VISIBLE : View.GONE));
        viewModel.getMensaje().observe(getViewLifecycleOwner(), e -> {
            String m = e.getContentIfNotHandled();
            if (m != null) Snackbar.make(binding.getRoot(), m, Snackbar.LENGTH_LONG).show();
        });
        viewModel.cargar();
    }

    private void registrar() {
        String nombre = binding.etNombre.getText() == null ? "" : binding.etNombre.getText().toString().trim();
        String ruc = binding.etRuc.getText() == null ? "" : binding.etRuc.getText().toString().trim();
        binding.tilNombre.setError(null); binding.tilRuc.setError(null);
        if (nombre.isEmpty()) { binding.tilNombre.setError("El nombre es obligatorio"); return; }
        if (!ruc.isEmpty() && ruc.length() != 11) {
            binding.tilRuc.setError("El RUC debe tener 11 dígitos"); return;
        }
        viewModel.registrar(nombre, ruc);
        binding.etNombre.setText(""); binding.etRuc.setText("");
    }

    private void confirmarEliminar(Empresa empresa) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar empresa")
                .setMessage("Se desactivará la empresa \"" + empresa.getNombre() + "\", sus sucursales y usuarios. Los datos históricos se conservarán.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (d, w) -> viewModel.eliminar(empresa.getId()))
                .show();
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}