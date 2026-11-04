package com.example.vitalhome.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

/**
 * Clase utilitaria para la gestión y monitoreo continuo del estado de conectividad a la red.
 * Permite la detección de estado Wi-Fi, Datos Móviles y Ethernet, e implementa un LiveData
 * observador para permitir que los ViewModels manejen la degradación elegante y el modo Offline.
 */
public final class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    private NetworkUtils() {
        // Constructor privado para prevenir instanciación
    }

    /**
     * Comprueba de manera sincrónica si el dispositivo cuenta actualmente con una conexión activa a internet.
     *
     * @param context Contexto de la aplicación.
     * @return true si hay conexión activa a internet, false en caso contrario.
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;

            Network activeNetwork = cm.getActiveNetwork();
            if (activeNetwork == null) return false;
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
            if (capabilities == null) return false;

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                     capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                     capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } catch (Exception e) {
            Log.e(TAG, "Error verificando conectividad de red", e);
            return false;
        }
    }

    /**
     * Observador LiveData reactivo en tiempo real para cambios de estado de conectividad a red.
     */
    public static class NetworkStatusLiveData extends LiveData<Boolean> {

        private final Context appContext;
        private final ConnectivityManager connectivityManager;
        private final ConnectivityManager.NetworkCallback networkCallback;

        public NetworkStatusLiveData(@NonNull Context context) {
            appContext = context.getApplicationContext();
            connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    postValue(true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    postValue(false);
                }
            };
        }

        @Override
        protected void onActive() {
            super.onActive();
            if (connectivityManager != null) {
                try {
                    postValue(NetworkUtils.isNetworkAvailable(appContext));
                    connectivityManager.registerDefaultNetworkCallback(networkCallback);
                } catch (Exception e) {
                    Log.e(TAG, "Error al registrar NetworkCallback", e);
                    postValue(false);
                }
            }
        }

        @Override
        protected void onInactive() {
            super.onInactive();
            if (connectivityManager != null) {
                try {
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                } catch (Exception e) {
                    Log.e(TAG, "Error al desregistrar NetworkCallback", e);
                }
            }
        }
    }
}
