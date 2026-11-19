package com.example.incidentapp.ui.gestion;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.incidentapp.R;
import com.example.incidentapp.databinding.FragmentRegistrarActivoBinding;
import com.example.incidentapp.model.Activo;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.util.MultipartUtils;
import com.example.incidentapp.viewmodel.RegistrarActivoViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MultipartBody;

public class RegistrarActivoFragment extends Fragment {
    private FragmentRegistrarActivoBinding binding;
    private RegistrarActivoViewModel viewModel;
    private final List<Uri> fotos = new ArrayList<>();
    private Uri fotoPendienteUri;
    private final ActivityResultLauncher<String> permisoCamara = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedido -> {
                if (Boolean.TRUE.equals(concedido)) abrirCamara();
                else if (binding != null) Snackbar.make(binding.getRoot(), "Se necesita permiso de cámara", Snackbar.LENGTH_LONG).show();
            }
    );

    private final ActivityResultLauncher<String[]> selectorFotos = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris == null) return;
                for (Uri uri : uris) {
                    if (fotos.size() >= 2) break;
                    if (!fotos.contains(uri)) fotos.add(uri);
                }
                mostrarPrevisualizacion();
            }
    );

    private final ActivityResultLauncher<Uri> tomarFoto = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), ok -> {
                if (Boolean.TRUE.equals(ok) && fotoPendienteUri != null && fotos.size() < 2) fotos.add(fotoPendienteUri);
                fotoPendienteUri = null;
                mostrarPrevisualizacion();
            }
    );

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = FragmentRegistrarActivoBinding.inflate(inflater, container, false); return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, Bundle state) {
        super.onViewCreated(view, state);
        if (!"Administrador".equalsIgnoreCase(SessionManager.getInstance().getRol())) {
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }
        viewModel = new ViewModelProvider(this).get(RegistrarActivoViewModel.class);
        binding.actTipo.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line,
                new String[]{"PC", "Laptop", "Impresora", "Router", "Switch", "Servidor", "Otro"}));
        binding.actEstado.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line,
                new String[]{"Activo", "Inactivo", "Mantenimiento"}));
        binding.actEstado.setText("Activo", false);
        binding.btnVolver.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnSeleccionarFotos.setOnClickListener(v -> {
            if(fotos.size()<2) selectorFotos.launch(new String[]{"image/*"});
        });
        binding.btnTomarFoto.setOnClickListener(v -> prepararCamara());
        binding.btnRegistrar.setOnClickListener(v -> registrar());
        viewModel.getLoading().observe(getViewLifecycleOwner(), l -> {
            boolean b=Boolean.TRUE.equals(l);
            binding.progress.setVisibility(b?View.VISIBLE:View.GONE);
            binding.btnRegistrar.setEnabled(!b);
        });
        viewModel.getMensaje().observe(getViewLifecycleOwner(), e -> {
            String m=e.getContentIfNotHandled();
            if(m!=null) Snackbar.make(binding.getRoot(),m,Snackbar.LENGTH_LONG).show();
        });
        viewModel.getRegistrado().observe(getViewLifecycleOwner(), e -> {
            Activo a=e.getContentIfNotHandled();
            if(a!=null) mostrarQrYVolver(a);
        });
        mostrarPrevisualizacion();
    }

    private void prepararCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) abrirCamara();
        else permisoCamara.launch(Manifest.permission.CAMERA);
    }

    private void abrirCamara() {
        if (fotos.size() >= 2) {
            Snackbar.make(binding.getRoot(), "Máximo 2 fotos", Snackbar.LENGTH_SHORT).show();
            return;
        }
        try {
            File carpeta = new File(requireContext().getCacheDir(), "imagenes");
            if (!carpeta.exists()) carpeta.mkdirs();
            File archivo = File.createTempFile("activo_", ".jpg", carpeta);
            fotoPendienteUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName()+".fileprovider", archivo);
            tomarFoto.launch(fotoPendienteUri);
        } catch (IOException ex) {
            Snackbar.make(binding.getRoot(), "No se pudo abrir la cámara", Snackbar.LENGTH_LONG).show();
        }
    }

    private void registrar() {
        String nombre = binding.etNombre.getText()==null?"":binding.etNombre.getText().toString().trim();
        String tipo = binding.actTipo.getText().toString().trim();
        String estado = binding.actEstado.getText().toString().trim();
        binding.tilNombre.setError(null);
        binding.tilTipo.setError(null);
        binding.tilEstado.setError(null);
        if(nombre.isEmpty()){
            binding.tilNombre.setError("El nombre es obligatorio");
            return;
        }
        if(tipo.isEmpty()){
            binding.tilTipo.setError("Selecciona un tipo");
            return;
        }
        if(estado.isEmpty()){
            binding.tilEstado.setError("Selecciona un estado");
            return;
        }
        List<MultipartBody.Part> partes = new ArrayList<>();
        try {
            for(Uri uri:fotos) partes.add(MultipartUtils.crearParteFoto(requireContext(), uri));
        }
        catch(IOException e){
            Snackbar.make(binding.getRoot(),"No se pudo leer una imagen",Snackbar.LENGTH_LONG).show();
            return;
        }
        viewModel.registrar(nombre,tipo,estado,partes);
    }

    private void mostrarQrYVolver(Activo activo) {
        try {
            Bitmap bitmap = new BarcodeEncoder().encodeBitmap(activo.getCodigoQr(), BarcodeFormat.QR_CODE, 700, 700);
            ImageView image = new ImageView(requireContext()); image.setImageBitmap(bitmap); image.setPadding(24,24,24,24); image.setAdjustViewBounds(true);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Activo registrado: " + activo.getCodigoQr())
                    .setMessage("QR generado correctamente. Imprime este código y colócalo en el equipo.")
                    .setView(image)
                    .setNeutralButton("Compartir QR", (d,w) -> compartirQr(bitmap, activo.getCodigoQr()))
                    .setPositiveButton("Listo", (d,w) -> NavHostFragment.findNavController(this).popBackStack())
                    .setCancelable(false).show();
        } catch(Exception e) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    private void compartirQr(Bitmap bitmap, String codigo) {
        try {
            File carpeta = new File(requireContext().getCacheDir(), "imagenes"); if (!carpeta.exists()) carpeta.mkdirs();
            File archivo = new File(carpeta, "QR_" + codigo + ".png");
            try (FileOutputStream out = new FileOutputStream(archivo)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName()+".fileprovider", archivo);
            Intent intent = new Intent(Intent.ACTION_SEND); intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Compartir QR del activo"));
        } catch (Exception e) {
            if (binding != null) Snackbar.make(binding.getRoot(), "No se pudo compartir el QR", Snackbar.LENGTH_LONG).show();
        }
    }

    private void mostrarPrevisualizacion() {
        if(binding==null) return;
        binding.contenedorPreviews.removeAllViews();
        binding.tvCantidadFotos.setText(fotos.size()+" / 2 fotos seleccionadas");
        boolean puede=fotos.size()<2;
        binding.btnTomarFoto.setEnabled(puede); binding.btnSeleccionarFotos.setEnabled(puede);
        binding.btnTomarFoto.setAlpha(puede?1f:.55f); binding.btnSeleccionarFotos.setAlpha(puede?1f:.55f);
        for(Uri uri:new ArrayList<>(fotos)) {
            View preview=LayoutInflater.from(requireContext()).inflate(R.layout.item_foto_preview,binding.contenedorPreviews,false);
            ImageView image=preview.findViewById(R.id.ivFotoPreview);
            View btn=preview.findViewById(R.id.btnEliminarFoto);
            image.setImageURI(uri); btn.setOnClickListener(v->{
                fotos.remove(uri);mostrarPrevisualizacion();
            });
            binding.contenedorPreviews.addView(preview);
        }
    }

    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}