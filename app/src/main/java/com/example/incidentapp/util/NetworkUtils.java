package com.example.incidentapp.util;
import android.content.Context; import android.net.*; import android.os.Build;
public final class NetworkUtils{private NetworkUtils(){} public static boolean conectado(Context c){ConnectivityManager cm=(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);if(cm==null)return false;Network n=cm.getActiveNetwork();if(n==null)return false;NetworkCapabilities cap=cm.getNetworkCapabilities(n);return cap!=null&&cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);} }
