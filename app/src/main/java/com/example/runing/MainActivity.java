package com.example.runing;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            mostrarFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                mostrarFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_salud) {
                mostrarFragment(new HealthFragment());
                return true;
            } else if (id == R.id.nav_control) {
                mostrarFragment(new ControlFragment());
                return true;
            } else if (id == R.id.nav_alertas) {
                return true;
            }
            return false;
        });
    }

    private void mostrarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void mostrarSalud() {
        bottomNav.setSelectedItemId(R.id.nav_salud);
    }

    public void mostrarCamaras() {
        // No cambia la pestaña de abajo: queda "Inicio" resaltado,
        // solo se apila la pantalla de Cámaras arriba.
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new CamarasFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}