package com.example.vitalhome.controladores;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.vitalhome.R;

public class MainActivity extends AppCompatActivity {

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
            androidx.navigation.NavGraph graph = controller.getNavInflater().inflate(R.navigation.nav_graph);
            
            if ("cliente".equals(rol)) {
                if (nav != null) nav.setVisibility(View.GONE);
                graph.setStartDestination(R.id.nav_dashboard_cliente);
            } else {
                if (nav != null) nav.setVisibility(View.VISIBLE);
                graph.setStartDestination(R.id.nav_dashboard);
                configurarNav(controller, p);
            }
            controller.setGraph(graph);

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