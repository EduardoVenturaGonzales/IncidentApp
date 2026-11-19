package com.example.incidentapp.repository;
import com.example.incidentapp.model.EmpresasResponse;
import com.example.incidentapp.model.MensajeResponse;
import com.example.incidentapp.model.RegistroEmpresaRequest;
import com.example.incidentapp.model.RegistroSucursalRequest;
import com.example.incidentapp.model.RegistroUsuarioRequest;
import com.example.incidentapp.model.SucursalesResponse;
import com.example.incidentapp.model.UsuariosResponse;
import com.example.incidentapp.network.RetrofitClient;
import retrofit2.Call;

public class GestionRepository {
    public Call<EmpresasResponse> obtenerEmpresas() {
        return RetrofitClient.getApiService().obtenerEmpresas();
    }
    public Call<MensajeResponse> registrarEmpresa(RegistroEmpresaRequest request) {
        return RetrofitClient.getApiService().registrarEmpresa(request);
    }
    public Call<MensajeResponse> eliminarEmpresa(int idEmpresa) {
        return RetrofitClient.getApiService().eliminarEmpresa(idEmpresa);
    }
    public Call<SucursalesResponse> obtenerSucursales(boolean todas) {
        return RetrofitClient.getApiService().obtenerSucursales(todas ? 1 : null);
    }
    public Call<MensajeResponse> registrarSucursal(RegistroSucursalRequest request) {
        return RetrofitClient.getApiService().registrarSucursal(request);
    }
    public Call<MensajeResponse> eliminarSucursal(int idSucursal) {
        return RetrofitClient.getApiService().eliminarSucursal(idSucursal);
    }
    public Call<UsuariosResponse> obtenerUsuarios() {
        return RetrofitClient.getApiService().obtenerUsuarios();
    }
    public Call<MensajeResponse> registrarUsuario(RegistroUsuarioRequest request) {
        return RetrofitClient.getApiService().registrarUsuario(request);
    }
    public Call<MensajeResponse> eliminarUsuario(int idUsuario) {
        return RetrofitClient.getApiService().eliminarUsuario(idUsuario);
    }
}