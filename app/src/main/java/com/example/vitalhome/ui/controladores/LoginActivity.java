package com.example.vitalhome.ui.controladores;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.vitalhome.R;
import com.example.vitalhome.data.local.AppDatabase;
import com.example.vitalhome.data.local.UserEntity;
import com.example.vitalhome.utils.AppExecutors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Actividad responsable de gestionar el inicio de sesión de los usuarios (Cliente o Cuidador).
 * Implementa autenticación híbrida consultando Room (SQLite), Firestore ("default")
 * y recuperación directa de contraseña por correo vía Firebase Authentication.
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously();
        }

        SharedPreferences p = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);

        // Si es el primer uso de la app en este celular y no hay registro previo -> ir al registro paso a paso
        if (!p.getBoolean("isRegistered", false)) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        // Si la sesión está activa -> ir a la pantalla principal
        if (p.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        EditText inMail = findViewById(R.id.etEmailLogin);
        EditText inPass = findViewById(R.id.etPasswordLogin);
        EditText inPhone = findViewById(R.id.etPhoneLogin);
        RadioGroup selPerfil = findViewById(R.id.rgLoginRole);
        Button btnEntrar = findViewById(R.id.btnLogin);
        TextView tvForgot = findViewById(R.id.tvForgotPassword);
        TextView btnGoReg = findViewById(R.id.tvGoToRegister);

        // Carga inicial de datos según rol seleccionado
        int actualId = selPerfil.getCheckedRadioButtonId();
        String k = (actualId == R.id.rbLoginAdulto) ? "email_cliente" : "email_cuidador";
        inMail.setText(p.getString(k, p.getString("email", "")));
        if (inPhone != null) {
            inPhone.setText(p.getString("phone_cuidador", ""));
            inPhone.setVisibility((actualId == R.id.rbLoginCuidador) ? View.VISIBLE : View.GONE);
        }

        selPerfil.setOnCheckedChangeListener((group, id) -> {
            String key = (id == R.id.rbLoginAdulto) ? "email_cliente" : "email_cuidador";
            String m = p.getString(key, p.getString("email", ""));
            inMail.setText(m);
            if (inPhone != null) {
                inPhone.setVisibility((id == R.id.rbLoginCuidador) ? View.VISIBLE : View.GONE);
            }
        });

        // --- Opción de Recuperación de Contraseña Real por Correo ---
        if (tvForgot != null) {
            tvForgot.setOnClickListener(v -> mostrarDialogoRecuperarPassword());
        }

        // --- Validación e inicio de sesión híbrido (Room + Firestore "default") ---
        btnEntrar.setOnClickListener(v -> {
            String mail = inMail.getText().toString().trim();
            String pass = inPass.getText().toString().trim();
            String tel = (inPhone != null) ? inPhone.getText().toString().trim() : "";
            String selectedRole = (selPerfil.getCheckedRadioButtonId() == R.id.rbLoginAdulto) ? "cliente" : "cuidador";

            if (mail.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, getString(R.string.msg_completa_datos), Toast.LENGTH_SHORT).show();
                return;
            }

            btnEntrar.setEnabled(false);

            // Intentar inicio de sesión con Firebase Auth si aplica
            try {
                auth.signInWithEmailAndPassword(mail, pass);
            } catch (Exception ignored) {}

            // 1. Consultar la base de datos local SQLite (Room) en hilo secundario
            AppExecutors.getInstance().diskIO().execute(() -> {
                UserEntity localUser = AppDatabase.getInstance(getApplicationContext())
                        .userDao().getUserByEmail(mail);

                AppExecutors.getInstance().mainThread().execute(() -> {
                    btnEntrar.setEnabled(true);

                    // Si existe el usuario en la base de datos local
                    if (localUser != null) {
                        if (pass.equals(localUser.password) || pass.equals(p.getString("password", ""))) {
                            iniciarSesionExitosa(p, mail, pass, localUser.rol != null ? localUser.rol : selectedRole, tel);
                        } else {
                            Toast.makeText(this, getString(R.string.msg_pass_incorrecta), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // 2. Si no está localmente, verificar en la nube (Firestore)
                        FirebaseFirestore db = FirebaseFirestore.getInstance("default");
                        String docId = mail.replace(".", "_");

                        db.collection("users").document(docId).get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        // Guardar en Room para acceso offline futuro
                                        UserEntity newUser = new UserEntity();
                                        newUser.email = mail;
                                        newUser.password = pass;
                                        newUser.nombreCompleto = doc.getString("nombre");
                                        newUser.rol = doc.getString("rol") != null ? doc.getString("rol") : selectedRole;
                                        newUser.telefono = doc.getString("telefono");
                                        newUser.createdAt = System.currentTimeMillis();

                                        AppExecutors.getInstance().diskIO().execute(() -> {
                                            AppDatabase.getInstance(getApplicationContext()).userDao().insert(newUser);
                                        });

                                        iniciarSesionExitosa(p, mail, pass, newUser.rol, tel);
                                    } else {
                                        // Verificación fallback con SharedPreferences
                                        String passOk = p.getString("password", "");
                                        if (!passOk.isEmpty() && java.util.Objects.equals(pass, passOk)) {
                                            iniciarSesionExitosa(p, mail, pass, selectedRole, tel);
                                        } else {
                                            Toast.makeText(this, getString(R.string.msg_pass_incorrecta), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(err -> {
                                    String passOk = p.getString("password", "");
                                    if (!passOk.isEmpty() && pass.equals(passOk)) {
                                        iniciarSesionExitosa(p, mail, pass, selectedRole, tel);
                                    } else {
                                        Toast.makeText(this, getString(R.string.msg_pass_incorrecta), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            });
        });

        // --- Navegación hacia la pantalla de registro permitiendo múltiples cuentas ---
        btnGoReg.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra("forceNewRegister", true);
            startActivity(intent);
        });
    }

    private void mostrarDialogoRecuperarPassword() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_recuperar_password, null);
        EditText etMailRecovery = dialogView.findViewById(R.id.etMailRecovery);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        View btnSubmit = dialogView.findViewById(R.id.btnSubmitRecovery);
        View btnCancel = dialogView.findViewById(R.id.btnCancelRecovery);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String mail = etMailRecovery != null ? etMailRecovery.getText().toString().trim() : "";
                if (mail.isEmpty()) {
                    Toast.makeText(this, "Por favor ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();

                // Ejecución directa de sendPasswordResetEmail en Firebase Auth
                FirebaseAuth.getInstance().sendPasswordResetEmail(mail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "✉️ Correo enviado a " + mail + ". Revise su casilla o Spam.", Toast.LENGTH_LONG).show();
                        } else {
                            String err = task.getException() != null ? task.getException().getMessage() : "Error al enviar correo";
                            Toast.makeText(this, "⚠️ " + err, Toast.LENGTH_LONG).show();
                        }
                    });
            });
        }

        dialog.show();
    }

    private void iniciarSesionExitosa(SharedPreferences p, String mail, String pass, String role, String tel) {
        SharedPreferences.Editor edit = p.edit();
        edit.putBoolean("isRegistered", true);
        edit.putBoolean("isLoggedIn", true);
        edit.putString("rol", role);
        edit.putString("email", mail);
        edit.putString("password", pass);

        if ("cliente".equals(role)) {
            edit.putString("email_cliente", mail);
        } else {
            edit.putString("email_cuidador", mail);
            edit.putString("phone_cuidador", tel);
        }

        edit.apply();

        Toast.makeText(this, "Bienvenido/a " + mail, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
}
