package com.example.vitalhome;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "SOS_CHANNEL";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        createNotificationChannel();

        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        SharedPreferences prefs = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                
                // RE-LEER SharedPreferences aquí para capturar cambios de rol al recargar
                SharedPreferences latestPrefs = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);
                String currentNombre = latestPrefs.getString("nombre", "Usuario Invitado");
                String currentRol = latestPrefs.getString("rol", "cuidador");
                String currentAdulto = latestPrefs.getString("nombreAdultoMayor", "Usuario");

                // Pasamos los datos actualizados a la página
                view.evaluateJavascript("actualizarUsuarioNativo('" + currentNombre + "', '" + currentRol + "', '" + currentAdulto + "');", null);
            }
        });

        webView.loadUrl("file:///android_asset/index.html");

        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void cerrarSesion() {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void cerrarApp() {
                finishAffinity();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void guardarRol(String nuevoRol) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("rol", nuevoRol);
                editor.apply();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void setMedStatus(String medName, boolean taken) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("med_" + medName, taken);
                editor.apply();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public boolean getMedStatus(String medName) {
                return prefs.getBoolean("med_" + medName, false);
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void guardarMeds(String medsJson) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("lista_medicamentos", medsJson);
                editor.apply();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public String cargarMeds() {
                return prefs.getString("lista_medicamentos", "[]");
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void guardarHistorial(String historyJson) {
                // Guardamos el historial de alertas y mediciones
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("historial_alertas", historyJson);
                editor.apply();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public String cargarHistorial() {
                return prefs.getString("historial_alertas", "[]");
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void guardarVitals(String vitalsJson) {
                // Guardamos los últimos signos vitales medidos
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("ultimos_vitals", vitalsJson);
                editor.apply();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public String cargarVitals() {
                return prefs.getString("ultimos_vitals", "{}");
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void dispararNotificacion(String nombre) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                
                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 
                        PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("🚨 ALERTA SOS - VitalHome")
                        .setContentText(nombre + " ha solicitado ayuda urgente.")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000});

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                try {
                    notificationManager.notify(1, builder.build());
                } catch (SecurityException e) {
                    Log.e("MainActivity", "Error al mostrar notificación: " + e.getMessage());
                }
            }
        }, "AndroidApp");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Canal de Emergencias";
            String description = "Alertas de ayuda SOS VitalHome";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}