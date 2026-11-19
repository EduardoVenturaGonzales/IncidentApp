package com.example.incidentapp.adapter;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.incidentapp.databinding.ItemNotificacionBinding;
import com.example.incidentapp.model.Notificacion;
import java.util.ArrayList;
import java.util.List;

public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.ViewHolder> {
    public interface Listener {
        void onClick(Notificacion notificacion);
    }
    private final Listener listener;
    private final List<Notificacion> items = new ArrayList<>();
    public NotificacionAdapter(Listener listener) {
        this.listener = listener;
    }
    public void setItems(List<Notificacion> lista) { items.clear(); if (lista != null) items.addAll(lista); notifyDataSetChanged(); }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemNotificacionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion n = items.get(position);
        holder.binding.tvTitulo.setText(n.getTitulo());
        holder.binding.tvMensaje.setText(n.getMensaje());
        holder.binding.tvFecha.setText(n.getFecha());
        holder.binding.tvTitulo.setTypeface(null, n.isLeida() ? Typeface.NORMAL : Typeface.BOLD);
        holder.binding.indicadorNoLeida.setVisibility(n.isLeida() ? android.view.View.GONE : android.view.View.VISIBLE);
        holder.binding.getRoot().setOnClickListener(v -> listener.onClick(n));
    }
    @Override public int getItemCount() {
        return items.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemNotificacionBinding binding;
        ViewHolder(ItemNotificacionBinding binding) {
            super(binding.getRoot()); this.binding = binding;
        }
    }
}