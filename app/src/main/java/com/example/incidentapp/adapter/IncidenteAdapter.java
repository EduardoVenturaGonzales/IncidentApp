package com.example.incidentapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.incidentapp.R;
import com.example.incidentapp.databinding.ItemIncidenteBinding;
import com.example.incidentapp.model.Incidente;

import java.util.ArrayList;
import java.util.List;

public class IncidenteAdapter extends RecyclerView.Adapter<IncidenteAdapter.IncidenteViewHolder> {

    public interface OnIncidenteClickListener {
        void onClick(Incidente incidente);
    }

    private final List<Incidente> lista = new ArrayList<>();
    private final OnIncidenteClickListener listener;

    public IncidenteAdapter(OnIncidenteClickListener listener) {
        this.listener = listener;
    }

    public void setIncidentes(List<Incidente> incidentes) {
        lista.clear();
        if (incidentes != null) {
            lista.addAll(incidentes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IncidenteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIncidenteBinding binding = ItemIncidenteBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new IncidenteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidenteViewHolder holder, int position) {
        holder.bind(lista.get(position));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    class IncidenteViewHolder extends RecyclerView.ViewHolder {
        private final ItemIncidenteBinding binding;

        IncidenteViewHolder(ItemIncidenteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Incidente incidente) {
            binding.tvTicket.setText("Incidente #" + incidente.getId());
            binding.tvTitulo.setText(incidente.getTitulo());
            binding.tvEstado.setText(incidente.getEstado());
            binding.tvPrioridad.setText(incidente.getPrioridad());
            binding.tvFecha.setText(incidente.getFechaRegistro() == null ? "Sin fecha" : incidente.getFechaRegistro());
            binding.tvTecnico.setText(
                    incidente.getTecnico() == null || incidente.getTecnico().trim().isEmpty()
                            ? "Sin técnico asignado"
                            : incidente.getTecnico()
            );

            int color;
            if ("Alta".equalsIgnoreCase(incidente.getPrioridad())) {
                color = Color.parseColor("#C62828");
            } else if ("Media".equalsIgnoreCase(incidente.getPrioridad())) {
                color = Color.parseColor("#EF6C00");
            } else {
                color = Color.parseColor("#2E7D32");
            }
            binding.tvPrioridad.setTextColor(color);

            binding.getRoot().setOnClickListener(v -> listener.onClick(incidente));
        }
    }
}
