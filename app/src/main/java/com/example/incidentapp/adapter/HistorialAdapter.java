package com.example.incidentapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.incidentapp.databinding.ItemHistorialBinding;
import com.example.incidentapp.model.Historial;

import java.util.ArrayList;
import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private final List<Historial> lista = new ArrayList<>();

    public void setHistorial(List<Historial> historial) {
        lista.clear();
        if (historial != null) {
            lista.addAll(historial);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistorialBinding binding = ItemHistorialBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new HistorialViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        holder.bind(lista.get(position));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistorialBinding binding;

        HistorialViewHolder(ItemHistorialBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Historial item) {
            String cambio = (item.getEstadoAnterior() == null ? "-" : item.getEstadoAnterior())
                    + " → "
                    + (item.getEstadoNuevo() == null ? "-" : item.getEstadoNuevo());

            binding.tvCambio.setText(cambio);
            binding.tvComentario.setText(item.getComentario());
            binding.tvUsuario.setText(
                    (item.getUsuario() == null ? "Usuario" : item.getUsuario())
                            + (item.getRol() == null ? "" : " · " + item.getRol())
            );
            binding.tvFecha.setText(item.getFechaCambio() == null ? "" : item.getFechaCambio());
        }
    }
}
