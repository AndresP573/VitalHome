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
        setContentView(R.layout.activity_register);

        EditText inNombre = findViewById(R.id.etNombreRegister);
        EditText inEmail = findViewById(R.id.etEmailRegister);
        EditText inPass = findViewById(R.id.etPasswordRegister);
        RadioGroup selRol = findViewById(R.id.rgRol);
        Button btnOk = findViewById(R.id.btnRegister);
        TextView btnLogin = findViewById(R.id.tvGoToLogin);

        btnOk.setOnClickListener(v -> {
            String n = inNombre.getText().toString().trim();
            String e = inEmail.getText().toString().trim();
            String p = inPass.getText().toString().trim();
            
            String role = (selRol.getCheckedRadioButtonId() == R.id.rbCliente) ? "cliente" : "cuidador";

            if (n.isEmpty() || e.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Completá todo por favor", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            
            // Guardamos el mail para que el login lo recuerde
            edit.putString("email", e);
            if (role.equals("cliente")) {
                edit.putString("email_cliente", e);
            } else {
                edit.putString("email_cuidador", e);
            }
            
            edit.putString("password", p);
            edit.putString("rol", role);
            edit.putBoolean("isLoggedIn", true); 
            
            if (role.equals("cliente")) {
                edit.putString("nombreAdultoMayor", n);
            } else if (!prefs.contains("nombreAdultoMayor")) {
                edit.putString("nombreAdultoMayor", "Mi Familiar");
            }
            
            edit.apply();
            
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}