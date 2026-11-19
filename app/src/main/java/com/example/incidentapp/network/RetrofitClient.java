package com.example.incidentapp.network;

import com.example.incidentapp.session.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {
    public static final String BASE_URL = "https://incidentappg1.pythonanywhere.com/";
    private static ApiService apiService;
    private RetrofitClient() {}

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // Evita imprimir JWT, contraseñas, cuerpos de incidentes y fotos en Logcat.
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        okhttp3.Request.Builder builder = chain.request().newBuilder();
                        String token = SessionManager.getInstance().getToken();
                        if (token != null && !token.trim().isEmpty()) builder.header("Authorization", "JWT " + token);
                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(logging)
                    .build();

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
