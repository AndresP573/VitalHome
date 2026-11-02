package com.example.vitalhome.ui.controladores;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.ui.adapter.RoleAdapter;
import com.example.vitalhome.ui.adapter.VinculoAdapter;
import com.example.vitalhome.ui.viewmodels.AppViewModelFactory;
import com.example.vitalhome.ui.viewmodels.RegisterViewModel;

/**
 * Actividad encargada del registro inicial personalizado por pasos (Wizard Paso a Paso).
 * Sigue la arquitectura MVVM estricta, delegando la lógica de negocio y persistencia a RegisterViewModel.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final RoleAdapter.RoleItem[] ROLE_ITEMS = {
            new RoleAdapter.RoleItem("Soy el Adulto Mayor", "Para uso directo del abuelo/a", true),
            new RoleAdapter.RoleItem("Soy el Familiar / Cuidador", "Para monitoreo y acompañamiento", false)
    };

    private static final VinculoAdapter.VinculoItem[] VINCULO_ITEMS_MASCULINO = {
            new VinculoAdapter.VinculoItem("Hijo"),
            new VinculoAdapter.VinculoItem("Sobrino"),
            new VinculoAdapter.VinculoItem("Nieto"),
            new VinculoAdapter.VinculoItem("Cuidador"),
            new VinculoAdapter.VinculoItem("Otro...")
    };

    private static final VinculoAdapter.VinculoItem[] VINCULO_ITEMS_FEMENINO = {
            new VinculoAdapter.VinculoItem("Hija"),
            new VinculoAdapter.VinculoItem("Sobrina"),
            new VinculoAdapter.VinculoItem("Nieta"),
            new VinculoAdapter.VinculoItem("Cuidadora"),
            new VinculoAdapter.VinculoItem("Otro...")
    };

    private RegisterViewModel viewModel;
    private RoleAdapter roleAdapter;
    private AlertDialog vinculoDialog;
    private ProgressBar pbRegister;
    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppViewModelFactory factory = new AppViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(RegisterViewModel.class);

        if (viewModel.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        boolean forceNewRegister = getIntent().getBooleanExtra("forceNewRegister", false);
        if (!forceNewRegister && viewModel.isRegistered()) {
            startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        setContentView(R.layout.activity_register);

        LinearLayout layoutPaso1 = findViewById(R.id.layoutPaso1);
        LinearLayout layoutPaso2 = findViewById(R.id.layoutPaso2);

        RecyclerView rvRoles = findViewById(R.id.rvRolesRegister);
        Button btnContinuarPaso1 = findViewById(R.id.btnContinuarPaso1);
        TextView tvGoToLoginPaso1 = findViewById(R.id.tvGoToLoginPaso1);

        ImageButton btnVolverPaso1 = findViewById(R.id.btnVolverPaso1);

        LinearLayout formCuidador = findViewById(R.id.layoutFormCuidador);
        LinearLayout formAdulto = findViewById(R.id.layoutFormAdulto);

        EditText inNombre = findViewById(R.id.etNombreRegister);
        TextView errNombre = findViewById(R.id.tvErrorNombre);

        EditText inEdadCuidador = findViewById(R.id.etEdadCuidador);
        TextView errEdadCuidador = findViewById(R.id.tvErrorEdadCuidador);
        RadioGroup rgGeneroCuidador = findViewById(R.id.rgGeneroCuidador);

        EditText inVinculo = findViewById(R.id.etVinculoRegister);
        TextView errVinculo = findViewById(R.id.tvErrorVinculo);
        EditText inVinculoEspecifique = findViewById(R.id.etVinculoEspecifique);

        EditText inTelefono = findViewById(R.id.etTelefonoRegister);
        TextView errTelefono = findViewById(R.id.tvErrorTelefono);

        EditText inEdad = findViewById(R.id.etEdadRegister);
        TextView errEdad = findViewById(R.id.tvErrorEdad);
        RadioGroup rgGenero = findViewById(R.id.rgGeneroRegister);

        EditText inEmail = findViewById(R.id.etEmailRegister);
        TextView errEmail = findViewById(R.id.tvErrorEmail);

        EditText inPass = findViewById(R.id.etPasswordRegister);
        TextView errPassword = findViewById(R.id.tvErrorPassword);

        btnOk = findViewById(R.id.btnRegister);
        pbRegister = findViewById(R.id.pbRegister);
        TextView btnLogin = findViewById(R.id.tvGoToLogin);

        android.text.TextWatcher ageGuardWatcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (s != null && s.length() > 3) {
                    s.clear();
                }
            }
        };

        if (inEdadCuidador != null) {
            inEdadCuidador.addTextChangedListener(ageGuardWatcher);
            inEdadCuidador.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }

        if (inEdad != null) {
            inEdad.addTextChangedListener(ageGuardWatcher);
            inEdad.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }

        if (rgGeneroCuidador != null) {
            rgGeneroCuidador.setOnCheckedChangeListener((group, checkedId) -> {
                if (inVinculo != null) inVinculo.setText("");
                if (inVinculoEspecifique != null) {
                    inVinculoEspecifique.setVisibility(View.GONE);
                    inVinculoEspecifique.setText("");
                }
            });
        }

        if (rvRoles != null) {
            rvRoles.setLayoutManager(new LinearLayoutManager(this));
            roleAdapter = new RoleAdapter(ROLE_ITEMS, isAdulto -> {});
            rvRoles.setAdapter(roleAdapter);
        }

        if (btnContinuarPaso1 != null) {
            btnContinuarPaso1.setOnClickListener(v -> {
                boolean isAdulto = (roleAdapter != null) && roleAdapter.isSelectedAdulto();
                if (isAdulto) {
                    if (formAdulto != null) formAdulto.setVisibility(View.VISIBLE);
                    if (formCuidador != null) formCuidador.setVisibility(View.GONE);
                } else {
                    if (formCuidador != null) formCuidador.setVisibility(View.VISIBLE);
                    if (formAdulto != null) formAdulto.setVisibility(View.GONE);
                }

                if (layoutPaso1 != null) layoutPaso1.setVisibility(View.GONE);
                if (layoutPaso2 != null) layoutPaso2.setVisibility(View.VISIBLE);
            });
        }

        if (btnVolverPaso1 != null) {
            btnVolverPaso1.setOnClickListener(v -> {
                if (layoutPaso2 != null) layoutPaso2.setVisibility(View.GONE);
                if (layoutPaso1 != null) layoutPaso1.setVisibility(View.VISIBLE);
            });
        }

        if (inVinculo != null) {
            inVinculo.setFocusable(false);
            inVinculo.setClickable(true);
            inVinculo.setCursorVisible(false);
            inVinculo.setKeyListener(null);
            inVinculo.setOnClickListener(v -> mostrarMenuVinculosRecyclerView(inVinculo, inVinculoEspecifique, rgGeneroCuidador));
        }

        if (btnOk != null) {
            btnOk.setOnClickListener(v -> {
                if (errNombre != null) errNombre.setVisibility(View.GONE);
                if (errVinculo != null) errVinculo.setVisibility(View.GONE);
                if (errTelefono != null) errTelefono.setVisibility(View.GONE);
                if (errEdad != null) errEdad.setVisibility(View.GONE);
                if (errEdadCuidador != null) errEdadCuidador.setVisibility(View.GONE);
                if (errEmail != null) errEmail.setVisibility(View.GONE);
                if (errPassword != null) errPassword.setVisibility(View.GONE);

                String n = inNombre != null ? inNombre.getText().toString() : "";
                String e = inEmail != null ? inEmail.getText().toString() : "";
                String p = inPass != null ? inPass.getText().toString() : "";

                boolean isAdulto = (roleAdapter != null) && roleAdapter.isSelectedAdulto();

                String edadStr;
                String generoStr;
                String vinculoTexto = "";
                String telStr = "";

                if (isAdulto) {
                    edadStr = (inEdad != null) ? inEdad.getText().toString().trim() : "";
                    generoStr = (rgGenero != null && rgGenero.getCheckedRadioButtonId() == R.id.rbGeneroM) ? "M" : "F";
                } else {
                    edadStr = (inEdadCuidador != null) ? inEdadCuidador.getText().toString().trim() : "";
                    generoStr = (rgGeneroCuidador != null && rgGeneroCuidador.getCheckedRadioButtonId() == R.id.rbGeneroCuidadorM) ? "M" : "F";
                    vinculoTexto = (inVinculo != null) ? inVinculo.getText().toString().trim() : "";
                    if ("Otro...".equals(vinculoTexto) || vinculoTexto.isEmpty()) {
                        if (inVinculoEspecifique != null && inVinculoEspecifique.getVisibility() == View.VISIBLE) {
                            String esp = inVinculoEspecifique.getText().toString().trim();
                            vinculoTexto = esp.isEmpty() ? "Otro vínculo" : esp;
                        } else if (vinculoTexto.isEmpty()) {
                            vinculoTexto = ("M".equals(generoStr)) ? "Cuidador" : "Cuidadora";
                        }
                    }
                    telStr = (inTelefono != null) ? inTelefono.getText().toString().trim() : "";
                }

                viewModel.registrarUsuario(n, e, p, isAdulto, edadStr, generoStr, vinculoTexto, telStr);
            });
        }

        View.OnClickListener irALogin = v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        };

        if (btnLogin != null) btnLogin.setOnClickListener(irALogin);
        if (tvGoToLoginPaso1 != null) tvGoToLoginPaso1.setOnClickListener(irALogin);

        configurarObservadores();
    }

    private void configurarObservadores() {
        viewModel.getEstadoRegistro().observe(this, estado -> {
            if (estado == null) return;
            switch (estado) {
                case CARGANDO:
                    if (pbRegister != null) pbRegister.setVisibility(View.VISIBLE);
                    if (btnOk != null) btnOk.setEnabled(false);
                    break;
                case EXITO:
                    if (pbRegister != null) pbRegister.setVisibility(View.GONE);
                    if (btnOk != null) btnOk.setEnabled(true);
                    String nombre = viewModel.getNombreRegistroExitoso().getValue();
                    Toast.makeText(this, "✓ Registro exitoso" + (nombre != null ? " de " + nombre : ""), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                    break;
                case ERROR_USUARIO_EXISTENTE:
                    if (pbRegister != null) pbRegister.setVisibility(View.GONE);
                    if (btnOk != null) btnOk.setEnabled(true);
                    Toast.makeText(this, getString(R.string.msg_user_already_registered), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    break;
                case ERROR_CAMPOS_INVALIDOS:
                case ERROR_RED:
                default:
                    if (pbRegister != null) pbRegister.setVisibility(View.GONE);
                    if (btnOk != null) btnOk.setEnabled(true);
                    break;
            }
        });

        viewModel.getMensajeError().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarMenuVinculosRecyclerView(EditText inVinculo, EditText inVinculoEspecifique, RadioGroup rgGeneroCuidador) {
        if (vinculoDialog != null && vinculoDialog.isShowing()) {
            vinculoDialog.dismiss();
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_vinculo_selector, null);
        RecyclerView rvDialog = dialogView.findViewById(R.id.rvVinculosDialog);

        vinculoDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        boolean esMasculino = (rgGeneroCuidador == null) || (rgGeneroCuidador.getCheckedRadioButtonId() == R.id.rbGeneroCuidadorM);
        VinculoAdapter.VinculoItem[] itemsSegunGenero = esMasculino ? VINCULO_ITEMS_MASCULINO : VINCULO_ITEMS_FEMENINO;

        if (rvDialog != null) {
            rvDialog.setLayoutManager(new LinearLayoutManager(this));
            VinculoAdapter adapter = new VinculoAdapter(itemsSegunGenero, (vinculo, isOtro) -> {
                if (inVinculo != null) {
                    inVinculo.setText(vinculo);
                }
                if (inVinculoEspecifique != null) {
                    if (isOtro || "Otro...".equals(vinculo)) {
                        inVinculoEspecifique.setVisibility(View.VISIBLE);
                        inVinculoEspecifique.setEnabled(true);
                        inVinculoEspecifique.requestFocus();
                    } else {
                        inVinculoEspecifique.setVisibility(View.GONE);
                        inVinculoEspecifique.setEnabled(false);
                        inVinculoEspecifique.setText("");
                    }
                }
                if (vinculoDialog != null) {
                    vinculoDialog.dismiss();
                }
            });
            rvDialog.setAdapter(adapter);
        }

        vinculoDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vinculoDialog != null && vinculoDialog.isShowing()) {
            vinculoDialog.dismiss();
        }
        vinculoDialog = null;
    }
}
