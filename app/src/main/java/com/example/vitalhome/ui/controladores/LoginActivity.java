package com.example.vitalhome.ui.controladores;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.vitalhome.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences p = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);
        EditText inMail = findViewById(R.id.etEmailLogin);
        EditText inPass = findViewById(R.id.etPasswordLogin);
        RadioGroup selPerfil = findViewById(R.id.rgLoginRole);
        Button btnEntrar = findViewById(R.id.btnLogin);
        TextView btnGoReg = findViewById(R.id.tvGoToRegister);

        // Carga inicial
        int actualId = selPerfil.getCheckedRadioButtonId();
        String k = (actualId == R.id.rbLoginAdulto) ? "email_cliente" : "email_cuidador";
        inMail.setText(p.getString(k, p.getString("email", "")));

        // Listener para cambiar el mail apenas se toca un perfil
        selPerfil.setOnCheckedChangeListener((group, id) -> {
            String key = (id == R.id.rbLoginAdulto) ? "email_cliente" : "email_cuidador";
            String m = p.getString(key, p.getString("email", ""));
            inMail.setText(m);
        });

        btnEntrar.setOnClickListener(v -> {
            String mail = inMail.getText().toString().trim();
            String pass = inPass.getText().toString().trim();
            
            String role = (selPerfil.getCheckedRadioButtonId() == R.id.rbLoginAdulto) ? "cliente" : "cuidador";
            String passOk = p.getString("password", "");

            if (mail.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completá los datos che", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.equals(passOk)) {
                SharedPreferences.Editor edit = p.edit();
                edit.putBoolean("isLoggedIn", true);
                edit.putString("rol", role);
                edit.putString("email", mail);
                edit.putString(role.equals("cliente") ? "email_cliente" : "email_cuidador", mail);
                edit.apply();
                
                startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            } else {
                Toast.makeText(this, "Pass incorrecta", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoReg.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}