package com.example.vitalhome;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final String CHANNEL_ID = "SOS_CHANNEL";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        createNotificationChannel();

        // Usamos el layout que tiene el WebView
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        // Configuramos el WebView para que permita ejecutar tu código JS
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); // Fundamental para que funcionen bien las vistas web modernas
        webSettings.setAllowFileAccess(true);   // Permite acceder a archivos locales si fuera necesario
        webSettings.setAllowContentAccess(true);

        // Recuperamos los datos guardados
        SharedPreferences prefs = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombre", "Usuario Invitado");
        String rolUsuario = prefs.getString("rol", "cuidador");
        String nombreAdultoMayor = prefs.getString("nombreAdultoMayor", "Usuario");

        // Configuramos qué pasa cuando la página web termina de cargar
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Pasamos: Nombre del logueado, Rol y Nombre del dueño de la pulsera
                view.evaluateJavascript("actualizarUsuarioNativo('" + nombreUsuario + "', '" + rolUsuario + "', '" + nombreAdultoMayor + "');", null);
            }
        });

        // Finalmente, cargamos tu archivo HTML (asegurate de que esté en la carpeta assets)
        webView.loadUrl("file:///android_asset/index.html");

        // Puente para el botón de Salir
        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void cerrarSesion() {
                // Borramos la sesión de SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                // Volvemos al Login y cerramos esta pantalla
                android.content.Intent intent = new android.content.Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @android.webkit.JavascriptInterface
            public void cerrarApp() {
                // Simplemente cerramos la aplicación sin borrar la sesión
                finishAffinity();
            }

            @android.webkit.JavascriptInterface
            public void guardarRol(String nuevoRol) {
                // Guardamos el cambio de rol permanentemente
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("rol", nuevoRol);
                editor.apply();
            }

            @android.webkit.JavascriptInterface
            public void setMedStatus(String medName, boolean taken) {
                // Guardamos si una pastilla fue tomada
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("med_" + medName, taken);
                editor.apply();
            }

            @android.webkit.JavascriptInterface
            public boolean getMedStatus(String medName) {
                return prefs.getBoolean("med_" + medName, false);
            }

            @android.webkit.JavascriptInterface
            public void guardarMeds(String medsJson) {
                // Guardamos la lista completa de medicamentos (JSON)
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("lista_medicamentos", medsJson);
                editor.apply();
            }

            @android.webkit.JavascriptInterface
            public String cargarMeds() {
                // Devolvemos la lista guardada o una vacía si no hay nada
                return prefs.getString("lista_medicamentos", "[]");
            }

            @android.webkit.JavascriptInterface
            public void dispararNotificacion(String nombre) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("🚨 ALERTA SOS - VitalHome")
                        .setContentText(nombre + " ha solicitado ayuda urgente.")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000});

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                // Verificamos permiso (simplificado para prototipo)
                try {
                    notificationManager.notify(1, builder.build());
                } catch (SecurityException e) {
                    e.printStackTrace();
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
            notificationManager.createNotificationChannel(channel);
        }
    }
}