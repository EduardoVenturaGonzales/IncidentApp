package com.example.incidentapp.ui.incidentes;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.incidentapp.R;
import com.example.incidentapp.adapter.SolicitudAnulacionAdapter;
import com.example.incidentapp.databinding.FragmentSolicitudesAnulacionBinding;
import com.example.incidentapp.model.SolicitudAnulacion;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.SolicitudesAnulacionViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class SolicitudesAnulacionFragment extends Fragment {
    private FragmentSolicitudesAnulacionBinding binding;
    private SolicitudesAnulacionViewModel viewModel;
    private SolicitudAnulacionAdapter adapter;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = FragmentSolicitudesAnulacionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, Bundle state) {
        super.onViewCreated(view, state);
        if (!"Administrador".equalsIgnoreCase(SessionManager.getInstance().getRol())) {
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }
        viewModel = new ViewModelProvider(this).get(SolicitudesAnulacionViewModel.class);
        adapter = new SolicitudAnulacionAdapter(new SolicitudAnulacionAdapter.Listener() {
            @Override public void onAprobar(SolicitudAnulacion s) {
                responder(s, "aprobar");
            }
            @Override public void onRechazar(SolicitudAnulacion s) {
                responder(s, "rechazar");
            }
            @Override public void onAbrir(SolicitudAnulacion s) {
                abrirDetalle(s);
            }
        });
        binding.recyclerSolicitudes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerSolicitudes.setAdapter(adapter);
        binding.btnVolver.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        viewModel.getSolicitudes().observe(getViewLifecycleOwner(), lista -> {
            adapter.setItems(lista);
            binding.tvVacio.setVisibility(lista == null || lista.isEmpty() ? View.VISIBLE : View.GONE); }
        );
        viewModel.getLoading().observe(getViewLifecycleOwner(), l -> binding.progress.setVisibility(Boolean.TRUE.equals(l) ? View.VISIBLE : View.GONE));
        viewModel.getMensaje().observe(getViewLifecycleOwner(), e -> {
            String m=e.getContentIfNotHandled();
            if(m!=null) Snackbar.make(binding.getRoot(),m,Snackbar.LENGTH_LONG).show();
        });
        viewModel.cargar("Pendiente");
    }

    private void responder(SolicitudAnulacion s, String accion) {
        EditText comentario = new EditText(requireContext());
        comentario.setHint("Comentario opcional");
        comentario.setPadding(48,24,48,24);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("aprobar".equals(accion) ? "Aprobar anulación" : "Rechazar anulación")
                .setMessage("Incidente #" + s.getIdIncidente() + " - " + s.getTitulo())
                .setView(comentario)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (d,w) -> viewModel.responder(s.getIdIncidente(), accion, comentario.getText().toString().trim()))
                .show();
    }

    private void abrirDetalle(SolicitudAnulacion s) {
        Bundle args = new Bundle(); args.putInt("id_incidente", s.getIdIncidente());
        NavHostFragment.findNavController(this).navigate(R.id.action_solicitudesAnulacionFragment_to_detalleIncidenteFragment, args);
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding=null;
    }
}