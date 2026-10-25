package com.example.incidentapp.network;

import com.example.incidentapp.session.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    // Dominio público de la API Flask en PythonAnywhere.
    // Retrofit exige que la URL base termine en /.
    public static final String BASE_URL = "https://incidentappg1.pythonanywhere.com/";

    private static ApiService apiService;

    private RetrofitClient() {
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        okhttp3.Request original = chain.request();
                        okhttp3.Request.Builder builder = original.newBuilder();

                        String token = SessionManager.getInstance().getToken();
                        if (token != null && !token.trim().isEmpty()) {
                            builder.header("Authorization", "JWT " + token);
                        }

                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(logging)
                    .build();

            // Igual que en clase: Retrofit.Builder + GsonConverterFactory.
            // Aquí agregamos el cliente OkHttp porque el caso práctico exige
            // enviar automáticamente el JWT en las peticiones protegidas.
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }

        return apiService;
    }
}
