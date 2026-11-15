package com.example.incidentapp.ui.gestion;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.incidentapp.R;
import com.example.incidentapp.adapter.ActivoAdapter;
import com.example.incidentapp.databinding.FragmentGestionActivosBinding;
import com.example.incidentapp.model.Activo;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.GestionActivosViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;

public class GestionActivosFragment extends Fragment {
    private FragmentGestionActivosBinding binding;
    private GestionActivosViewModel viewModel;
    private ActivoAdapter adapter;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = FragmentGestionActivosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, Bundle state) {
        super.onViewCreated(view, state);
        if (!"Administrador".equalsIgnoreCase(SessionManager.getInstance().getRol())) {
            NavHostFragment.findNavController(this).popBackStack(); return;
        }
        viewModel = new ViewModelProvider(this).get(GestionActivosViewModel.class);
        adapter = new ActivoAdapter(new ActivoAdapter.Listener() {
            @Override public void onVerQr(Activo activo) { mostrarQr(activo); }
            @Override public void onEliminar(Activo activo) { confirmarEliminar(activo); }
        });
        binding.recyclerActivos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerActivos.setAdapter(adapter);
        binding.btnVolver.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnNuevoActivo.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_gestionActivosFragment_to_registrarActivoFragment));

        viewModel.getActivos().observe(getViewLifecycleOwner(), lista -> {
            adapter.setActivos(lista);
            binding.tvVacio.setVisibility(lista == null || lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), l -> binding.progress.setVisibility(Boolean.TRUE.equals(l) ? View.VISIBLE : View.GONE));
        viewModel.getMensaje().observe(getViewLifecycleOwner(), e -> { String m=e.getContentIfNotHandled(); if(m!=null) Snackbar.make(binding.getRoot(),m,Snackbar.LENGTH_LONG).show(); });
        viewModel.cargar();
    }

    @Override public void onResume() { super.onResume(); if (viewModel != null) viewModel.cargar(); }

    private void mostrarQr(Activo activo) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(activo.getCodigoQr(), BarcodeFormat.QR_CODE, 700, 700);
            ImageView image = new ImageView(requireContext());
            image.setImageBitmap(bitmap); image.setPadding(24,24,24,24); image.setAdjustViewBounds(true);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(activo.getCodigoQr() + " · " + activo.getNombre())
                    .setMessage("Este QR contiene únicamente el código " + activo.getCodigoQr() + ". Puedes imprimirlo y pegarlo al equipo.")
                    .setView(image)
                    .setNeutralButton("Compartir QR", (d,w) -> compartirQr(bitmap, activo.getCodigoQr()))
                    .setPositiveButton("Cerrar", null).show();
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), "No se pudo generar el QR", Snackbar.LENGTH_LONG).show();
        }
    }

    private void compartirQr(Bitmap bitmap, String codigo) {
        try {
            File carpeta = new File(requireContext().getCacheDir(), "imagenes");
            if (!carpeta.exists()) carpeta.mkdirs();
            File archivo = new File(carpeta, "QR_" + codigo + ".png");
            try (FileOutputStream out = new FileOutputStream(archivo)) { bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); }
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", archivo);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png"); intent.putExtra(Intent.EXTRA_STREAM, uri); intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Compartir QR del activo"));
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), "No se pudo compartir el QR", Snackbar.LENGTH_LONG).show();
        }
    }

    private void confirmarEliminar(Activo activo) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar activo TI")
                .setMessage("¿Eliminar " + activo.getCodigoQr() + " - " + activo.getNombre() + "? Si tiene incidentes históricos se conservará como Baja.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (d,w) -> viewModel.eliminar(activo.getId()))
                .show();
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding=null; }
}
