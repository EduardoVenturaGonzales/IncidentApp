package com.example.incidentapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.incidentapp.databinding.ItemSolicitudAnulacionBinding;
import com.example.incidentapp.model.SolicitudAnulacion;

import java.util.ArrayList;
import java.util.List;

public class SolicitudAnulacionAdapter extends RecyclerView.Adapter<SolicitudAnulacionAdapter.ViewHolder> {
    public interface Listener {
        void onAprobar(SolicitudAnulacion solicitud);
        void onRechazar(SolicitudAnulacion solicitud);
        void onAbrir(SolicitudAnulacion solicitud);
    }
    private final Listener listener;
    private final List<SolicitudAnulacion> items = new ArrayList<>();

    public SolicitudAnulacionAdapter(Listener listener) { this.listener = listener; }
    public void setItems(List<SolicitudAnulacion> lista) { items.clear(); if (lista != null) items.addAll(lista); notifyDataSetChanged(); }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSolicitudAnulacionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SolicitudAnulacion s = items.get(position);
        holder.binding.tvTitulo.setText("#" + s.getIdIncidente() + " · " + s.getTitulo());
        holder.binding.tvSolicitante.setText("Solicitante: " + s.getSolicitante());
        holder.binding.tvMotivo.setText(s.getMotivo());
        holder.binding.tvFecha.setText(s.getFechaSolicitud());
        boolean pendiente = "Pendiente".equalsIgnoreCase(s.getEstado());
        holder.binding.btnAprobar.setVisibility(pendiente ? android.view.View.VISIBLE : android.view.View.GONE);
        holder.binding.btnRechazar.setVisibility(pendiente ? android.view.View.VISIBLE : android.view.View.GONE);
        holder.binding.btnAprobar.setOnClickListener(v -> listener.onAprobar(s));
        holder.binding.btnRechazar.setOnClickListener(v -> listener.onRechazar(s));
        holder.binding.getRoot().setOnClickListener(v -> listener.onAbrir(s));
    }
    @Override public int getItemCount() { return items.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSolicitudAnulacionBinding binding;
        ViewHolder(ItemSolicitudAnulacionBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
