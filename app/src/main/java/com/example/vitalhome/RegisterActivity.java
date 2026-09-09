package com.example.vitalhome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Le decimos a Java que use el diseño que creaste recién
        setContentView(R.layout.activity_register);

        // Enlazamos las variables de Java con los IDs del XML
        EditText etNombre = findViewById(R.id.etNombreRegister);
        EditText etEmail = findViewById(R.id.etEmailRegister);
        EditText etPassword = findViewById(R.id.etPasswordRegister);
        RadioGroup rgRol = findViewById(R.id.rgRol);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Acción para el botón "Registrarse"
        btnRegister.setOnClickListener(v -> {
            // Obtenemos el texto que escribió el usuario
            String nombre = etNombre.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            
            String rol;
            if (rgRol.getCheckedRadioButtonId() == R.id.rbCliente) {
                rol = "cliente";
            } else {
                rol = "cuidador";
            }

            // Validamos que no haya dejado campos vacíos
            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                // Usamos SharedPreferences para guardar los datos en el celular como si fuera una mini base de datos
                SharedPreferences prefs = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("nombre", nombre);
                editor.putString("email", email);
                editor.putString("password", password);
                editor.putString("rol", rol);
                editor.putBoolean("isLoggedIn", true); // Auto-login al registrarse
                
                // Algoritmo para guardar el nombre del Adulto Mayor en la Pulsera
                switch (rol) {
                    case "cliente":
                        editor.putString("nombreAdultoMayor", nombre);
                        break;
                    case "cuidador":
                    default:
                        // Si es cuidador, podemos poner un nombre por defecto si no hay uno guardado
                        if (!prefs.contains("nombreAdultoMayor")) {
                            editor.putString("nombreAdultoMayor", "Mi Familiar");
                        }
                        break;
                }
                
                editor.apply();

                Toast.makeText(RegisterActivity.this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();

                // Navegamos directamente al Dashboard (MainActivity)
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Acción para el texto "¿Ya tenés cuenta?"
        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}