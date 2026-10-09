package com.example.incidentapp.ui.incidentes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.incidentapp.R;
import com.example.incidentapp.adapter.HistorialAdapter;
import com.example.incidentapp.databinding.DialogCambiarEstadoBinding;
import com.example.incidentapp.databinding.FragmentDetalleIncidenteBinding;
import com.example.incidentapp.model.DetalleIncidenteResponse;
import com.example.incidentapp.model.FotoIncidente;
import com.example.incidentapp.model.Incidente;
import com.example.incidentapp.model.Tecnico;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.DetalleIncidenteViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class DetalleIncidenteFragment extends Fragment {

    private FragmentDetalleIncidenteBinding binding;
    private DetalleIncidenteViewModel viewModel;
    private HistorialAdapter historialAdapter;
    private int idIncidente;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetalleIncidenteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idIncidente = getArguments() == null ? 0 : getArguments().getInt("id_incidente", 0);
        viewModel = new ViewModelProvider(this).get(DetalleIncidenteViewModel.class);

        historialAdapter = new HistorialAdapter();
        binding.recyclerHistorial.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerHistorial.setAdapter(historialAdapter);
        binding.recyclerHistorial.setNestedScrollingEnabled(false);

        binding.btnVolver.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnCambiarEstado.setOnClickListener(v -> mostrarDialogoEstado());
        binding.btnAsignarTecnico.setOnClickListener(v -> viewModel.cargarTecnicos());

        String rol = SessionManager.getInstance().getRol();
        binding.btnCambiarEstado.setVisibility(
                "Tecnico".equalsIgnoreCase(rol) || "Administrador".equalsIgnoreCase(rol)
                        ? View.VISIBLE : View.GONE
        );
        binding.btnAsignarTecnico.setVisibility(
                "Administrador".equalsIgnoreCase(rol) ? View.VISIBLE : View.GONE
        );

        viewModel.getLoading().observe(getViewLifecycleOwner(), cargando ->
                binding.progressDetalle.setVisibility(Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE)
        );

        viewModel.getDetalle().observe(getViewLifecycleOwner(), this::mostrarDetalle);

        viewModel.getMensaje().observe(getViewLifecycleOwner(), evento -> {
            String mensaje = evento.getContentIfNotHandled();
            if (mensaje != null) {
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getTecnicos().observe(getViewLifecycleOwner(), evento -> {
            List<Tecnico> tecnicos = evento.getContentIfNotHandled();
            if (tecnicos != null) {
                mostrarDialogoTecnicos(tecnicos);
            }
        });

        if (idIncidente > 0) {
            viewModel.cargarDetalle(idIncidente);
        }
    }

    private void mostrarDetalle(DetalleIncidenteResponse detalle) {
        if (detalle == null || detalle.getIncidente() == null) {
            return;
        }

        Incidente i = detalle.getIncidente();
        binding.tvTicket.setText("Incidente #" + i.getId());
        binding.tvTitulo.setText(i.getTitulo());
        binding.tvDescripcion.setText(i.getDescripcion());
        binding.tvEstado.setText(i.getEstado());
        binding.tvPrioridad.setText(i.getPrioridad());
        binding.tvCategoria.setText(i.getCategoria());
        binding.tvSucursal.setText(valor(i.getSucursal()));
        binding.tvDireccion.setText(valor(i.getDireccionSucursal()));
        binding.tvActivo.setText(i.getActivo() == null ? "Sin activo asociado" : i.getActivo());
        binding.tvCodigoQr.setText(i.getCodigoQr() == null ? "-" : i.getCodigoQr());
        binding.tvSolicitante.setText(valor(i.getSolicitante()));
        binding.tvTecnico.setText(i.getTecnico() == null ? "Sin técnico asignado" : i.getTecnico());
        binding.tvFecha.setText(valor(i.getFechaRegistro()));

        historialAdapter.setHistorial(detalle.getHistorial());
        binding.tvSinHistorial.setVisibility(
                detalle.getHistorial() == null || detalle.getHistorial().isEmpty()
                        ? View.VISIBLE : View.GONE
        );

        mostrarFotos(detalle.getFotos());
    }

    private String valor(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private void mostrarFotos(List<FotoIncidente> fotos) {
        binding.contenedorFotos.removeAllViews();

        if (fotos == null || fotos.isEmpty()) {
            binding.tvSinFotos.setVisibility(View.VISIBLE);
            return;
        }

        binding.tvSinFotos.setVisibility(View.GONE);

        for (FotoIncidente foto : fotos) {
            ImageView imagen = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(150), dp(115));
            params.setMarginEnd(dp(12));
            imagen.setLayoutParams(params);
            imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imagen.setBackgroundResource(R.drawable.bg_image_preview);
            imagen.setClipToOutline(true);

            GlideUrl glideUrl = crearUrlConJwt(foto.getUrl());
            Glide.with(this)
                    .load(glideUrl)
                    .centerCrop()
                    .into(imagen);

            imagen.setOnClickListener(v -> mostrarFotoGrande(glideUrl));
            binding.contenedorFotos.addView(imagen);
        }
    }

    private GlideUrl crearUrlConJwt(String url) {
        return new GlideUrl(
                url,
                new LazyHeaders.Builder()
                        .addHeader("Authorization", "JWT " + SessionManager.getInstance().getToken())
                        .build()
        );
    }

    private void mostrarFotoGrande(GlideUrl url) {
        ImageView imageView = new ImageView(requireContext());
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(dp(8), dp(8), dp(8), dp(8));

        Glide.with(this).load(url).fitCenter().into(imageView);

        new MaterialAlertDialogBuilder(requireContext())
                .setView(imageView)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void mostrarDialogoEstado() {
        DialogCambiarEstadoBinding dialogBinding = DialogCambiarEstadoBinding.inflate(getLayoutInflater());
        String rol = SessionManager.getInstance().getRol();

        String[] estados = "Administrador".equalsIgnoreCase(rol)
                ? new String[]{"Abierto", "En proceso", "Resuelto", "Cerrado"}
                : new String[]{"En proceso", "Resuelto"};

        dialogBinding.actEstado.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                estados
        ));
        dialogBinding.actEstado.setText(estados[0], false);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambiar estado")
                .setView(dialogBinding.getRoot())
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String estado = dialogBinding.actEstado.getText().toString().trim();
                    String comentario = dialogBinding.etComentario.getText() == null
                            ? ""
                            : dialogBinding.etComentario.getText().toString().trim();

                    if (comentario.isEmpty()) {
                        dialogBinding.tilComentario.setError("El comentario es obligatorio");
                        return;
                    }

                    viewModel.cambiarEstado(idIncidente, estado, comentario);
                    dialog.dismiss();
                }));

        dialog.show();
    }

    private void mostrarDialogoTecnicos(List<Tecnico> tecnicos) {
        if (tecnicos.isEmpty()) {
            Snackbar.make(binding.getRoot(), "No hay técnicos disponibles", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String[] nombres = new String[tecnicos.size()];
        for (int i = 0; i < tecnicos.size(); i++) {
            nombres[i] = tecnicos.get(i).getNombre();
        }

        final int[] seleccionado = {0};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Asignar técnico")
                .setSingleChoiceItems(nombres, 0, (dialog, which) -> seleccionado[0] = which)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Asignar", (dialog, which) -> {
                    Tecnico tecnico = tecnicos.get(seleccionado[0]);
                    viewModel.asignarTecnico(idIncidente, tecnico.getId());
                })
                .show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
