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
import com.example.incidentapp.viewmodel.MapaViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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

        viewModel.cargarSucursales();
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
        if (mMap == null || sucursales == null || sucursales.isEmpty()) {
            return;
        }

        mMap.clear();
        LatLng primeraUbicacion = null;
        Marker primerMarcador = null;

        for (Sucursal sucursal : sucursales) {
            if (sucursal.getLatitud() == null || sucursal.getLongitud() == null) {
                continue;
            }

            LatLng ubicacion = new LatLng(
                    sucursal.getLatitud(),
                    sucursal.getLongitud()
            );

            Marker marker = mMap.addMarker(
                    new MarkerOptions()
                            .position(ubicacion)
                            .title(sucursal.getNombre())
                            .snippet(sucursal.getDireccion())
            );

            if (marker != null) {
                marker.setTag(sucursal);
            }

            if (primeraUbicacion == null) {
                primeraUbicacion = ubicacion;
                primerMarcador = marker;
            }
        }

        if (primeraUbicacion != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(primeraUbicacion, 15.5f));
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
        binding.tvDireccionMapa.setText(sucursal.getDireccion());

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
