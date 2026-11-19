package com.example.incidentapp.util;
import android.content.Context;
import android.net.Uri;
import java.io.*;
import java.util.*;

public final class OfflineFileUtils {
    private OfflineFileUtils() {}
    public static List<String> copiar(Context context, List<Uri> uris) throws IOException {
        List<String> out = new ArrayList<>();
        File dir = new File(context.getFilesDir(), "pendientes");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("No se pudo crear carpeta offline");
        int n = 0;
        try {
            for (Uri uri : uris) {
                File f = new File(dir, "pendiente_" + System.currentTimeMillis() + "_" + (n++) + ".jpg");
                try (InputStream in = context.getContentResolver().openInputStream(uri);
                     OutputStream os = new FileOutputStream(f)) {
                    if (in == null) throw new IOException("No se pudo leer foto");
                    byte[] b = new byte[8192]; int r;
                    while ((r = in.read(b)) != -1) os.write(b, 0, r);
                }
                out.add(f.getAbsolutePath());
            }
            return out;
        } catch (IOException e) {
            borrar(out);
            throw e;
        }
    }

    public static void borrar(List<String> paths) {
        if (paths == null) return;
        for (String p : paths) if (p != null) {
            File f = new File(p);
            if (f.isFile()) f.delete();
        }
    }
}