package com.example.incidentapp.ui.incidentes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.incidentapp.R;
import com.example.incidentapp.adapter.IncidenteAdapter;
import com.example.incidentapp.databinding.FragmentListaIncidentesBinding;
import com.example.incidentapp.viewmodel.IncidentesViewModel;
import com.google.android.material.snackbar.Snackbar;

public class ListaIncidentesFragment extends Fragment {

    private FragmentListaIncidentesBinding binding;
    private IncidentesViewModel viewModel;
    private IncidenteAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListaIncidentesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(IncidentesViewModel.class);

        adapter = new IncidenteAdapter(incidente -> {
            Bundle args = new Bundle();
            args.putInt("id_incidente", incidente.getId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_listaIncidentesFragment_to_detalleIncidenteFragment, args);
        });

        binding.recyclerIncidentes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerIncidentes.setAdapter(adapter);

        String[] estados = {"Todos", "Abierto", "En proceso", "Resuelto", "Cerrado"};
        String[] prioridades = {"Todos", "Alta", "Media", "Baja"};

        binding.actEstado.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                estados
        ));
        binding.actPrioridad.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                prioridades
        ));

        binding.actEstado.setText("Todos", false);
        binding.actPrioridad.setText("Todos", false);

        binding.btnAplicarFiltros.setOnClickListener(v -> cargar());
        binding.btnReintentar.setOnClickListener(v -> cargar());
        binding.btnVolver.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        viewModel.getLoading().observe(getViewLifecycleOwner(), cargando ->
                binding.progressLista.setVisibility(Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE)
        );

        viewModel.getIncidentes().observe(getViewLifecycleOwner(), lista -> {
            adapter.setIncidentes(lista);
            boolean vacia = lista == null || lista.isEmpty();
            binding.layoutVacio.setVisibility(vacia ? View.VISIBLE : View.GONE);
            binding.recyclerIncidentes.setVisibility(vacia ? View.GONE : View.VISIBLE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), evento -> {
            String mensaje = evento.getContentIfNotHandled();
            if (mensaje != null) {
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
            }
        });

        cargar();
    }

    private void cargar() {
        viewModel.cargar(
                binding.actEstado.getText().toString(),
                binding.actPrioridad.getText().toString()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
