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
import com.example.incidentapp.adapter.UsuarioGestionAdapter;
import com.example.incidentapp.databinding.FragmentRegistrarUsuarioBinding;
import com.example.incidentapp.model.Sucursal;
import com.example.incidentapp.model.Usuario;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.viewmodel.RegistrarUsuarioViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class RegistrarUsuarioFragment extends Fragment {
    private FragmentRegistrarUsuarioBinding binding; private RegistrarUsuarioViewModel viewModel;
    private final List<Sucursal>sucursales=new ArrayList<>();
    private boolean dev,admin;
    private UsuarioGestionAdapter adapter;
    @Override public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container,Bundle state){
        binding=FragmentRegistrarUsuarioBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view,Bundle state){
        super.onViewCreated(view,state);
        String rol=SessionManager.getInstance().getRol();dev="Desarrollador".equalsIgnoreCase(rol);admin="Administrador".equalsIgnoreCase(rol);
        if(!dev&&!admin){NavHostFragment.findNavController(this).popBackStack();
            return;
        }
        viewModel=new ViewModelProvider(this).get(RegistrarUsuarioViewModel.class);
        adapter=new UsuarioGestionAdapter(this::confirmarEliminar);
        binding.recyclerUsuarios.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUsuarios.setAdapter(adapter);
        binding.recyclerUsuarios.setNestedScrollingEnabled(false);
        binding.btnVolver.setOnClickListener(v->NavHostFragment.findNavController(this).popBackStack());
        binding.btnRegistrar.setOnClickListener(v->registrar());configurar();
        viewModel.getSucursales().observe(getViewLifecycleOwner(),lista->{
            sucursales.clear();
            if(lista!=null)sucursales.addAll(lista);
            List<String>n=new ArrayList<>();
            for(Sucursal s:sucursales)n.add((s.getEmpresa()==null?"":s.getEmpresa()+" · ")+s.getNombre());
            binding.actSucursal.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_dropdown_item_1line,n));
            if(admin&&n.size()==1)binding.actSucursal.setText(n.get(0),false);
        });
        viewModel.getUsuarios().observe(getViewLifecycleOwner(),lista->{
            if(dev){
                List<Usuario>admins=new ArrayList<>();
                if(lista!=null)for(Usuario u:lista)if("Administrador".equalsIgnoreCase(u.getRol()))admins.add(u);
                adapter.setUsuarios(admins);
            }else adapter.setUsuarios(lista);
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(),l->{
            boolean b=Boolean.TRUE.equals(l);
            binding.progressRegistro.setVisibility(b?View.VISIBLE:View.GONE);
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
        viewModel.cargarSucursales(dev);
        viewModel.cargarUsuarios();
    }
    private void configurar(){
        if(dev){
            binding.tvTitulo.setText("Gestionar administradores");
            binding.tvDescripcion.setText("Registra o elimina administradores de las sucursales.");
            binding.tvTituloLista.setText("Administradores registrados");
            binding.actRol.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_dropdown_item_1line,new String[]{"Administrador"}));
            binding.actRol.setText("Administrador",false);
            binding.actRol.setEnabled(false);
            binding.tilSucursal.setVisibility(View.VISIBLE);
        }else{
            binding.tvTitulo.setText("Gestionar usuarios");
            binding.tvDescripcion.setText("Registra técnicos o solicitantes únicamente en tu sucursal.");
            binding.tvTituloLista.setText("Técnicos y solicitantes");
            binding.actRol.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_dropdown_item_1line,new String[]{"Tecnico","Solicitante"}));
            binding.tilSucursal.setVisibility(View.GONE);
        }
    }
    private void registrar(){
        String nombre=txt(binding.etNombre),correo=txt(binding.etCorreo),pass=txt(binding.etContrasena),rol=binding.actRol.getText().toString().trim();
        if(nombre.isEmpty()){
            binding.tilNombre.setError("Obligatorio");
            return;
        }
        if(correo.isEmpty()){
            binding.tilCorreo.setError("Obligatorio");
            return;
        }
        if(pass.isEmpty()){
            binding.tilContrasena.setError("Obligatorio");
            return;
        }
        if(rol.isEmpty()){
            binding.tilRol.setError("Selecciona un rol");
            return;
        }
        Integer idSuc=null;
        if(dev){
            String elegido=binding.actSucursal.getText().toString().trim();
            for(Sucursal s:sucursales){
                String n=(s.getEmpresa()==null?"":s.getEmpresa()+" · ")+s.getNombre();
                if(n.equals(elegido)){
                    idSuc=s.getId();
                    break;
                }
            }
            if(idSuc==null){
                binding.tilSucursal.setError("Selecciona una sucursal");
                return;
            }
        }
        viewModel.registrarUsuario(nombre,correo,pass,rol,idSuc);
    }
    private void confirmarEliminar(Usuario u){
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Eliminar usuario").setMessage("¿Eliminar a "+u.getNombre()+" ("+u.getRol()+")? El usuario quedará inactivo y no podrá iniciar sesión.").setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(d,w)->viewModel.eliminarUsuario(u.getId())).show();
    }
    private String txt(android.widget.EditText e){
        return e.getText()==null?"":e.getText().toString().trim();
    }
    private void limpiar(){
        binding.etNombre.setText("");
        binding.etCorreo.setText("");
        binding.etContrasena.setText("");
        if(dev)binding.actSucursal.setText("",false);
        if(admin)binding.actRol.setText("",false);
    }
    @Override public void onDestroyView(){
        super.onDestroyView();binding=null;
    }
}