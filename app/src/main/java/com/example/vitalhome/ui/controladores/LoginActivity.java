package com.example.vitalhome.ui.controladores;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.vitalhome.R;
import com.example.vitalhome.ui.viewmodels.AppViewModelFactory;
import com.example.vitalhome.ui.viewmodels.LoginViewModel;
import com.example.vitalhome.utils.SecurityUtils;

/**
 * Actividad responsable de la interfaz de usuario para el inicio de sesión.
 * Sigue la arquitectura MVVM estricta, delegando toda la lógica de negocio y persistencia
 * a LoginViewModel.
 */
public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private ProgressBar pbLogin;
    private Button btnEntrar;
    private EditText inMail;
    private EditText inPass;
    private EditText inPhone;
    private RadioGroup selPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppViewModelFactory factory = new AppViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);

        // Verificaciones de estado previo de sesión
        if (!viewModel.isRegistered()) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        if (viewModel.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        inMail = findViewById(R.id.etEmailLogin);
        inPass = findViewById(R.id.etPasswordLogin);
        inPhone = findViewById(R.id.etPhoneLogin);
        selPerfil = findViewById(R.id.rgLoginRole);
        btnEntrar = findViewById(R.id.btnLogin);
        pbLogin = findViewById(R.id.pbLogin);
        TextView tvForgot = findViewById(R.id.tvForgotPassword);
        TextView btnGoReg = findViewById(R.id.tvGoToRegister);

        // Carga inicial de datos según rol seleccionado
        int actualId = selPerfil.getCheckedRadioButtonId();
        String roleKey = (actualId == R.id.rbLoginAdulto) ? "email_cliente" : "email_cuidador";
        inMail.setText(viewModel.getSavedEmail(roleKey));
        if (inPhone != null) {
            inPhone.setText(viewModel.getSavedPhone());
            inPhone.setVisibility((actualId == R.id.rbLoginCuidador) ? View.VISIBLE : View.GONE);
        }

        selPerfil.setOnCheckedChangeListener((group, id) -> {
            String key = (id == R.id.rbLoginAdulto) ? "email_cliente" : "email_cuidador";
            inMail.setText(viewModel.getSavedEmail(key));
            if (inPhone != null) {
                inPhone.setVisibility((id == R.id.rbLoginCuidador) ? View.VISIBLE : View.GONE);
            }
        });

        if (tvForgot != null) {
            tvForgot.setOnClickListener(v -> mostrarDialogoRecuperarPassword());
        }

        btnEntrar.setOnClickListener(v -> {
            String mail = SecurityUtils.sanitizeString(inMail.getText().toString());
            String pass = inPass != null ? inPass.getText().toString().trim() : "";
            String tel = inPhone != null ? SecurityUtils.sanitizePhone(inPhone.getText().toString()) : "";
            String selectedRole = (selPerfil.getCheckedRadioButtonId() == R.id.rbLoginAdulto) ? "cliente" : "cuidador";

            viewModel.iniciarSesion(mail, pass, tel, selectedRole);
        });

        btnGoReg.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra("forceNewRegister", true);
            startActivity(intent);
        });

        configurarObservadores();
    }

    private void configurarObservadores() {
        viewModel.getEstadoLogin().observe(this, estado -> {
            if (estado == null) return;
            switch (estado) {
                case CARGANDO:
                    if (pbLogin != null) pbLogin.setVisibility(View.VISIBLE);
                    setControlesActivos(false);
                    break;
                case EXITO:
                    if (pbLogin != null) pbLogin.setVisibility(View.GONE);
                    setControlesActivos(true);
                    Toast.makeText(this, "Bienvenido/a " + (viewModel.getSesionExitosa().getValue() != null ? viewModel.getSesionExitosa().getValue().nombre : ""), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                    break;
                case ERROR_CREDENTIALS:
                case ERROR_CAMPOS_VACIOS:
                case ERROR_RED:
                    if (pbLogin != null) pbLogin.setVisibility(View.GONE);
                    setControlesActivos(true);
                    break;
                case ESPERANDO:
                default:
                    if (pbLogin != null) pbLogin.setVisibility(View.GONE);
                    setControlesActivos(true);
                    break;
            }
        });

        viewModel.getMensajeError().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getMensajeRecuperacion().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setControlesActivos(boolean activo) {
        if (btnEntrar != null) btnEntrar.setEnabled(activo);
        if (inMail != null) inMail.setEnabled(activo);
        if (inPass != null) inPass.setEnabled(activo);
        if (inPhone != null) inPhone.setEnabled(activo);
        if (selPerfil != null) {
            for (int i = 0; i < selPerfil.getChildCount(); i++) {
                selPerfil.getChildAt(i).setEnabled(activo);
            }
        }
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
                viewModel.solicitarRecuperacionPassword(mail);
            });
        }

        dialog.show();
    }
}
