package com.example.incidentapp.adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.incidentapp.databinding.ItemActivoBinding;
import com.example.incidentapp.model.Activo;
import java.util.ArrayList;
import java.util.List;

public class ActivoAdapter extends RecyclerView.Adapter<ActivoAdapter.ViewHolder> {
    public interface Listener {
        void onVerQr(Activo activo);
        void onEliminar(Activo activo);
    }
    private final Listener listener;
    private final List<Activo> activos = new ArrayList<>();
    public ActivoAdapter(Listener listener) {
        this.listener = listener;
    }
    public void setActivos(List<Activo> lista) { activos.clear(); if (lista != null) activos.addAll(lista); notifyDataSetChanged(); }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemActivoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activo a = activos.get(position);
        holder.binding.tvCodigo.setText(a.getCodigoQr());
        holder.binding.tvNombre.setText(a.getNombre());
        holder.binding.tvDetalle.setText(a.getTipo() + " · " + a.getEstado());
        holder.binding.btnQr.setOnClickListener(v -> listener.onVerQr(a));
        holder.binding.btnEliminar.setOnClickListener(v -> listener.onEliminar(a));
    }
    @Override public int getItemCount() { return activos.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemActivoBinding binding;
        ViewHolder(ItemActivoBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}