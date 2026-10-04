package com.example.vitalhome.ui.controladores;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.vitalhome.R;
import com.example.vitalhome.utils.MedicationAlarmScheduler;
import com.example.vitalhome.utils.NotificationHelper;
import com.example.vitalhome.utils.VitalHomeBackgroundService;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Actividad principal de la aplicación VitalHome.
 * Controla la navegación general mediante Jetpack Navigation, la verificación de permisos y la inicialización de alarmas.
 * Escucha las alertas críticas y de tomas de medicamentos en tiempo real entre celulares desde Cloud Firestore sin duplicaciones.
 */
public class MainActivity extends AppCompatActivity {

    private ListenerRegistration globalAlertListener;

    // --- Permisos de notificaciones en Android 13+ ---
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Callback del launcher de permisos
            });

    // --- Inicialización y configuración del NavController ---
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences p = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);
        String rol = p.getString("rol", "cuidador");

        View nav = findViewById(R.id.nav_container);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (host != null) {
            NavController controller = host.getNavController();

            // Configuración de barra de navegación según el rol
            if ("cliente".equals(rol)) {
                if (nav != null) nav.setVisibility(View.GONE);
            } else {
                if (nav != null) nav.setVisibility(View.VISIBLE);
                configurarNav(controller, p);
            }

            // Armado del grafo de navegación
            if (savedInstanceState == null) {
                androidx.navigation.NavGraph graph = controller.getNavInflater().inflate(R.navigation.nav_graph);
                graph.setStartDestination("cliente".equals(rol) ? R.id.nav_dashboard_cliente : R.id.nav_dashboard);
                controller.setGraph(graph);

                // Permisos POST_NOTIFICATIONS en Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    }
                }

                // Reprogramación automática de alarmas al abrir la actividad
                MedicationAlarmScheduler.reprogramarTodas(this);
            }

            // Iniciar la escucha en tiempo real de alertas SOS enviadas por otros celulares
            iniciarEscuchadorAlertasGlobales(rol);

            // Iniciar servicio en segundo plano continuo para el cuidador
            iniciarServicioSegundoPlano(rol);

            // Callback del botón Atrás del sistema
            getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    int startDestId = controller.getGraph().getStartDestinationId();
                    if (controller.getCurrentDestination() != null && controller.getCurrentDestination().getId() == startDestId) {
                        finish();
                    } else {
                        controller.popBackStack(startDestId, false);
                    }
                }
            });
        }
    }

    private void iniciarServicioSegundoPlano(String rol) {
        if ("cuidador".equals(rol)) {
            try {
                Intent serviceIntent = new Intent(this, VitalHomeBackgroundService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            } catch (Exception ignored) {}
        }
    }

    // --- Escuchador de Alertas Críticas en Tiempo Real entre Celulares vía Cloud Firestore ---
    private void iniciarEscuchadorAlertasGlobales(String rol) {
        if (!"cuidador".equals(rol)) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance("default");

        globalAlertListener = db.collection("alerts")
            .whereEqualTo("resuelto", false)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null) return;

                SharedPreferences p = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);
                Set<String> yaNotificadas = new HashSet<>(p.getStringSet("alertas_notificadas_ids", new HashSet<>()));

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        String docId = dc.getDocument().getId();
                        boolean isLocal = dc.getDocument().getMetadata().hasPendingWrites();

                        // Marcar inmediatamente como resuelto en Firestore para descartar duplicados y registros antiguos
                        db.collection("alerts").document(docId).update("resuelto", true);

                        String tipo = dc.getDocument().getString("tipo");
                        String tituloDoc = dc.getDocument().getString("titulo");
                        String desc = dc.getDocument().getString("descripcion");
                        String abuelo = dc.getDocument().getString("pacienteNombre");
                        String abueloNombre = java.util.Objects.requireNonNullElse(abuelo, "Tu familiar");
                        Date ts = dc.getDocument().getDate("timestamp");

                        // Solo mostrar Toast y sonido para emergencias recientes creadas en el último minuto
                        boolean esCritica = "SOS".equalsIgnoreCase(tipo) || "CAIDA".equalsIgnoreCase(tipo) || "FUEGO".equalsIgnoreCase(tipo) || "HOGAR".equalsIgnoreCase(tipo) || "MEDICINA".equalsIgnoreCase(tipo);
                        boolean esReciente = ts != null && (System.currentTimeMillis() - ts.getTime() < 60000);

                        if (!isLocal && !yaNotificadas.contains(docId) && esCritica && esReciente) {
                            yaNotificadas.add(docId);
                            p.edit().putStringSet("alertas_notificadas_ids", yaNotificadas).apply();

                            String tituloFinal;
                            String toastText;
                            String mensajeText;

                            if ("MEDICINA".equalsIgnoreCase(tipo)) {
                                tituloFinal = "✅ Medicamento Tomado";
                                toastText = "✅ Medicamento Tomado";
                                mensajeText = desc != null ? desc : "Medicamento tomado";
                            } else if ("SOS".equalsIgnoreCase(tipo)) {
                                tituloFinal = "🚨 ALERTA SOS";
                                toastText = "🚨 ¡ALERTA SOS DE " + abueloNombre + "!";
                                mensajeText = abueloNombre + ": " + (desc != null ? desc : "Emergencia SOS");
                            } else if ("CAIDA".equalsIgnoreCase(tipo)) {
                                tituloFinal = "⚠️ ¡ALERTA DE CAÍDA!";
                                toastText = "⚠️ ¡CAÍDA DETECTADA DE " + abueloNombre + "!";
                                mensajeText = abueloNombre + ": " + (desc != null ? desc : "Caída detectada");
                            } else if ("HOGAR".equalsIgnoreCase(tipo)) {
                                tituloFinal = "🏠 ¡ALERTA EN EL HOGAR!";
                                toastText = "🏠 ¡ALERTA EN EL HOGAR DE " + abueloNombre + "!";
                                mensajeText = abueloNombre + ": " + (desc != null ? desc : "Evento del hogar");
                            } else {
                                tituloFinal = tituloDoc != null ? tituloDoc : "⚠️ Alerta";
                                toastText = "⚠️ Alerta de " + abueloNombre;
                                mensajeText = abueloNombre + ": " + (desc != null ? desc : "Evento detectado");
                            }

                            NotificationHelper.notificarAlertaCuidadorConId(this, docId, tipo, tituloFinal, mensajeText);
                            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
    }

    // --- Configuración de la barra de navegación inferior ---
    private void configurarNav(NavController controller, SharedPreferences p) {
        findViewById(R.id.btnNavInicio).setOnClickListener(v -> controller.navigate(R.id.nav_dashboard));
        findViewById(R.id.btnNavAlertas).setOnClickListener(v -> controller.navigate(R.id.nav_alertas));

        View btnCambiar = findViewById(R.id.btnNavCambiar);
        if (btnCambiar != null) {
            btnCambiar.setOnClickListener(v -> {
                p.edit().putString("rol", "cliente").apply();
                recargar();
            });
        }

        findViewById(R.id.btnNavSalir).setOnClickListener(v -> mostrarDialogoSalirOCerrarSesion(p));
    }

    // --- Diálogo emergente estilizado para Cerrar Sesión vs Salir de la App ---
    private void mostrarDialogoSalirOCerrarSesion(SharedPreferences p) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_opciones_sesion, null);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        View btnCerrarSesion = dialogView.findViewById(R.id.btnDialogCerrarSesion);
        View btnSalirApp = dialogView.findViewById(R.id.btnDialogSalirApp);
        View btnCancelar = dialogView.findViewById(R.id.btnDialogCancelar);

        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> {
                dialog.dismiss();
                p.edit().putBoolean("isLoggedIn", false).remove("password").apply();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        if (btnSalirApp != null) {
            btnSalirApp.setOnClickListener(v -> finishAffinity());
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    // --- Recarga de la actividad ---
    private void recargar() {
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (globalAlertListener != null) {
            globalAlertListener.remove();
            globalAlertListener = null;
        }
    }
}
