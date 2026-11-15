package com.example.incidentapp.ui.incidentes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
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
import com.example.incidentapp.model.SolicitudAnulacion;
import com.example.incidentapp.model.Tecnico;
import com.example.incidentapp.model.VerificarActivoResponse;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.DetalleIncidenteViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.List;

public class DetalleIncidenteFragment extends Fragment {

    private FragmentDetalleIncidenteBinding binding;
    private DetalleIncidenteViewModel viewModel;
    private HistorialAdapter historialAdapter;
    private int idIncidente;
    private Incidente incidenteActual;
    private String rol;

    private final ActivityResultLauncher<ScanOptions> escanerQr = registerForActivityResult(
            new ScanContract(), resultado -> {
                if (resultado.getContents() != null && viewModel != null) {
                    viewModel.verificarActivo(idIncidente, resultado.getContents().trim());
                }
            }
    );

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = FragmentDetalleIncidenteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, Bundle state) {
        super.onViewCreated(view, state);
        idIncidente = getArguments() == null ? 0 : getArguments().getInt("id_incidente", 0);
        rol = SessionManager.getInstance().getRol();
        viewModel = new ViewModelProvider(this).get(DetalleIncidenteViewModel.class);

        historialAdapter = new HistorialAdapter();
        binding.recyclerHistorial.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerHistorial.setAdapter(historialAdapter);
        binding.recyclerHistorial.setNestedScrollingEnabled(false);

        binding.btnVolver.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnCambiarEstado.setOnClickListener(v -> mostrarDialogoEstado());
        binding.btnAsignarTecnico.setOnClickListener(v -> viewModel.cargarTecnicos());
        binding.btnComoLlegar.setOnClickListener(v -> abrirComoLlegar());
        binding.btnVerificarActivo.setOnClickListener(v -> abrirEscanerVerificacion());
        binding.btnSolicitarAnulacion.setOnClickListener(v -> mostrarDialogoAnulacion());
        binding.btnEliminarIncidente.setOnClickListener(v -> mostrarDialogoEliminar());

        viewModel.getLoading().observe(getViewLifecycleOwner(), l -> binding.progressDetalle.setVisibility(Boolean.TRUE.equals(l) ? View.VISIBLE : View.GONE));
        viewModel.getDetalle().observe(getViewLifecycleOwner(), this::mostrarDetalle);
        viewModel.getMensaje().observe(getViewLifecycleOwner(), e -> {
            String m = e.getContentIfNotHandled();
            if (m != null && binding != null) Snackbar.make(binding.getRoot(), m, Snackbar.LENGTH_LONG).show();
        });
        viewModel.getTecnicos().observe(getViewLifecycleOwner(), e -> {
            List<Tecnico> lista = e.getContentIfNotHandled();
            if (lista != null) mostrarDialogoTecnicos(lista);
        });
        viewModel.getVerificacion().observe(getViewLifecycleOwner(), e -> {
            VerificarActivoResponse r = e.getContentIfNotHandled();
            if (r != null) Snackbar.make(binding.getRoot(), r.getMensaje(), Snackbar.LENGTH_LONG).show();
        });
        viewModel.getEliminado().observe(getViewLifecycleOwner(), e -> {
            Boolean ok = e.getContentIfNotHandled();
            if (Boolean.TRUE.equals(ok) && binding != null) {
                Snackbar sb = Snackbar.make(binding.getRoot(), "Incidente eliminado correctamente", Snackbar.LENGTH_SHORT);
                sb.addCallback(new Snackbar.Callback() {
                    @Override public void onDismissed(Snackbar bar, int event) {
                        if (isAdded()) requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
                sb.show();
            }
        });

        if (idIncidente > 0) viewModel.cargarDetalle(idIncidente);
    }

    private void mostrarDetalle(DetalleIncidenteResponse detalle) {
        if (detalle == null || detalle.getIncidente() == null) return;
        Incidente i = detalle.getIncidente(); incidenteActual = i;
        binding.tvTicket.setText("Incidente #" + i.getId());
        binding.tvTitulo.setText(i.getTitulo()); binding.tvDescripcion.setText(i.getDescripcion());
        binding.tvEstado.setText(i.getEstado()); binding.tvPrioridad.setText(i.getPrioridad()); binding.tvCategoria.setText(i.getCategoria());
        binding.tvSucursal.setText(valor(i.getSucursal())); binding.tvDireccion.setText(valor(i.getDireccionSucursal()));
        binding.tvActivo.setText(i.getActivo() == null ? "Sin activo asociado" : i.getActivo());
        binding.tvCodigoQr.setText(i.getCodigoQr() == null ? "-" : i.getCodigoQr());
        binding.tvSolicitante.setText(valor(i.getSolicitante())); binding.tvTecnico.setText(i.getTecnico() == null ? "Sin técnico asignado" : i.getTecnico());
        binding.tvFecha.setText(valor(i.getFechaRegistro()));
        historialAdapter.setHistorial(detalle.getHistorial());
        binding.tvSinHistorial.setVisibility(detalle.getHistorial() == null || detalle.getHistorial().isEmpty() ? View.VISIBLE : View.GONE);
        mostrarFotos(detalle.getFotos());
        configurarAcciones(detalle);
    }

    private void configurarAcciones(DetalleIncidenteResponse detalle) {
        boolean admin = "Administrador".equalsIgnoreCase(rol);
        boolean tecnico = "Tecnico".equalsIgnoreCase(rol);
        boolean solicitante = "Solicitante".equalsIgnoreCase(rol);
        boolean anulado = incidenteActual != null && "Anulado".equalsIgnoreCase(incidenteActual.getEstado());

        binding.btnCambiarEstado.setVisibility((admin || tecnico) && !anulado ? View.VISIBLE : View.GONE);
        binding.btnAsignarTecnico.setVisibility(admin && !anulado ? View.VISIBLE : View.GONE);
        binding.btnComoLlegar.setVisibility(tecnico && !anulado ? View.VISIBLE : View.GONE);
        binding.btnVerificarActivo.setVisibility(tecnico && !anulado && incidenteActual.getIdActivo() != null ? View.VISIBLE : View.GONE);
        binding.btnEliminarIncidente.setVisibility(admin ? View.VISIBLE : View.GONE);

        SolicitudAnulacion solicitud = detalle.getSolicitudAnulacion();
        boolean pendiente = solicitud != null && "Pendiente".equalsIgnoreCase(solicitud.getEstado());
        binding.btnSolicitarAnulacion.setVisibility(solicitante && !anulado && !pendiente ? View.VISIBLE : View.GONE);
        if (solicitante && solicitud != null) {
            binding.tvSolicitudAnulacion.setVisibility(View.VISIBLE);
            binding.tvSolicitudAnulacion.setText("Solicitud de anulación: " + solicitud.getEstado());
        } else {
            binding.tvSolicitudAnulacion.setVisibility(View.GONE);
        }
    }

    private void abrirEscanerVerificacion() {
        ScanOptions o = new ScanOptions();
        o.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        o.setPrompt("Escanea el QR del equipo que estás atendiendo");
        o.setBeepEnabled(true); o.setOrientationLocked(false);
        escanerQr.launch(o);
    }

    private void mostrarDialogoAnulacion() {
        EditText motivo = new EditText(requireContext()); motivo.setHint("Ej. Lo reporté por error o el problema se solucionó solo"); motivo.setMinLines(3); motivo.setPadding(48,24,48,24);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Solicitar anulación")
                .setMessage("Explica por qué deseas anular este incidente.")
                .setView(motivo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Solicitar", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String texto = motivo.getText().toString().trim();
            if (texto.isEmpty()) { motivo.setError("El motivo es obligatorio"); return; }
            viewModel.solicitarAnulacion(idIncidente, texto); dialog.dismiss();
        }));
        dialog.show();
    }

    private void mostrarDialogoEliminar() {
        EditText motivo = new EditText(requireContext()); motivo.setHint("Motivo de eliminación (opcional)"); motivo.setPadding(48,24,48,24);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar incidente")
                .setMessage("Esta acción eliminará definitivamente el incidente, su historial y sus fotografías. Se guardará una auditoría mínima.")
                .setView(motivo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (d,w) -> viewModel.eliminarIncidente(idIncidente, motivo.getText().toString().trim()))
                .show();
    }

    private String valor(String v) { return v == null || v.trim().isEmpty() ? "-" : v; }

    private void mostrarFotos(List<FotoIncidente> fotos) {
        binding.contenedorFotos.removeAllViews();
        if (fotos == null || fotos.isEmpty()) { binding.tvSinFotos.setVisibility(View.VISIBLE); return; }
        binding.tvSinFotos.setVisibility(View.GONE);
        for (FotoIncidente foto : fotos) {
            ImageView imagen = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(150), dp(115)); params.setMarginEnd(dp(12)); imagen.setLayoutParams(params);
            imagen.setScaleType(ImageView.ScaleType.CENTER_CROP); imagen.setBackgroundResource(R.drawable.bg_image_preview); imagen.setClipToOutline(true);
            GlideUrl url = crearUrlConJwt(foto.getUrl()); Glide.with(this).load(url).centerCrop().into(imagen);
            imagen.setOnClickListener(v -> mostrarFotoGrande(url)); binding.contenedorFotos.addView(imagen);
        }
    }

    private GlideUrl crearUrlConJwt(String url) { return new GlideUrl(url, new LazyHeaders.Builder().addHeader("Authorization", "JWT " + SessionManager.getInstance().getToken()).build()); }
    private void mostrarFotoGrande(GlideUrl url) { ImageView image=new ImageView(requireContext()); image.setAdjustViewBounds(true); image.setPadding(dp(8),dp(8),dp(8),dp(8)); Glide.with(this).load(url).fitCenter().into(image); new MaterialAlertDialogBuilder(requireContext()).setView(image).setPositiveButton("Cerrar",null).show(); }

    private void abrirComoLlegar() {
        if (incidenteActual == null || incidenteActual.getLatitud() == null || incidenteActual.getLongitud() == null) { Snackbar.make(binding.getRoot(),"La sucursal no tiene coordenadas disponibles",Snackbar.LENGTH_LONG).show(); return; }
        double lat=incidenteActual.getLatitud(), lon=incidenteActual.getLongitud();
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+lat+","+lon)); intent.setPackage("com.google.android.apps.maps");
        if(intent.resolveActivity(requireContext().getPackageManager())!=null){startActivity(intent);return;}
        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/dir/?api=1&destination="+lat+","+lon)));
    }

    private void mostrarDialogoEstado() {
        DialogCambiarEstadoBinding d = DialogCambiarEstadoBinding.inflate(getLayoutInflater());
        String[] estados = "Administrador".equalsIgnoreCase(rol) ? new String[]{"Abierto","En proceso","Resuelto","Cerrado"} : new String[]{"En proceso","Resuelto"};
        d.actEstado.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_dropdown_item_1line,estados)); d.actEstado.setText(estados[0],false);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext()).setTitle("Cambiar estado").setView(d.getRoot()).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",null).create();
        dialog.setOnShowListener(x -> dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener(v->{String estado=d.actEstado.getText().toString().trim();String comentario=d.etComentario.getText()==null?"":d.etComentario.getText().toString().trim();if(comentario.isEmpty()){d.tilComentario.setError("El comentario es obligatorio");return;}viewModel.cambiarEstado(idIncidente,estado,comentario);dialog.dismiss();})); dialog.show();
    }

    private void mostrarDialogoTecnicos(List<Tecnico> tecnicos) {
        if(tecnicos.isEmpty()){Snackbar.make(binding.getRoot(),"No hay técnicos disponibles en esta sucursal",Snackbar.LENGTH_SHORT).show();return;}
        String[] nombres=new String[tecnicos.size()];for(int i=0;i<tecnicos.size();i++)nombres[i]=tecnicos.get(i).getNombre();final int[] sel={0};
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Asignar técnico de esta sucursal").setSingleChoiceItems(nombres,0,(d,w)->sel[0]=w).setNegativeButton("Cancelar",null).setPositiveButton("Asignar",(d,w)->viewModel.asignarTecnico(idIncidente,tecnicos.get(sel[0]).getId())).show();
    }
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}
