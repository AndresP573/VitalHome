package com.example.vitalhome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

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
            
            if ("cliente".equals(rol)) {
                if (nav != null) nav.setVisibility(View.GONE);
                controller.setGraph(R.navigation.nav_graph);
                controller.navigate(R.id.nav_dashboard_cliente);
            } else {
                if (nav != null) nav.setVisibility(View.VISIBLE);
                configurarNav(controller, p);
            }
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
            finish();
        });
    }

    private void recargar() {
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}