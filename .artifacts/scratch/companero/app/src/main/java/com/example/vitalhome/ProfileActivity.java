package com.example.vitalhome;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SeekBar seekBarSensitivity = findViewById(R.id.seekBarSensitivity);
        seekBarSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // progress va de 0 a 4 (5 niveles)
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int level = seekBar.getProgress() + 1;
                Toast.makeText(ProfileActivity.this,
                        "Sensibilidad ajustada a nivel " + level, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnBackToHome).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnViewHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        setupBottomNav();
    }

    private void setupBottomNav() {
        findViewById(R.id.navAlert).setOnClickListener(v ->
                startActivity(new Intent(this, AlertActivity.class)));

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            // Ya estamos en Perfil
        });
    }
}