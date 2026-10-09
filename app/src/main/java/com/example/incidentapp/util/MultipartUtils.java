package com.example.incidentapp.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class MultipartUtils {

    private MultipartUtils() {
    }

    public static MultipartBody.Part crearParteFoto(Context context, Uri uri) throws IOException {
        String nombre = obtenerNombre(context, uri);
        String tipo = context.getContentResolver().getType(uri);
        if (tipo == null || tipo.trim().isEmpty()) {
            tipo = "image/*";
        }

        byte[] bytes;
        try (InputStream input = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            if (input == null) {
                throw new IOException("No se pudo abrir la imagen");
            }

            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = input.read(buffer)) != -1) {
                output.write(buffer, 0, leidos);
            }
            bytes = output.toByteArray();
        }

        RequestBody cuerpo = RequestBody.create(MediaType.parse(tipo), bytes);
        return MultipartBody.Part.createFormData("fotos", nombre, cuerpo);
    }

    private static String obtenerNombre(Context context, Uri uri) {
        String nombre = "foto_incidente.jpg";

        try (Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (indice >= 0) {
                    String resultado = cursor.getString(indice);
                    if (resultado != null && !resultado.trim().isEmpty()) {
                        nombre = resultado;
                    }
                }
            }
        }

        return nombre;
    }
}
