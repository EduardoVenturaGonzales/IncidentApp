package com.example.incidentapp.network;

import com.example.incidentapp.session.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    /*
     * Emulador Android -> PC local: 10.0.2.2
     * Al publicar el backend en PythonAnywhere cambia solo esta URL.
     */
    public static final String BASE_URL = "http://10.1.220.178:5000/";

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
