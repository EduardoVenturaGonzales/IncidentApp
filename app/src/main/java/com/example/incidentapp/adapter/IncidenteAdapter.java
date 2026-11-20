package com.example.incidentapp.adapter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.incidentapp.R;
import com.example.incidentapp.databinding.ItemIncidenteBinding;
import com.example.incidentapp.model.Incidente;
import com.example.incidentapp.util.SlaUtils;
import java.util.ArrayList;
import java.util.List;

public class IncidenteAdapter extends RecyclerView.Adapter<IncidenteAdapter.VH> {
    public interface OnIncidenteClickListener {
        void onClick(Incidente incidente);
    }

    private final List<Incidente> lista = new ArrayList<>();
    private final OnIncidenteClickListener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
            handler.postDelayed(this, 1000);
        }
    };

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
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIncidenteBinding binding = ItemIncidenteBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(lista.get(position));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        handler.removeCallbacks(tick);
    }

    class VH extends RecyclerView.ViewHolder {
        private final ItemIncidenteBinding binding;

        VH(ItemIncidenteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Incidente incidente) {
            binding.tvTicket.setText("Incidente #" + incidente.getId());
            binding.tvTitulo.setText(incidente.getTitulo());
            binding.tvEstado.setText(incidente.getEstado());
            binding.tvPrioridad.setText(incidente.getPrioridad());
            binding.tvFecha.setText(
                    incidente.getFechaRegistro() == null
                            ? "Sin fecha"
                            : incidente.getFechaRegistro()
            );
            binding.tvTecnico.setText(
                    incidente.getTecnico() == null || incidente.getTecnico().trim().isEmpty()
                            ? "Sin técnico asignado"
                            : incidente.getTecnico()
            );

            aplicarColorPrioridad(incidente);
            aplicarSla(incidente);
            aplicarIndicadorEstadoFinal(incidente);

            binding.getRoot().setOnClickListener(v -> listener.onClick(incidente));
        }

        private void aplicarColorPrioridad(Incidente incidente) {
            int color;

            if ("Alta".equalsIgnoreCase(incidente.getPrioridad())) {
                color = Color.parseColor("#C62828");
            } else if ("Media".equalsIgnoreCase(incidente.getPrioridad())) {
                color = Color.parseColor("#EF6C00");
            } else {
                color = Color.parseColor("#2E7D32");
            }

            binding.tvPrioridad.setTextColor(color);
        }

        private void aplicarSla(Incidente incidente) {
            SlaUtils.Estado sla = SlaUtils.calcular(incidente);
            binding.tvSla.setText(sla.texto);

            int color;
            if (sla.color == SlaUtils.ColorEstado.ROJO) {
                color = Color.parseColor("#C62828");
            } else if (sla.color == SlaUtils.ColorEstado.AMARILLO) {
                color = Color.parseColor("#EF6C00");
            } else if (sla.color == SlaUtils.ColorEstado.VERDE) {
                color = Color.parseColor("#2E7D32");
            } else {
                color = Color.GRAY;
            }

            binding.tvSla.setTextColor(color);
        }

        private void aplicarIndicadorEstadoFinal(Incidente incidente) {
            String estado = incidente.getEstado();
            boolean indicadorRojo = "No Resuelto".equalsIgnoreCase(estado) || "Anulado".equalsIgnoreCase(estado);

            if (indicadorRojo) {
                binding.getRoot().setCardBackgroundColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.incident_final_background)
                );
                binding.getRoot().setStrokeColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.incident_final_stroke)
                );
                binding.tvEstado.setTextColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.incident_final_text)
                );
            } else {
                binding.getRoot().setCardBackgroundColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.white)
                );
                binding.getRoot().setStrokeColor(Color.parseColor("#D7DCE0"));
                binding.tvEstado.setTextColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.incidentapp_dark)
                );
            }
        }
    }
}