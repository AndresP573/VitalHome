package com.example.vitalhome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.Objects;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // REVISAR SI YA INICIÓ SESIÓN ANTES
        SharedPreferences prefs = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Saltamos directamente al Dashboard
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        // Le indicamos que use el diseño XML del login
        setContentView(R.layout.activity_login);

        // Enlazamos las variables con los IDs del XML
        EditText etEmail = findViewById(R.id.etEmailLogin);
        EditText etPassword = findViewById(R.id.etPasswordLogin);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        // Pre-rellenar el email si existe
        String savedEmail = prefs.getString("email", "");
        if (!savedEmail.isEmpty()) {
            etEmail.setText(savedEmail);
        }

        // Configuración de Biometría
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Error de autenticación: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Si la huella es correcta, iniciamos sesión
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.apply();

                Toast.makeText(getApplicationContext(),
                        "¡Acceso concedido!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Huella no reconocida",
                        Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Acceso Biométrico")
                .setSubtitle("Usa tu huella para entrar a VitalHome")
                .setNegativeButtonText("Usar contraseña")
                .build();

        // Autenticación automática si ya está registrado
        if (!savedEmail.isEmpty()) {
            biometricPrompt.authenticate(promptInfo);
        }

        // Acción para el botón de Iniciar Sesión
        btnLogin.setOnClickListener(v -> {
            String emailInput = etEmail.getText().toString().trim();
            String passInput = etPassword.getText().toString().trim();

            // Abrimos SharedPreferences para leer los datos que guardamos en el Registro
            String savedPass = prefs.getString("password", "");
            String registeredEmail = prefs.getString("email", "");

            // Validamos las credenciales
            if (emailInput.isEmpty() || passInput.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Ingresá tus datos", Toast.LENGTH_SHORT).show();
            } else if (Objects.equals(emailInput, registeredEmail) && Objects.equals(passInput, savedPass)) {
                // Guardamos que el usuario ya inició sesión
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.apply();

                Toast.makeText(LoginActivity.this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show();

                // Login exitoso: saltamos a la MainActivity (donde está tu WebView con el HTML)
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                // Cerramos esta pantalla para que el usuario no vuelva al login si toca el botón de "Atrás"
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }
        });

        // Acción para el texto "¿No tenés cuenta?"
        tvGoToRegister.setOnClickListener(v -> {
            // Navegamos a la pantalla de Registro
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
