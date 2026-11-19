package com.example.incidentapp.ui.incidentes;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.*;
import com.bumptech.glide.load.model.*;
import com.example.incidentapp.R;
import com.example.incidentapp.adapter.HistorialAdapter;
import com.example.incidentapp.databinding.*;
import com.example.incidentapp.model.*;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.util.SlaUtils;
import com.example.incidentapp.viewmodel.DetalleIncidenteViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.*;
import java.util.*;

public class DetalleIncidenteFragment extends Fragment{
 private FragmentDetalleIncidenteBinding binding;
 private DetalleIncidenteViewModel vm;
 private HistorialAdapter hist;
 private int id;
 private Incidente actual;
 private String rol;
 private final Handler timer=new Handler(Looper.getMainLooper());
 private final Runnable tick=new Runnable(){
  public void run(){
   actualizarSla();
   timer.postDelayed(this,1000);
  }
 };
 private final ActivityResultLauncher<ScanOptions> scanner=registerForActivityResult(new ScanContract(),r->{
  if(r.getContents()!=null&&vm!=null)vm.verificarActivo(id,r.getContents().trim());
 });
 @Override public View onCreateView(@NonNull LayoutInflater i,ViewGroup c,Bundle s){
  binding=FragmentDetalleIncidenteBinding.inflate(i,c,false);
  return binding.getRoot();
 }
 @Override public void onViewCreated(@NonNull View v,Bundle s){
  id=getArguments()==null?0:getArguments().getInt("id_incidente",0);
  rol=SessionManager.getInstance().getRol();
  vm=new ViewModelProvider(this).get(DetalleIncidenteViewModel.class);
  hist=new HistorialAdapter();
  binding.recyclerHistorial.setLayoutManager(new LinearLayoutManager(requireContext()));
  binding.recyclerHistorial.setAdapter(hist);
  binding.recyclerHistorial.setNestedScrollingEnabled(false);
  binding.btnVolver.setOnClickListener(x->requireActivity().getOnBackPressedDispatcher().onBackPressed());
  binding.btnCambiarEstado.setOnClickListener(x->dialogEstado());
  binding.btnCambiarPrioridad.setOnClickListener(x->dialogPrioridad());
  binding.btnAsignarTecnico.setOnClickListener(x->vm.cargarTecnicos());
  binding.btnComoLlegar.setOnClickListener(x->comoLlegar());
  binding.btnVerificarActivo.setOnClickListener(x->scan());
  binding.btnSolicitarAnulacion.setOnClickListener(x->dialogAnular());
  binding.btnEliminarIncidente.setOnClickListener(x->dialogEliminar());
  vm.getLoading().observe(getViewLifecycleOwner(),l->binding.progressDetalle.setVisibility(Boolean.TRUE.equals(l)?View.VISIBLE:View.GONE));
  vm.getDetalle().observe(getViewLifecycleOwner(),this::mostrar);
  vm.getMensaje().observe(getViewLifecycleOwner(),e->{
   String m=e.getContentIfNotHandled();
   if(m!=null&&binding!=null)Snackbar.make(binding.getRoot(),m,Snackbar.LENGTH_LONG).show();
  });
  vm.getTecnicos().observe(getViewLifecycleOwner(),e->{
   List<Tecnico> l=e.getContentIfNotHandled();
   if(l!=null)dialogTecnicos(l);
  });
  vm.getVerificacion().observe(getViewLifecycleOwner(),e->{
   VerificarActivoResponse r=e.getContentIfNotHandled();
   if(r!=null)Snackbar.make(binding.getRoot(),r.getMensaje(),Snackbar.LENGTH_LONG).show();
  });
  vm.getEliminado().observe(getViewLifecycleOwner(),e->{
   Boolean ok=e.getContentIfNotHandled();
   if(Boolean.TRUE.equals(ok)&&binding!=null){
    Snackbar sb=Snackbar.make(binding.getRoot(),"Incidente eliminado correctamente",Snackbar.LENGTH_SHORT);
    sb.addCallback(new Snackbar.Callback(){
     public void onDismissed(Snackbar b,int ev){
      if(isAdded())requireActivity().getOnBackPressedDispatcher().onBackPressed();
     }
    });
    sb.show();
   }
  });
  if(id>0)vm.cargarDetalle(id);
  timer.post(tick);
 }
 private void mostrar(DetalleIncidenteResponse d){
  if(d==null||d.getIncidente()==null)return;
  actual=d.getIncidente();
  binding.tvTicket.setText("Incidente #"+actual.getId());
  binding.tvTitulo.setText(actual.getTitulo());
  binding.tvDescripcion.setText(actual.getDescripcion());
  binding.tvEstado.setText(actual.getEstado());
  binding.tvPrioridad.setText(actual.getPrioridad());
  binding.tvCategoria.setText(actual.getCategoria());
  binding.tvSucursal.setText(val(actual.getSucursal()));
  binding.tvDireccion.setText(val(actual.getDireccionSucursal()));
  binding.tvActivo.setText(actual.getActivo()==null?"Sin activo asociado":actual.getActivo());
  binding.tvCodigoQr.setText(actual.getCodigoQr()==null?"-":actual.getCodigoQr());
  binding.tvSolicitante.setText(val(actual.getSolicitante()));
  binding.tvTecnico.setText(actual.getTecnico()==null?"Sin técnico asignado":actual.getTecnico());
  binding.tvFecha.setText(val(actual.getFechaRegistro()));hist.setHistorial(d.getHistorial());
  binding.tvSinHistorial.setVisibility(d.getHistorial()==null||d.getHistorial().isEmpty()?View.VISIBLE:View.GONE);
  fotos(d.getFotos());
  acciones(d);
  actualizarSla();
 }
 private void actualizarSla(){
  if(binding==null||actual==null)return;
  SlaUtils.Estado e=SlaUtils.calcular(actual);
  binding.tvSlaDetalle.setText(e.texto);
  binding.tvSlaDetalle.setTextColor(e.color==SlaUtils.ColorEstado.ROJO?Color.parseColor("#C62828"):e.color==SlaUtils.ColorEstado.AMARILLO?Color.parseColor("#EF6C00"):e.color==SlaUtils.ColorEstado.VERDE?Color.parseColor("#2E7D32"):Color.GRAY);
 }
 private void acciones(DetalleIncidenteResponse d){
  boolean admin="Administrador".equalsIgnoreCase(rol),tec="Tecnico".equalsIgnoreCase(rol),sol="Solicitante".equalsIgnoreCase(rol);
  String est=actual.getEstado();
  boolean finalizado="Resuelto".equalsIgnoreCase(est)||"No Resuelto".equalsIgnoreCase(est)||"Anulado".equalsIgnoreCase(est);
  binding.btnCambiarEstado.setVisibility((admin||tec)&&!finalizado?View.VISIBLE:View.GONE);
  binding.btnCambiarPrioridad.setVisibility(admin&&!finalizado?View.VISIBLE:View.GONE);
  binding.btnAsignarTecnico.setVisibility(admin&&!finalizado?View.VISIBLE:View.GONE);
  binding.btnComoLlegar.setVisibility(tec&&!"Anulado".equalsIgnoreCase(est)?View.VISIBLE:View.GONE);
  binding.btnVerificarActivo.setVisibility(tec&&!finalizado&&actual.getIdActivo()!=null?View.VISIBLE:View.GONE);
  binding.btnEliminarIncidente.setVisibility(admin?View.VISIBLE:View.GONE);
  SolicitudAnulacion a=d.getSolicitudAnulacion();
  boolean pen=a!=null&&"Pendiente".equalsIgnoreCase(a.getEstado());
  binding.btnSolicitarAnulacion.setVisibility(sol&&!finalizado&&!pen?View.VISIBLE:View.GONE);
  if(sol&&a!=null){
   binding.tvSolicitudAnulacion.setVisibility(View.VISIBLE);
   binding.tvSolicitudAnulacion.setText("Solicitud de anulación: "+a.getEstado());
  }else binding.tvSolicitudAnulacion.setVisibility(View.GONE);
 }
 private void dialogEstado(){
  if(actual==null)return;
  String est=actual.getEstado();List<String> opciones=new ArrayList<>();
  if("Tecnico".equalsIgnoreCase(rol)){
   if("Abierto".equalsIgnoreCase(est)){
    opciones.add("En proceso");
    opciones.add("Resuelto");
   }else if("En proceso".equalsIgnoreCase(est))opciones.add("Resuelto");
  }else if("Administrador".equalsIgnoreCase(rol)){
   if("Abierto".equalsIgnoreCase(est)){
    opciones.add("En proceso");
    opciones.add("Resuelto");
   }else if("En proceso".equalsIgnoreCase(est))opciones.add("Resuelto");
   else if("No Resuelto".equalsIgnoreCase(est))opciones.add("Abierto");
  }if(opciones.isEmpty()){
   Snackbar.make(binding.getRoot(),"No hay cambios de estado disponibles",Snackbar.LENGTH_SHORT).show();
   return;
  }
  DialogCambiarEstadoBinding d=DialogCambiarEstadoBinding.inflate(getLayoutInflater());
  d.actEstado.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_dropdown_item_1line,opciones));
  d.actEstado.setText(opciones.get(0),false);
  androidx.appcompat.app.AlertDialog dlg=new MaterialAlertDialogBuilder(requireContext()).setTitle("Cambiar estado").setView(d.getRoot()).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",null).create();
  dlg.setOnShowListener(x->dlg.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v->{
   String n=d.actEstado.getText().toString().trim(),c=d.etComentario.getText()==null?"":d.etComentario.getText().toString().trim();
   if(c.isEmpty()){
    d.tilComentario.setError("El comentario es obligatorio");
    return;
   }
   vm.cambiarEstado(id,n,c);
   dlg.dismiss();
  }));
  dlg.show();
 }
 private void dialogPrioridad(){
  String[] ps={"Alta","Media","Baja"};
  final int[] sel={Math.max(0,Arrays.asList(ps).indexOf(actual.getPrioridad()))};
  EditText c=new EditText(requireContext());
  c.setHint("Comentario (opcional)");
  new MaterialAlertDialogBuilder(requireContext()).setTitle("Cambiar prioridad").setSingleChoiceItems(ps,sel[0],(d,w)->sel[0]=w).setView(c).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(d,w)->vm.cambiarPrioridad(id,ps[sel[0]],c.getText().toString().trim())).show();
 }
 private void scan(){
  ScanOptions o=new ScanOptions();
  o.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
  o.setPrompt("Escanea el QR del equipo que estás atendiendo");
  o.setBeepEnabled(true);
  o.setOrientationLocked(false);
  scanner.launch(o);
 }
 private void dialogAnular(){
  EditText m=new EditText(requireContext());
  m.setHint("Motivo de anulación");
  m.setMinLines(3);
  androidx.appcompat.app.AlertDialog d=new MaterialAlertDialogBuilder(requireContext()).setTitle("Solicitar anulación").setMessage("Explica por qué deseas anular este incidente.").setView(m).setNegativeButton("Cancelar",null).setPositiveButton("Solicitar",null).create();
  d.setOnShowListener(x->d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v->{
   String t=m.getText().toString().trim();if(t.isEmpty()){m.setError("El motivo es obligatorio");
    return;
   }
   vm.solicitarAnulacion(id,t);
   d.dismiss();
  }));
  d.show();
 }
 private void dialogEliminar(){
  EditText m=new EditText(requireContext());
  m.setHint("Motivo de eliminación (opcional)");
  new MaterialAlertDialogBuilder(requireContext()).setTitle("Eliminar incidente").setMessage("Esta acción eliminará definitivamente el incidente, su historial y fotografías.").setView(m).setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(d,w)->vm.eliminarIncidente(id,m.getText().toString().trim())).show();
 }
 private void dialogTecnicos(List<Tecnico> ts){
  if(ts.isEmpty()){
   Snackbar.make(binding.getRoot(),"No hay técnicos disponibles",Snackbar.LENGTH_SHORT).show();
   return;
  }
  String[] ns=new String[ts.size()];
  for(int i=0;i<ts.size();i++)ns[i]=ts.get(i).getNombre();
  final int[] s={0};
  new MaterialAlertDialogBuilder(requireContext()).setTitle("Asignar técnico").setSingleChoiceItems(ns,0,(d,w)->s[0]=w).setNegativeButton("Cancelar",null).setPositiveButton("Asignar",(d,w)->vm.asignarTecnico(id,ts.get(s[0]).getId())).show();
 }
 private void fotos(List<FotoIncidente> fs){
  binding.contenedorFotos.removeAllViews();
  if(fs==null||fs.isEmpty()){
   binding.tvSinFotos.setVisibility(View.VISIBLE);
   return;
  }
  binding.tvSinFotos.setVisibility(View.GONE);
  for(FotoIncidente f:fs){
   ImageView im=new ImageView(requireContext());
   LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(150),dp(115));
   p.setMarginEnd(dp(12));
   im.setLayoutParams(p);
   im.setScaleType(ImageView.ScaleType.CENTER_CROP);
   GlideUrl u=url(f.getUrl());
   Glide.with(this).load(u).centerCrop().into(im);
   im.setOnClickListener(v->fotoGrande(u));
   binding.contenedorFotos.addView(im);
  }
 }
 private GlideUrl url(String u){
  return new GlideUrl(u,new LazyHeaders.Builder().addHeader("Authorization","JWT "+SessionManager.getInstance().getToken()).build());
 }
 private void fotoGrande(GlideUrl u){
  ImageView i=new ImageView(requireContext());
  i.setAdjustViewBounds(true);
  Glide.with(this).load(u).fitCenter().into(i);
  new MaterialAlertDialogBuilder(requireContext()).setView(i).setPositiveButton("Cerrar",null).show();
 }
 private void comoLlegar(){
  if(actual==null||actual.getLatitud()==null||actual.getLongitud()==null){
   Snackbar.make(binding.getRoot(),"La sucursal no tiene coordenadas",Snackbar.LENGTH_LONG).show();
   return;
  }
  double la=actual.getLatitud(),lo=actual.getLongitud();
  Intent in=new Intent(Intent.ACTION_VIEW,Uri.parse("google.navigation:q="+la+","+lo));
  in.setPackage("com.google.android.apps.maps");
  if(in.resolveActivity(requireContext().getPackageManager())!=null)startActivity(in);
  else startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/dir/?api=1&destination="+la+","+lo)));
 }
 private String val(String v){
  return v==null||v.trim().isEmpty()?"-":v;
 }
 private int dp(int v){
  return Math.round(v*getResources().getDisplayMetrics().density);
 }
 @Override public void onDestroyView(){
  super.onDestroyView();
  timer.removeCallbacks(tick);
  binding=null;
 }
}