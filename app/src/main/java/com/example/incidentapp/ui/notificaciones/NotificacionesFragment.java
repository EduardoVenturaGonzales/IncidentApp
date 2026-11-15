package com.example.incidentapp.ui.notificaciones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.incidentapp.R;
import com.example.incidentapp.adapter.NotificacionAdapter;
import com.example.incidentapp.databinding.FragmentNotificacionesBinding;
import com.example.incidentapp.model.Notificacion;
import com.example.incidentapp.viewmodel.NotificacionesViewModel;
import com.google.android.material.snackbar.Snackbar;

public class NotificacionesFragment extends Fragment {
    private FragmentNotificacionesBinding binding;
    private NotificacionesViewModel viewModel;
    private NotificacionAdapter adapter;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = FragmentNotificacionesBinding.inflate(inflater, container, false); return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, Bundle state) {
        super.onViewCreated(view, state);
        viewModel = new ViewModelProvider(this).get(NotificacionesViewModel.class);
        adapter = new NotificacionAdapter(this::abrirNotificacion);
        binding.recyclerNotificaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerNotificaciones.setAdapter(adapter);
        binding.btnVolver.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnLeerTodas.setOnClickListener(v -> viewModel.marcarTodas());
        viewModel.getNotificaciones().observe(getViewLifecycleOwner(), lista -> {
            adapter.setItems(lista); binding.tvVacio.setVisibility(lista == null || lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), l -> binding.progress.setVisibility(Boolean.TRUE.equals(l) ? View.VISIBLE : View.GONE));
        viewModel.getMensaje().observe(getViewLifecycleOwner(), e -> { String m=e.getContentIfNotHandled(); if(m!=null) Snackbar.make(binding.getRoot(),m,Snackbar.LENGTH_LONG).show(); });
        viewModel.cargar();
    }

    private void abrirNotificacion(Notificacion n) {
        if (!n.isLeida()) viewModel.marcarLeida(n.getId());
        if ("SOLICITUD_ANULACION".equalsIgnoreCase(n.getTipo())) {
            NavHostFragment.findNavController(this).navigate(R.id.action_notificacionesFragment_to_solicitudesAnulacionFragment);
            return;
        }
        if (n.getIdIncidente() != null && n.getIdIncidente() > 0) {
            Bundle args = new Bundle(); args.putInt("id_incidente", n.getIdIncidente());
            NavHostFragment.findNavController(this).navigate(R.id.action_notificacionesFragment_to_detalleIncidenteFragment, args);
        }
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding=null; }
}
