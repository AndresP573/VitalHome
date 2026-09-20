package com.example.vitalhome.ui.controladores;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.vitalhome.R;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Callback de manejo del permiso (se puede dejar vacío o logs internos)
            });

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
            
            // 1. La visibilidad de la barra de navegación y configurarNav se ejecutan SIEMPRE
            if ("cliente".equals(rol)) {
                if (nav != null) nav.setVisibility(View.GONE);
            } else {
                if (nav != null) nav.setVisibility(View.VISIBLE);
                configurarNav(controller, p);
            }

            // 2. El armado del grafo se ejecuta SOLO cuando savedInstanceState == null
            if (savedInstanceState == null) {
                androidx.navigation.NavGraph graph = controller.getNavInflater().inflate(R.navigation.nav_graph);
                graph.setStartDestination("cliente".equals(rol) ? R.id.nav_dashboard_cliente : R.id.nav_dashboard);
                controller.setGraph(graph);

                // Solicitar permiso POST_NOTIFICATIONS en Android 13+ si no está concedido
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    }
                }
            }

            // 3. El OnBackPressedCallback se queda igual
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

    private void configurarNav(NavController controller, SharedPreferences p) {
        findViewById(R.id.btnNavInicio).setOnClickListener(v -> controller.navigate(R.id.nav_dashboard));
        findViewById(R.id.btnNavAlertas).setOnClickListener(v -> controller.navigate(R.id.nav_alertas));
        
        findViewById(R.id.btnNavCambiar).setOnClickListener(v -> {
            p.edit().putString("rol", "cliente").apply();
            recargar();
        });

        findViewById(R.id.btnNavSalir).setOnClickListener(v -> {
            p.edit().putBoolean("isLoggedIn", false).apply();
            finishAffinity();
            System.exit(0);
        });
    }

    private void recargar() {
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
}
