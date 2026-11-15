package com.example.incidentapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.incidentapp.databinding.ItemUsuarioGestionBinding;
import com.example.incidentapp.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuarioGestionAdapter extends RecyclerView.Adapter<UsuarioGestionAdapter.ViewHolder> {
    public interface Listener { void onEliminar(Usuario usuario); }
    private final Listener listener;
    private final List<Usuario> usuarios = new ArrayList<>();

    public UsuarioGestionAdapter(Listener listener) { this.listener = listener; }
    public void setUsuarios(List<Usuario> lista) {
        usuarios.clear(); if (lista != null) usuarios.addAll(lista); notifyDataSetChanged();
    }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemUsuarioGestionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Usuario u = usuarios.get(position);
        holder.binding.tvNombre.setText(u.getNombre());
        holder.binding.tvRol.setText(u.getRol());
        holder.binding.tvCorreo.setText(u.getCorreo());
        holder.binding.tvSucursal.setText(u.getSucursal() == null ? "" : u.getSucursal());
        holder.binding.btnEliminar.setOnClickListener(v -> listener.onEliminar(u));
    }
    @Override public int getItemCount() { return usuarios.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemUsuarioGestionBinding binding;
        ViewHolder(ItemUsuarioGestionBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
