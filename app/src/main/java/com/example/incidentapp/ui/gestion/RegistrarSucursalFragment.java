package com.example.incidentapp.ui.gestion;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.incidentapp.adapter.SucursalGestionAdapter;
import com.example.incidentapp.databinding.FragmentRegistrarSucursalBinding;
import com.example.incidentapp.model.Empresa;
import com.example.incidentapp.model.Sucursal;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.RegistrarSucursalViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class RegistrarSucursalFragment extends Fragment {
    private FragmentRegistrarSucursalBinding binding;
    private RegistrarSucursalViewModel viewModel;
    private final List<Empresa> empresas = new ArrayList<>();
    private SucursalGestionAdapter adapter;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state){
        binding=FragmentRegistrarSucursalBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, Bundle state){
        super.onViewCreated(view,state);
        if(!"Desarrollador".equalsIgnoreCase(SessionManager.getInstance().getRol())){
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }
        viewModel=new ViewModelProvider(this).get(RegistrarSucursalViewModel.class);
        adapter=new SucursalGestionAdapter(this::confirmarEliminar);
        binding.recyclerSucursales.setLayoutManager(new LinearLayoutManager(requireContext())); binding.recyclerSucursales.setAdapter(adapter); binding.recyclerSucursales.setNestedScrollingEnabled(false);
        binding.btnVolver.setOnClickListener(v->NavHostFragment.findNavController(this).popBackStack()); binding.btnRegistrar.setOnClickListener(v->registrar());
        viewModel.getEmpresas().observe(getViewLifecycleOwner(),lista->{
            empresas.clear();
            if(lista!=null)empresas.addAll(lista);
            List<String>n=new ArrayList<>();
            for(Empresa e:empresas)n.add(e.getNombre());
            binding.actEmpresa.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_dropdown_item_1line,n));
        });
        viewModel.getSucursales().observe(getViewLifecycleOwner(),adapter::setSucursales);
        viewModel.getLoading().observe(getViewLifecycleOwner(),l->{
            boolean b=Boolean.TRUE.equals(l);binding.progressRegistro.setVisibility(b?View.VISIBLE:View.GONE);
            binding.btnRegistrar.setEnabled(!b);
        });
        viewModel.getMensaje().observe(getViewLifecycleOwner(),e->{
            String m=e.getContentIfNotHandled();
            if(m!=null)Snackbar.make(binding.getRoot(),m,Snackbar.LENGTH_LONG).show();
        });
        viewModel.getRegistrado().observe(getViewLifecycleOwner(),e->{
            Boolean ok=e.getContentIfNotHandled();
            if(Boolean.TRUE.equals(ok))limpiar();
        });
        viewModel.cargarTodo();
    }
    private void registrar(){
        String empresaNom=binding.actEmpresa.getText().toString().trim();
        Empresa emp=null;
        for(Empresa e:empresas)if(e.getNombre().equals(empresaNom)){
            emp=e;break;
        }
        String nombre=txt(binding.etNombre),dir=txt(binding.etDireccion),lat=txt(binding.etLatitud),lon=txt(binding.etLongitud);
        if(emp==null){
            binding.tilEmpresa.setError("Selecciona una empresa");
            return;
        }
        if(nombre.isEmpty()){
            binding.tilNombre.setError("Obligatorio");
            return;
        }
        if(dir.isEmpty()){
            binding.tilDireccion.setError("Obligatorio");
            return;
        }
        try{
            viewModel.registrarSucursal(nombre,dir,Double.parseDouble(lat),Double.parseDouble(lon),emp.getId());
        }catch(Exception ex){
            Snackbar.make(binding.getRoot(),"Latitud y longitud deben ser números válidos",Snackbar.LENGTH_LONG).show();
        }
    }
    private void confirmarEliminar(Sucursal s){
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Eliminar sucursal").setMessage("Se desactivará \""+s.getNombre()+"\" y sus usuarios/activos. El historial se conserva.").setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(d,w)->viewModel.eliminarSucursal(s.getId())).show();
    }
    private String txt(android.widget.EditText e){
        return e.getText()==null?"":e.getText().toString().trim();
    }
    private void limpiar(){
        binding.actEmpresa.setText("",false);
        binding.etNombre.setText("");
        binding.etDireccion.setText("");
        binding.etLatitud.setText("");
        binding.etLongitud.setText("");
    }
    @Override public void onDestroyView(){
        super.onDestroyView();binding=null;
    }
}