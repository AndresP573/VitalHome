package com.example.vitalhome;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button buttonSos = findViewById(R.id.buttonSos);
        buttonSos.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AlertActivity.class);
            intent.putExtra("alert_type", "sos_manual");
            intent.putExtra("contacts", "Juan y Ana");
            startActivity(intent);
        });

        findViewById(R.id.cardSensorFall).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        findViewById(R.id.cardSensorWater).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        setupBottomNav();
    }

    private void setupBottomNav() {
        findViewById(R.id.navAlert).setOnClickListener(v ->
                startActivity(new Intent(this, AlertActivity.class)));

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}
