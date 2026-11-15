package com.example.incidentapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.incidentapp.databinding.ItemSucursalGestionBinding;
import com.example.incidentapp.model.Sucursal;

import java.util.ArrayList;
import java.util.List;

public class SucursalGestionAdapter extends RecyclerView.Adapter<SucursalGestionAdapter.ViewHolder> {
    public interface Listener { void onEliminar(Sucursal sucursal); }
    private final Listener listener;
    private final List<Sucursal> sucursales = new ArrayList<>();

    public SucursalGestionAdapter(Listener listener) { this.listener = listener; }
    public void setSucursales(List<Sucursal> lista) {
        sucursales.clear(); if (lista != null) sucursales.addAll(lista); notifyDataSetChanged();
    }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSucursalGestionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Sucursal s = sucursales.get(position);
        holder.binding.tvNombre.setText(s.getNombre());
        holder.binding.tvEmpresa.setText(s.getEmpresa() == null ? "" : s.getEmpresa());
        holder.binding.tvDireccion.setText(s.getDireccion());
        holder.binding.btnEliminar.setOnClickListener(v -> listener.onEliminar(s));
    }
    @Override public int getItemCount() { return sucursales.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSucursalGestionBinding binding;
        ViewHolder(ItemSucursalGestionBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
