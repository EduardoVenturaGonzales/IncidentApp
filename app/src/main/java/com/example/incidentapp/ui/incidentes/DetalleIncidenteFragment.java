package com.example.incidentapp.ui.incidentes;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.incidentapp.R;
import com.example.incidentapp.adapter.HistorialAdapter;
import com.example.incidentapp.databinding.DialogCambiarEstadoBinding;
import com.example.incidentapp.databinding.DialogFotoIncidenteBinding;
import com.example.incidentapp.databinding.FragmentDetalleIncidenteBinding;
import com.example.incidentapp.model.DetalleIncidenteResponse;
import com.example.incidentapp.model.FotoIncidente;
import com.example.incidentapp.model.Incidente;
import com.example.incidentapp.model.SolicitudAnulacion;
import com.example.incidentapp.model.Tecnico;
import com.example.incidentapp.model.VerificarActivoResponse;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.util.SlaUtils;
import com.example.incidentapp.viewmodel.DetalleIncidenteViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetalleIncidenteFragment extends Fragment {

    private FragmentDetalleIncidenteBinding binding;
    private DetalleIncidenteViewModel viewModel;
    private HistorialAdapter historialAdapter;
    private int idIncidente;
    private Incidente incidenteActual;
    private String rol;

    private final Handler timer = new Handler(Looper.getMainLooper());

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            actualizarSla();
            timer.postDelayed(this, 1000);
        }
    };

    private final ActivityResultLauncher<ScanOptions> scanner = registerForActivityResult(
            new ScanContract(),
            resultado -> {
                if (resultado.getContents() != null && viewModel != null) {
                    viewModel.verificarActivo(idIncidente, resultado.getContents().trim());
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetalleIncidenteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idIncidente = getArguments() == null
                ? 0
                : getArguments().getInt("id_incidente", 0);

        rol = SessionManager.getInstance().getRol();
        viewModel = new ViewModelProvider(this).get(DetalleIncidenteViewModel.class);
        historialAdapter = new HistorialAdapter();

        configurarHistorial();
        configurarAcciones();
        observarViewModel();

        if (idIncidente > 0) {
            viewModel.cargarDetalle(idIncidente);
        }

        timer.post(tick);
    }

    private void configurarHistorial() {
        binding.recyclerHistorial.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerHistorial.setAdapter(historialAdapter);
        binding.recyclerHistorial.setNestedScrollingEnabled(false);
    }

    private void configurarAcciones() {
        binding.btnVolver.setOnClickListener(
                v -> requireActivity().getOnBackPressedDispatcher().onBackPressed()
        );
        binding.btnCambiarEstado.setOnClickListener(v -> dialogEstado());
        binding.btnCambiarPrioridad.setOnClickListener(v -> dialogPrioridad());
        binding.btnAsignarTecnico.setOnClickListener(v -> viewModel.cargarTecnicos());
        binding.btnComoLlegar.setOnClickListener(v -> comoLlegar());
        binding.btnVerificarActivo.setOnClickListener(v -> scan());
        binding.btnSolicitarAnulacion.setOnClickListener(v -> dialogAnular());
        binding.btnEliminarIncidente.setOnClickListener(v -> dialogEliminar());
    }

    private void observarViewModel() {
        viewModel.getLoading().observe(
                getViewLifecycleOwner(),
                loading -> binding.progressDetalle.setVisibility(
                        Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE
                )
        );

        viewModel.getDetalle().observe(getViewLifecycleOwner(), this::mostrar);

        viewModel.getMensaje().observe(getViewLifecycleOwner(), evento -> {
            String mensaje = evento.getContentIfNotHandled();
            if (mensaje != null && binding != null) {
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getTecnicos().observe(getViewLifecycleOwner(), evento -> {
            List<Tecnico> tecnicos = evento.getContentIfNotHandled();
            if (tecnicos != null) {
                dialogTecnicos(tecnicos);
            }
        });

        viewModel.getVerificacion().observe(getViewLifecycleOwner(), evento -> {
            VerificarActivoResponse respuesta = evento.getContentIfNotHandled();
            if (respuesta != null) {
                Snackbar.make(binding.getRoot(), respuesta.getMensaje(), Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getEliminado().observe(getViewLifecycleOwner(), evento -> {
            Boolean eliminado = evento.getContentIfNotHandled();
            if (Boolean.TRUE.equals(eliminado) && binding != null) {
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Incidente eliminado correctamente", Snackbar.LENGTH_SHORT);
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (isAdded()) {
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });

                snackbar.show();
            }
        });
    }

    private void mostrar(DetalleIncidenteResponse detalle) {
        if (detalle == null || detalle.getIncidente() == null) {
            return;
        }

        incidenteActual = detalle.getIncidente();

        binding.tvTicket.setText("Incidente #" + incidenteActual.getId());
        binding.tvTitulo.setText(incidenteActual.getTitulo());
        binding.tvDescripcion.setText(incidenteActual.getDescripcion());
        binding.tvEstado.setText(incidenteActual.getEstado());
        binding.tvPrioridad.setText(incidenteActual.getPrioridad());
        binding.tvCategoria.setText(incidenteActual.getCategoria());
        binding.tvSucursal.setText(valor(incidenteActual.getSucursal()));
        binding.tvDireccion.setText(valor(incidenteActual.getDireccionSucursal()));
        binding.tvActivo.setText(
                incidenteActual.getActivo() == null
                        ? "Sin activo asociado"
                        : incidenteActual.getActivo()
        );
        binding.tvCodigoQr.setText(
                incidenteActual.getCodigoQr() == null ? "-" : incidenteActual.getCodigoQr()
        );
        binding.tvSolicitante.setText(valor(incidenteActual.getSolicitante()));
        binding.tvTecnico.setText(
                incidenteActual.getTecnico() == null
                        ? "Sin técnico asignado"
                        : incidenteActual.getTecnico()
        );
        binding.tvFecha.setText(valor(incidenteActual.getFechaRegistro()));

        historialAdapter.setHistorial(detalle.getHistorial());
        binding.tvSinHistorial.setVisibility(
                detalle.getHistorial() == null || detalle.getHistorial().isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );

        aplicarIndicadorEstadoFinal();
        mostrarFotos(detalle.getFotos());
        actualizarAcciones(detalle);
        actualizarSla();
    }

    private void aplicarIndicadorEstadoFinal() {
        if (incidenteActual == null) {
            return;
        }

        String estado = incidenteActual.getEstado();
        boolean indicadorRojo = "No Resuelto".equalsIgnoreCase(estado)
                || "Anulado".equalsIgnoreCase(estado);

        if (indicadorRojo) {
            binding.cardResumenIncidente.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.incident_final_background));
            binding.cardResumenIncidente.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.incident_final_stroke));
            binding.tvEstado.setTextColor(ContextCompat.getColor(requireContext(), R.color.incident_final_text));
        } else {
            binding.cardResumenIncidente.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            binding.cardResumenIncidente.setStrokeColor(Color.parseColor("#D7DCE0"));
            binding.tvEstado.setTextColor(ContextCompat.getColor(requireContext(), R.color.incidentapp_dark));
        }
    }

    private void actualizarSla() {
        if (binding == null || incidenteActual == null) {
            return;
        }

        SlaUtils.Estado estado = SlaUtils.calcular(incidenteActual);
        binding.tvSlaDetalle.setText(estado.texto);

        int color;
        if (estado.color == SlaUtils.ColorEstado.ROJO) {
            color = Color.parseColor("#C62828");
        } else if (estado.color == SlaUtils.ColorEstado.AMARILLO) {
            color = Color.parseColor("#EF6C00");
        } else if (estado.color == SlaUtils.ColorEstado.VERDE) {
            color = Color.parseColor("#2E7D32");
        } else {
            color = Color.GRAY;
        }

        binding.tvSlaDetalle.setTextColor(color);
    }

    private void actualizarAcciones(DetalleIncidenteResponse detalle) {
        boolean administrador = "Administrador".equalsIgnoreCase(rol);
        boolean tecnico = "Tecnico".equalsIgnoreCase(rol);
        boolean solicitante = "Solicitante".equalsIgnoreCase(rol);

        String estado = incidenteActual.getEstado();
        boolean finalizado = "Resuelto".equalsIgnoreCase(estado) || "No Resuelto".equalsIgnoreCase(estado) || "Anulado".equalsIgnoreCase(estado);

        binding.btnCambiarEstado.setVisibility((administrador || tecnico) && !finalizado ? View.VISIBLE : View.GONE);
        binding.btnCambiarPrioridad.setVisibility(administrador && !finalizado ? View.VISIBLE : View.GONE);
        binding.btnAsignarTecnico.setVisibility(administrador && !finalizado ? View.VISIBLE : View.GONE);
        binding.btnComoLlegar.setVisibility(tecnico && !"Anulado".equalsIgnoreCase(estado) ? View.VISIBLE : View.GONE);
        binding.btnVerificarActivo.setVisibility(
                tecnico && !finalizado && incidenteActual.getIdActivo() != null
                        ? View.VISIBLE
                        : View.GONE
        );
        binding.btnEliminarIncidente.setVisibility(administrador ? View.VISIBLE : View.GONE);

        SolicitudAnulacion solicitud = detalle.getSolicitudAnulacion();
        boolean pendiente = solicitud != null
                && "Pendiente".equalsIgnoreCase(solicitud.getEstado());

        binding.btnSolicitarAnulacion.setVisibility(
                solicitante && !finalizado && !pendiente ? View.VISIBLE : View.GONE
        );

        if (solicitante && solicitud != null) {
            binding.tvSolicitudAnulacion.setVisibility(View.VISIBLE);
            binding.tvSolicitudAnulacion.setText(
                    "Solicitud de anulación: " + solicitud.getEstado()
            );
        } else {
            binding.tvSolicitudAnulacion.setVisibility(View.GONE);
        }
    }

    private void dialogEstado() {
        if (incidenteActual == null) {
            return;
        }

        String estadoActual = incidenteActual.getEstado();
        List<String> opciones = new ArrayList<>();

        if ("Tecnico".equalsIgnoreCase(rol)) {
            if ("Abierto".equalsIgnoreCase(estadoActual)) {
                opciones.add("En proceso");
                opciones.add("Resuelto");
            } else if ("En proceso".equalsIgnoreCase(estadoActual)) {
                opciones.add("Resuelto");
            }
        } else if ("Administrador".equalsIgnoreCase(rol)) {
            if ("Abierto".equalsIgnoreCase(estadoActual)) {
                opciones.add("En proceso");
                opciones.add("Resuelto");
            } else if ("En proceso".equalsIgnoreCase(estadoActual)) {
                opciones.add("Resuelto");
            } else if ("No Resuelto".equalsIgnoreCase(estadoActual)) {
                opciones.add("Abierto");
            }
        }

        if (opciones.isEmpty()) {
            Snackbar.make(binding.getRoot(), "No hay cambios de estado disponibles", Snackbar.LENGTH_SHORT).show();
            return;
        }

        DialogCambiarEstadoBinding dialogBinding = DialogCambiarEstadoBinding.inflate(
                getLayoutInflater()
        );

        dialogBinding.actEstado.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        opciones
                )
        );
        dialogBinding.actEstado.setText(opciones.get(0), false);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambiar estado")
                .setView(dialogBinding.getRoot())
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();

        dialog.setOnShowListener(ignored -> dialog
                .getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String nuevoEstado = dialogBinding.actEstado.getText().toString().trim();
                    String comentario = dialogBinding.etComentario.getText() == null
                            ? ""
                            : dialogBinding.etComentario.getText().toString().trim();

                    if (comentario.isEmpty()) {
                        dialogBinding.tilComentario.setError("El comentario es obligatorio");
                        return;
                    }

                    viewModel.cambiarEstado(idIncidente, nuevoEstado, comentario);
                    dialog.dismiss();
                }));

        dialog.show();
    }

    private void dialogPrioridad() {
        String[] prioridades = {"Alta", "Media", "Baja"};
        final int[] seleccion = {
                Math.max(0, Arrays.asList(prioridades).indexOf(incidenteActual.getPrioridad()))
        };

        EditText comentario = new EditText(requireContext());
        comentario.setHint("Comentario (opcional)");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambiar prioridad")
                .setSingleChoiceItems(
                        prioridades,
                        seleccion[0],
                        (dialog, posicion) -> seleccion[0] = posicion
                )
                .setView(comentario)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(
                        "Guardar",
                        (dialog, which) -> viewModel.cambiarPrioridad(
                                idIncidente,
                                prioridades[seleccion[0]],
                                comentario.getText().toString().trim()
                        )
                )
                .show();
    }

    private void scan() {
        ScanOptions opciones = new ScanOptions();
        opciones.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        opciones.setPrompt("Escanea el QR del equipo que estás atendiendo");
        opciones.setBeepEnabled(true);
        opciones.setOrientationLocked(false);
        scanner.launch(opciones);
    }

    private void dialogAnular() {
        EditText motivo = new EditText(requireContext());
        motivo.setHint("Motivo de anulación");
        motivo.setMinLines(3);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Solicitar anulación")
                .setMessage("Explica por qué deseas anular este incidente.")
                .setView(motivo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Solicitar", null)
                .create();

        dialog.setOnShowListener(ignored -> dialog
                .getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String texto = motivo.getText().toString().trim();
                    if (texto.isEmpty()) {
                        motivo.setError("El motivo es obligatorio");
                        return;
                    }

                    viewModel.solicitarAnulacion(idIncidente, texto);
                    dialog.dismiss();
                }));

        dialog.show();
    }

    private void dialogEliminar() {
        EditText motivo = new EditText(requireContext());
        motivo.setHint("Motivo de eliminación (opcional)");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar incidente")
                .setMessage(
                        "Esta acción eliminará definitivamente el incidente, "
                                + "su historial y fotografías."
                )
                .setView(motivo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(
                        "Eliminar",
                        (dialog, which) -> viewModel.eliminarIncidente(
                                idIncidente,
                                motivo.getText().toString().trim()
                        )
                )
                .show();
    }

    private void dialogTecnicos(List<Tecnico> tecnicos) {
        if (tecnicos.isEmpty()) {
            Snackbar.make(
                    binding.getRoot(),
                    "No hay técnicos disponibles",
                    Snackbar.LENGTH_SHORT
            ).show();
            return;
        }

        String[] nombres = new String[tecnicos.size()];
        for (int i = 0; i < tecnicos.size(); i++) {
            nombres[i] = tecnicos.get(i).getNombre();
        }

        final int[] seleccion = {0};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Asignar técnico")
                .setSingleChoiceItems(nombres, 0, (dialog, posicion) -> seleccion[0] = posicion)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(
                        "Asignar",
                        (dialog, which) -> viewModel.asignarTecnico(
                                idIncidente,
                                tecnicos.get(seleccion[0]).getId()
                        )
                )
                .show();
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

            GlideUrl urlFoto = crearUrlProtegida(foto.getUrl());

            Glide.with(this)
                    .load(urlFoto)
                    .centerCrop()
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(imagen);

            imagen.setOnClickListener(v -> mostrarFotoGrande(urlFoto));
            binding.contenedorFotos.addView(imagen);
        }
    }

    private GlideUrl crearUrlProtegida(String url) {
        return new GlideUrl(url, new LazyHeaders.Builder().addHeader("Authorization", "JWT " + SessionManager.getInstance().getToken()).build());
    }

    private void mostrarFotoGrande(GlideUrl urlFoto) {
        DialogFotoIncidenteBinding dialogBinding = DialogFotoIncidenteBinding.inflate(
                getLayoutInflater()
        );

        Glide.with(this)
                .load(urlFoto)
                .fitCenter()
                .error(android.R.drawable.ic_dialog_alert)
                .into(dialogBinding.ivFotoGrande);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Foto del incidente")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void comoLlegar() {
        if (incidenteActual == null
                || incidenteActual.getLatitud() == null
                || incidenteActual.getLongitud() == null) {
            Snackbar.make(
                    binding.getRoot(),
                    "La sucursal no tiene coordenadas",
                    Snackbar.LENGTH_LONG
            ).show();
            return;
        }

        double latitud = incidenteActual.getLatitud();
        double longitud = incidenteActual.getLongitud();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + latitud + "," + longitud));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "https://www.google.com/maps/dir/?api=1&destination="
                                            + latitud
                                            + ","
                                            + longitud
                            )
                    )
            );
        }
    }

    private String valor(String valor) {
        return valor == null || valor.trim().isEmpty() ? "-" : valor;
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timer.removeCallbacks(tick);
        binding = null;
    }
}