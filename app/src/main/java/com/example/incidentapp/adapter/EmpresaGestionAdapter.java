package com.example.incidentapp.adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.incidentapp.databinding.ItemEmpresaGestionBinding;
import com.example.incidentapp.model.Empresa;
import java.util.ArrayList;
import java.util.List;

public class EmpresaGestionAdapter extends RecyclerView.Adapter<EmpresaGestionAdapter.ViewHolder> {
    public interface Listener {
        void onEliminar(Empresa empresa);
    }
    private final Listener listener;
    private final List<Empresa> empresas = new ArrayList<>();
    public EmpresaGestionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setEmpresas(List<Empresa> lista) {
        empresas.clear();
        if (lista != null) empresas.addAll(lista);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemEmpresaGestionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Empresa e = empresas.get(position);
        holder.binding.tvNombre.setText(e.getNombre());
        holder.binding.tvRuc.setText(e.getRuc() == null || e.getRuc().trim().isEmpty() ? "Sin RUC" : "RUC: " + e.getRuc());
        holder.binding.btnEliminar.setOnClickListener(v -> listener.onEliminar(e));
    }

    @Override public int getItemCount() {
        return empresas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemEmpresaGestionBinding binding;
        ViewHolder(ItemEmpresaGestionBinding binding) {
            super(binding.getRoot()); this.binding = binding;
        }
    }
}