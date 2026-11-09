package com.example.incidentapp.ui.mapa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.incidentapp.R;
import com.example.incidentapp.databinding.FragmentMapaBinding;
import com.example.incidentapp.model.Sucursal;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.MapaViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

public class MapaFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener {

    private FragmentMapaBinding binding;
    private MapaViewModel viewModel;
    private GoogleMap mMap;
    private List<Sucursal> sucursales = Collections.emptyList();
    private boolean esAdministrador;
    private boolean esDesarrollador;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMapaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MapaViewModel.class);

        String rol = SessionManager.getInstance().getRol();
        esAdministrador = "Administrador".equalsIgnoreCase(rol);
        esDesarrollador = "Desarrollador".equalsIgnoreCase(rol);

        if (esDesarrollador) {
            binding.tvTituloMapa.setText("Todas las sucursales");
            binding.checkMostrarTodas.setVisibility(View.GONE);
        } else if (esAdministrador) {
            binding.tvTituloMapa.setText("Sucursales de la empresa");
            binding.checkMostrarTodas.setVisibility(View.VISIBLE);
            binding.checkMostrarTodas.setChecked(false);
            binding.checkMostrarTodas.setOnCheckedChangeListener((buttonView, isChecked) -> {
                binding.tvTituloMapa.setText(
                        isChecked ? "Sucursales de la empresa" : "Mi sucursal"
                );
                viewModel.cargarSucursales(isChecked);
            });
        } else {
            binding.tvTituloMapa.setText("Mi sucursal");
            binding.checkMostrarTodas.setVisibility(View.GONE);
        }

        binding.btnVolver.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapContainer);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, mapFragment)
                    .commitNow();
        }

        mapFragment.getMapAsync(this);

        viewModel.getSucursales().observe(getViewLifecycleOwner(), lista -> {
            sucursales = lista == null ? Collections.emptyList() : lista;
            dibujarSucursales();
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), cargando ->
                binding.progressMapa.setVisibility(
                        Boolean.TRUE.equals(cargando) ? View.VISIBLE : View.GONE
                )
        );

        viewModel.getMensaje().observe(getViewLifecycleOwner(), evento -> {
            String mensaje = evento.getContentIfNotHandled();
            if (mensaje != null) {
                Snackbar.make(binding.getRoot(), mensaje, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.cargarSucursales(false);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Sucursal) {
                mostrarSucursal((Sucursal) tag);
            }
            return false;
        });

        dibujarSucursales();
    }

    private void dibujarSucursales() {
        if (mMap == null) {
            return;
        }

        mMap.clear();

        if (sucursales == null || sucursales.isEmpty()) {
            binding.tvSucursalMapa.setText("Sin sucursales disponibles");
            binding.tvEmpresaMapa.setText("");
            binding.tvDireccionMapa.setText("");
            binding.tvIncidentesMapa.setText("");
            binding.tvCoordenadasMapa.setText("");
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        int marcadoresValidos = 0;
        Marker primerMarcador = null;
        LatLng primeraUbicacion = null;

        for (Sucursal sucursal : sucursales) {
            if (sucursal.getLatitud() == null || sucursal.getLongitud() == null) {
                continue;
            }

            LatLng ubicacion = new LatLng(
                    sucursal.getLatitud(),
                    sucursal.getLongitud()
            );

            String snippet = sucursal.getDireccion()
                    + " · Incidentes abiertos: "
                    + sucursal.getIncidentesAbiertos();

            Marker marker = mMap.addMarker(
                    new MarkerOptions()
                            .position(ubicacion)
                            .title(sucursal.getNombre())
                            .snippet(snippet)
            );

            if (marker != null) {
                marker.setTag(sucursal);
            }

            boundsBuilder.include(ubicacion);
            marcadoresValidos++;

            if (primeraUbicacion == null) {
                primeraUbicacion = ubicacion;
                primerMarcador = marker;
            }
        }

        if (marcadoresValidos == 1 && primeraUbicacion != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(primeraUbicacion, 15.5f));
        } else if (marcadoresValidos > 1) {
            binding.mapArea.post(() -> {
                try {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                            boundsBuilder.build(),
                            100
                    ));
                } catch (Exception ignored) {
                    // Si el mapa aún no terminó de medir, se mantiene la cámara actual.
                }
            });
        }

        if (primerMarcador != null) {
            primerMarcador.showInfoWindow();
            Object tag = primerMarcador.getTag();
            if (tag instanceof Sucursal) {
                mostrarSucursal((Sucursal) tag);
            }
        }
    }

    private void mostrarSucursal(Sucursal sucursal) {
        binding.tvSucursalMapa.setText(sucursal.getNombre());
        binding.tvEmpresaMapa.setText(
                sucursal.getEmpresa() == null ? "" : sucursal.getEmpresa()
        );
        binding.tvDireccionMapa.setText(sucursal.getDireccion());
        binding.tvIncidentesMapa.setText(
                "Incidentes abiertos: " + sucursal.getIncidentesAbiertos()
        );

        if (sucursal.getLatitud() != null && sucursal.getLongitud() != null) {
            binding.tvCoordenadasMapa.setText(
                    "Latitud: " + sucursal.getLatitud()
                            + "   Longitud: " + sucursal.getLongitud()
            );
        } else {
            binding.tvCoordenadasMapa.setText("Coordenadas no disponibles");
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        binding.tvPuntoMapa.setText(
                "Punto seleccionado: " + latLng.latitude + ", " + latLng.longitude
        );
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        binding.tvPuntoMapa.setText(
                "Punto seleccionado: " + latLng.latitude + ", " + latLng.longitude
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
