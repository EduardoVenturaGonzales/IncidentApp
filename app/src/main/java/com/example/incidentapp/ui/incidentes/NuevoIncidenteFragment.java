package com.example.incidentapp.ui.incidentes;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.incidentapp.R;
import com.example.incidentapp.databinding.FragmentNuevoIncidenteBinding;
import com.example.incidentapp.model.Activo;
import com.example.incidentapp.model.Sucursal;
import com.example.incidentapp.util.MultipartUtils;
import com.example.incidentapp.viewmodel.NuevoIncidenteViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;

public class NuevoIncidenteFragment extends Fragment {

    private FragmentNuevoIncidenteBinding binding;
    private NuevoIncidenteViewModel viewModel;
    private final List<Sucursal> sucursales = new ArrayList<>();
    private final List<Uri> fotos = new ArrayList<>();
    private Integer idActivoSeleccionado = null;

    private final ActivityResultLauncher<String[]> selectorFotos = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(),
            uris -> {
                fotos.clear();
                if (uris != null) {
                    for (int i = 0; i < uris.size() && i < 2; i++) {
                        fotos.add(uris.get(i));
                    }
                    if (uris.size() > 2) {
                        Snackbar.make(requireView(), "Solo se permiten hasta 2 fotos", Snackbar.LENGTH_SHORT).show();
                    }
                }
                mostrarPrevisualizacion();
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNuevoIncidenteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NuevoIncidenteViewModel.class);

        binding.actCategoria.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Red", "Hardware", "Software", "Acceso", "Impresora", "Otro"}
        ));

        binding.actPrioridad.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Alta", "Media", "Baja"}
        ));

        binding.btnVolver.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnSeleccionarFotos.setOnClickListener(v -> selectorFotos.launch(new String[]{"image/*"}));
        binding.btnBuscarActivo.setOnClickListener(v -> viewModel.buscarActivo(
                binding.etCodigoQr.getText() == null ? "" : binding.etCodigoQr.getText().toString()
        ));
        binding.btnRegistrar.setOnClickListener(v -> registrar());

        viewModel.getSucursales().observe(getViewLifecycleOwner(), lista -> {
            sucursales.clear();
            if (lista != null) {
                sucursales.addAll(lista);
            }

            List<String> nombres = new ArrayList<>();
            for (Sucursal sucursal : sucursales) {
                nombres.add(sucursal.getNombre());
            }

            binding.actSucursal.setAdapter(new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    nombres
            ));
        });

        viewModel.getActivo().observe(getViewLifecycleOwner(), activo -> {
            if (activo == null) {
                idActivoSeleccionado = null;
                binding.tvActivoSeleccionado.setText("Sin activo seleccionado");
            } else {
                idActivoSeleccionado = activo.getId();
                binding.tvActivoSeleccionado.setText(
                        activo.getNombre() + " · " + activo.getCodigoQr() + " · " + activo.getSucursal()
                );
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), cargando -> {
            boolean activo = Boolean.TRUE.equals(cargando);
            binding.progressNuevo.setVisibility(activo ? View.VISIBLE : View.GONE);
            binding.btnRegistrar.setEnabled(!activo);
        });

        viewModel.getMensaje().observe(getViewLifecycleOwner(), evento -> {
            String mensaje = evento.getContentIfNotHandled();
            if (mensaje != null) {
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getRegistrado().observe(getViewLifecycleOwner(), evento -> {
            Integer id = evento.getContentIfNotHandled();
            if (id != null) {
                Bundle args = new Bundle();
                args.putInt("id_incidente", id);
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_nuevoIncidenteFragment_to_detalleIncidenteFragment, args);
            }
        });

        viewModel.cargarSucursales();
    }

    private void registrar() {
        String titulo = texto(binding.etTitulo);
        String descripcion = texto(binding.etDescripcion);
        String categoria = binding.actCategoria.getText().toString().trim();
        String prioridad = binding.actPrioridad.getText().toString().trim();
        String sucursalNombre = binding.actSucursal.getText().toString().trim();

        binding.tilTitulo.setError(null);
        binding.tilDescripcion.setError(null);
        binding.tilCategoria.setError(null);
        binding.tilPrioridad.setError(null);
        binding.tilSucursal.setError(null);

        if (titulo.isEmpty()) {
            binding.tilTitulo.setError("El título es obligatorio");
            return;
        }
        if (descripcion.isEmpty()) {
            binding.tilDescripcion.setError("La descripción es obligatoria");
            return;
        }
        if (categoria.isEmpty()) {
            binding.tilCategoria.setError("Selecciona una categoría");
            return;
        }
        if (prioridad.isEmpty()) {
            binding.tilPrioridad.setError("Selecciona una prioridad");
            return;
        }

        Sucursal sucursal = buscarSucursal(sucursalNombre);
        if (sucursal == null) {
            binding.tilSucursal.setError("Selecciona una sucursal");
            return;
        }

        List<MultipartBody.Part> partes = new ArrayList<>();
        try {
            for (Uri uri : fotos) {
                partes.add(MultipartUtils.crearParteFoto(requireContext(), uri));
            }
        } catch (IOException e) {
            Snackbar.make(binding.getRoot(), "No se pudo leer una de las imágenes", Snackbar.LENGTH_LONG).show();
            return;
        }

        viewModel.registrar(
                titulo,
                descripcion,
                categoria,
                prioridad,
                sucursal.getId(),
                idActivoSeleccionado,
                partes
        );
    }

    private String texto(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private Sucursal buscarSucursal(String nombre) {
        for (Sucursal s : sucursales) {
            if (s.getNombre().equals(nombre)) {
                return s;
            }
        }
        return null;
    }

    private void mostrarPrevisualizacion() {
        binding.contenedorPreviews.removeAllViews();
        binding.tvCantidadFotos.setText(fotos.size() + " / 2 fotos seleccionadas");

        for (Uri uri : fotos) {
            ImageView image = new ImageView(requireContext());
            int size = Math.round(100 * getResources().getDisplayMetrics().density);
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(size, size);
            params.setMarginEnd(Math.round(10 * getResources().getDisplayMetrics().density));
            image.setLayoutParams(params);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setImageURI(uri);
            image.setBackgroundResource(R.drawable.bg_image_preview);
            image.setClipToOutline(true);
            binding.contenedorPreviews.addView(image);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
