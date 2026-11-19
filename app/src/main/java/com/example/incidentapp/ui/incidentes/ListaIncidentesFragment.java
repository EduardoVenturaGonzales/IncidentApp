package com.example.incidentapp.ui.incidentes;

import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.incidentapp.R;
import com.example.incidentapp.adapter.IncidenteAdapter;
import com.example.incidentapp.databinding.FragmentListaIncidentesBinding;
import com.example.incidentapp.util.AppEvents;
import com.example.incidentapp.viewmodel.IncidentesViewModel;
import com.google.android.material.snackbar.Snackbar;

public class ListaIncidentesFragment extends Fragment {
    private FragmentListaIncidentesBinding binding;
    private IncidentesViewModel viewModel;
    private IncidenteAdapter adapter;
    private boolean receiverRegistrado=false;
    private final BroadcastReceiver syncReceiver=new BroadcastReceiver(){
        @Override public void onReceive(Context context, Intent intent){ if(binding!=null) cargar(); }
    };

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding=FragmentListaIncidentesBinding.inflate(inflater,container,false); return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        viewModel=new ViewModelProvider(this).get(IncidentesViewModel.class);
        adapter=new IncidenteAdapter(incidente->{ Bundle args=new Bundle(); args.putInt("id_incidente",incidente.getId()); NavHostFragment.findNavController(this).navigate(R.id.action_listaIncidentesFragment_to_detalleIncidenteFragment,args); });
        binding.recyclerIncidentes.setLayoutManager(new LinearLayoutManager(requireContext())); binding.recyclerIncidentes.setAdapter(adapter);
        String[] estados={"Todos","Abierto","En proceso","Resuelto","No Resuelto","Anulado"};
        String[] prioridades={"Todos","Alta","Media","Baja"};
        binding.actEstado.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_dropdown_item_1line,estados));
        binding.actPrioridad.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_dropdown_item_1line,prioridades));
        binding.actEstado.setText("Todos",false); binding.actPrioridad.setText("Todos",false);
        binding.btnAplicarFiltros.setOnClickListener(v->cargar()); binding.btnReintentar.setOnClickListener(v->cargar()); binding.btnVolver.setOnClickListener(v->NavHostFragment.findNavController(this).popBackStack());
        viewModel.getLoading().observe(getViewLifecycleOwner(),c->binding.progressLista.setVisibility(Boolean.TRUE.equals(c)?View.VISIBLE:View.GONE));
        viewModel.getIncidentes().observe(getViewLifecycleOwner(),lista->{adapter.setIncidentes(lista);boolean vacia=lista==null||lista.isEmpty();binding.layoutVacio.setVisibility(vacia?View.VISIBLE:View.GONE);binding.recyclerIncidentes.setVisibility(vacia?View.GONE:View.VISIBLE);});
        viewModel.getError().observe(getViewLifecycleOwner(),evento->{String m=evento.getContentIfNotHandled();if(m!=null)Snackbar.make(binding.getRoot(),m,Snackbar.LENGTH_LONG).show();});
        cargar();
    }
    private void cargar(){ viewModel.cargar(binding.actEstado.getText().toString(),binding.actPrioridad.getText().toString()); }
    @Override public void onResume(){super.onResume();if(!receiverRegistrado){IntentFilter f=new IntentFilter(AppEvents.ACTION_SYNC_COMPLETADA);if(Build.VERSION.SDK_INT>=33)requireContext().registerReceiver(syncReceiver,f,Context.RECEIVER_NOT_EXPORTED);else requireContext().registerReceiver(syncReceiver,f);receiverRegistrado=true;}}
    @Override public void onPause(){super.onPause();if(receiverRegistrado){try{requireContext().unregisterReceiver(syncReceiver);}catch(Exception ignored){}receiverRegistrado=false;}}
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}
