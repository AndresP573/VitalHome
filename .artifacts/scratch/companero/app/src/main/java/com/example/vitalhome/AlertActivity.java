package com.example.vitalhome;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AlertActivity extends AppCompatActivity {

    private static final long COUNTDOWN_MS = 8000;
    private CountDownTimer countDownTimer;
    private boolean cancelled = false;

    private TextView textCountdown;
    private TextView textNotifyingContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        textCountdown = findViewById(R.id.textCountdown);
        textNotifyingContacts = findViewById(R.id.textNotifyingContacts);
        
        String contacts = getIntent().getStringExtra("contacts");
        if (contacts != null) {
            textNotifyingContacts.setText("Avisando a: " + contacts);
        }

        Button buttonImOk = findViewById(R.id.buttonImOk);
        Button buttonNeedHelp = findViewById(R.id.buttonNeedHelp);
        Button buttonViewProfile = findViewById(R.id.buttonViewProfile);

        vibrate();
        startCountdown();

        buttonImOk.setOnClickListener(v -> {
            cancelled = true;
            countDownTimer.cancel();
            textCountdown.setText("Alerta cancelada");
            finish();
        });

        buttonNeedHelp.setOnClickListener(v -> {
            countDownTimer.cancel();
            confirmAlertNow();
        });

        buttonViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AlertActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        setupBottomNav();
    }

    private void setupBottomNav() {
        findViewById(R.id.navAlert).setOnClickListener(v -> {
            // Ya estamos en Alerta
        });

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(COUNTDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000;
                textCountdown.setText("Notificando en " + secondsLeft + "s");
            }

            @Override
            public void onFinish() {
                if (!cancelled) {
                    confirmAlertNow();
                }
            }
        };
        countDownTimer.start();
    }

    private void confirmAlertNow() {
        textCountdown.setText("Contactos notificados");
        // TODO: acá dispararías la llamada real a tu backend / API de notificaciones
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}