package com.example.vitalhome.ui.controladores;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.data.local.AppDatabase;
import com.example.vitalhome.data.local.UserEntity;
import com.example.vitalhome.data.repository.UserRepository;
import com.example.vitalhome.ui.adapter.RoleAdapter;
import com.example.vitalhome.ui.adapter.VinculoAdapter;
import com.example.vitalhome.utils.AppExecutors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Actividad encargada del registro inicial personalizado por pasos (Wizard Paso a Paso).
 * Paso 1: Selección de Perfil/Rol. Paso 2: Carga de datos específica según el rol.
 * Realiza autenticación previa en Firebase Auth y sincronización en Cloud Firestore.
 */
public class RegisterActivity extends AppCompatActivity {

    // --- Ítems para el RecyclerView de Roles ---
    private static final RoleAdapter.RoleItem[] ROLE_ITEMS = {
            new RoleAdapter.RoleItem("Soy el Adulto Mayor", "Para uso directo del abuelo/a", true),
            new RoleAdapter.RoleItem("Soy el Familiar / Cuidador", "Para monitoreo y acompañamiento", false)
    };

    // --- Ítems dinámicos según género para el Menú de Vínculos del Cuidador ---
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

    private RoleAdapter roleAdapter;
    private AlertDialog vinculoDialog;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences checkPrefs = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);

        // Si ya hay sesión activa -> ir directo a la app principal
        if (checkPrefs.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        // Si el usuario ya está registrado en el celular -> ir al Login para no solicitar registro nuevamente
        boolean forceNewRegister = getIntent().getBooleanExtra("forceNewRegister", false);
        if (!forceNewRegister && checkPrefs.getBoolean("isRegistered", false)) {
            startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        setContentView(R.layout.activity_register);

        // Inicializar Firestore para verificación y registro
        db = FirebaseFirestore.getInstance();

        // --- Bindeo de contenedores del Wizard por pasos ---
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

        // Campos específicos Cuidador
        EditText inEdadCuidador = findViewById(R.id.etEdadCuidador);
        TextView errEdadCuidador = findViewById(R.id.tvErrorEdadCuidador);
        RadioGroup rgGeneroCuidador = findViewById(R.id.rgGeneroCuidador);

        EditText inVinculo = findViewById(R.id.etVinculoRegister);
        TextView errVinculo = findViewById(R.id.tvErrorVinculo);
        EditText inVinculoEspecifique = findViewById(R.id.etVinculoEspecifique);

        EditText inTelefono = findViewById(R.id.etTelefonoRegister);
        TextView errTelefono = findViewById(R.id.tvErrorTelefono);

        // Campos específicos Adulto Mayor
        EditText inEdad = findViewById(R.id.etEdadRegister);
        TextView errEdad = findViewById(R.id.tvErrorEdad);
        RadioGroup rgGenero = findViewById(R.id.rgGeneroRegister);

        // Campos Generales
        EditText inEmail = findViewById(R.id.etEmailRegister);
        TextView errEmail = findViewById(R.id.tvErrorEmail);

        EditText inPass = findViewById(R.id.etPasswordRegister);
        TextView errPassword = findViewById(R.id.tvErrorPassword);

        Button btnOk = findViewById(R.id.btnRegister);
        TextView btnLogin = findViewById(R.id.tvGoToLogin);

        // --- Bloqueo de Autofill para evitar que el teléfono se copie en la Edad ---
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

        // Limpiar el campo vínculo si el Cuidador cambia su género
        if (rgGeneroCuidador != null) {
            rgGeneroCuidador.setOnCheckedChangeListener((group, checkedId) -> {
                if (inVinculo != null) {
                    inVinculo.setText("");
                }
                if (inVinculoEspecifique != null) {
                    inVinculoEspecifique.setVisibility(View.GONE);
                    inVinculoEspecifique.setText("");
                }
            });
        }

        // --- Configuración del RecyclerView de Roles en Paso 1 ---
        if (rvRoles != null) {
            rvRoles.setLayoutManager(new LinearLayoutManager(this));
            roleAdapter = new RoleAdapter(ROLE_ITEMS, isAdulto -> {
                // Callback de selección de rol
            });
            rvRoles.setAdapter(roleAdapter);
        }

        // --- Transición del Paso 1 al Paso 2 ---
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

        // --- Regreso del Paso 2 al Paso 1 ---
        if (btnVolverPaso1 != null) {
            btnVolverPaso1.setOnClickListener(v -> {
                if (layoutPaso2 != null) layoutPaso2.setVisibility(View.GONE);
                if (layoutPaso1 != null) layoutPaso1.setVisibility(View.VISIBLE);
            });
        }

        // --- Evento al tocar el campo Vínculo con flecha desplegable (Lectura únicamente, no editable) ---
        if (inVinculo != null) {
            inVinculo.setFocusable(false);
            inVinculo.setClickable(true);
            inVinculo.setCursorVisible(false);
            inVinculo.setKeyListener(null);
            inVinculo.setOnClickListener(v -> mostrarMenuVinculosRecyclerView(inVinculo, inVinculoEspecifique, rgGeneroCuidador));
        }

        // --- Evento de registro final con verificación previa en BD ---
        if (btnOk != null) {
            btnOk.setOnClickListener(v -> {
                boolean esValido = true;

                if (errNombre != null) errNombre.setVisibility(View.GONE);
                if (errVinculo != null) errVinculo.setVisibility(View.GONE);
                if (errTelefono != null) errTelefono.setVisibility(View.GONE);
                if (errEdad != null) errEdad.setVisibility(View.GONE);
                if (errEdadCuidador != null) errEdadCuidador.setVisibility(View.GONE);
                if (errEmail != null) errEmail.setVisibility(View.GONE);
                if (errPassword != null) errPassword.setVisibility(View.GONE);

                String n = inNombre != null ? inNombre.getText().toString().trim() : "";
                String e = inEmail != null ? inEmail.getText().toString().trim() : "";
                String p = inPass != null ? inPass.getText().toString().trim() : "";

                boolean isAdulto = (roleAdapter != null) && roleAdapter.isSelectedAdulto();
                String role = isAdulto ? "cliente" : "cuidador";

                if (n.length() < 3) {
                    if (errNombre != null) errNombre.setVisibility(View.VISIBLE);
                    esValido = false;
                }

                if (e.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                    if (errEmail != null) errEmail.setVisibility(View.VISIBLE);
                    esValido = false;
                }

                if (p.length() < 4) {
                    if (errPassword != null) errPassword.setVisibility(View.VISIBLE);
                    esValido = false;
                }

                String edadStr;
                String generoStr;
                String vinculoTexto = "";
                String telStr = "";

                if (isAdulto) {
                    edadStr = (inEdad != null) ? inEdad.getText().toString().trim() : "";
                    generoStr = (rgGenero != null && rgGenero.getCheckedRadioButtonId() == R.id.rbGeneroM) ? "M" : "F";

                    try {
                        int edadNum = Integer.parseInt(edadStr);
                        if (edadNum <= 0 || edadNum > 120) {
                            if (errEdad != null) errEdad.setVisibility(View.VISIBLE);
                            esValido = false;
                        }
                    } catch (Exception ex) {
                        if (errEdad != null) errEdad.setVisibility(View.VISIBLE);
                        esValido = false;
                    }
                } else {
                    edadStr = (inEdadCuidador != null) ? inEdadCuidador.getText().toString().trim() : "";
                    generoStr = (rgGeneroCuidador != null && rgGeneroCuidador.getCheckedRadioButtonId() == R.id.rbGeneroCuidadorM) ? "M" : "F";

                    try {
                        int edadCuidadorNum = Integer.parseInt(edadStr);
                        if (edadCuidadorNum <= 0 || edadCuidadorNum > 120) {
                            if (errEdadCuidador != null) errEdadCuidador.setVisibility(View.VISIBLE);
                            esValido = false;
                        }
                    } catch (Exception ex) {
                        if (errEdadCuidador != null) errEdadCuidador.setVisibility(View.VISIBLE);
                        esValido = false;
                    }

                    vinculoTexto = (inVinculo != null) ? inVinculo.getText().toString().trim() : "";
                    if ("Otro...".equals(vinculoTexto) || vinculoTexto.isEmpty()) {
                        if (inVinculoEspecifique != null && inVinculoEspecifique.getVisibility() == View.VISIBLE) {
                            String esp = inVinculoEspecifique.getText().toString().trim();
                            vinculoTexto = esp.isEmpty() ? "Otro vínculo" : esp;
                        } else if (vinculoTexto.isEmpty()) {
                            vinculoTexto = ("M".equals(generoStr)) ? "Cuidador" : "Cuidadora";
                        }
                    }

                    if (vinculoTexto.length() < 2) {
                        if (errVinculo != null) errVinculo.setVisibility(View.VISIBLE);
                        esValido = false;
                    }

                    telStr = (inTelefono != null) ? inTelefono.getText().toString().trim() : "";
                    if (telStr.length() < 6) {
                        if (errTelefono != null) errTelefono.setVisibility(View.VISIBLE);
                        esValido = false;
                    }
                }

                if (!esValido) {
                    Toast.makeText(this, getString(R.string.msg_faltan_datos), Toast.LENGTH_SHORT).show();
                    return;
                }

                btnOk.setEnabled(false);

                // --- Verificación de Usuario Existente en BD (Room + Firestore) ---
                String finalEdadStr = edadStr;
                String finalGeneroStr = generoStr;
                String finalVinculoTexto = vinculoTexto;
                String finalTelStr = telStr;

                AppExecutors.getInstance().diskIO().execute(() -> {
                    UserEntity userLocal = AppDatabase.getInstance(getApplicationContext()).userDao().getUserByEmail(e);

                    AppExecutors.getInstance().mainThread().execute(() -> {
                        if (userLocal != null) {
                            btnOk.setEnabled(true);
                            Toast.makeText(this, getString(R.string.msg_user_already_registered), Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        } else {
                            // Verificar también en Firestore en la nube
                            String docId = e.replace(".", "_");
                            db.collection("users").document(docId).get().addOnCompleteListener(task -> {
                                btnOk.setEnabled(true);
                                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                    Toast.makeText(this, getString(R.string.msg_user_already_registered), Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                } else {
                                    // Usuario nuevo: Registrar en Room y Firestore con Autenticación previa
                                    realizarRegistroNuevo(n, e, p, role, isAdulto, finalEdadStr, finalGeneroStr, finalVinculoTexto, finalTelStr);
                                }
                            });
                        }
                    });
                });
            });
        }

        // --- Redirección al Login ---
        View.OnClickListener irALogin = v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        };

        if (btnLogin != null) btnLogin.setOnClickListener(irALogin);
        if (tvGoToLoginPaso1 != null) tvGoToLoginPaso1.setOnClickListener(irALogin);
    }

    private void realizarRegistroNuevo(String n, String e, String p, String role, boolean isAdulto,
                                      String edadStr, String generoStr, String vinculoTexto, String telStr) {
        UserEntity userEntity = new UserEntity();
        userEntity.nombreCompleto = n;
        userEntity.email = e;
        userEntity.password = p;
        userEntity.rol = role;
        userEntity.createdAt = System.currentTimeMillis();

        try {
            userEntity.edad = Integer.parseInt(edadStr);
        } catch (Exception ignored) {
            userEntity.edad = null;
        }

        userEntity.genero = generoStr;

        if (isAdulto) {
            userEntity.telefono = null;
            userEntity.vinculo = null;
            userEntity.password = null;
        } else {
            userEntity.telefono = telStr;
            userEntity.vinculo = vinculoTexto;
            userEntity.password = p;
        }

        // 1. Guardar localmente en Room (SQLite)
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(getApplicationContext()).userDao().insert(userEntity));

        // 2. Autenticar previamente en Firebase Auth para cumplir con las reglas de seguridad de Firestore
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(e, p)
            .addOnCompleteListener(task -> {
                UserRepository userRepository = new UserRepository();
                userRepository.syncUserToFirestore(userEntity);
            })
            .addOnFailureListener(ex -> {
                auth.signInWithEmailAndPassword(e, p)
                    .addOnCompleteListener(signInTask -> {
                        UserRepository userRepository = new UserRepository();
                        userRepository.syncUserToFirestore(userEntity);
                    })
                    .addOnFailureListener(err -> {
                        auth.signInAnonymously().addOnCompleteListener(anonTask -> {
                            UserRepository userRepository = new UserRepository();
                            userRepository.syncUserToFirestore(userEntity);
                        });
                    });
            });

        SharedPreferences prefs = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();

        edit.putBoolean("isRegistered", true);
        edit.putBoolean("isLoggedIn", true);
        edit.putString("rol", role);
        edit.putString("email", e);
        edit.putString("password", p);

        if (isAdulto) {
            edit.putString("email_cliente", e);
            edit.putString("nombreAdultoMayor", n);
            edit.putString("edad_cliente", edadStr);
            edit.putString("genero_cliente", generoStr);
        } else {
            edit.putString("email_cuidador", e);
            edit.putString("nombre", n);
            edit.putString("vinculo_cuidador", vinculoTexto);
            edit.putString("phone_cuidador", telStr);
            edit.putString("nombreAdultoMayor", "Mi Familiar");
        }

        edit.apply();

        Toast.makeText(this, "✓ Registro exitoso de " + n, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    /**
     * Muestra el menú desplegable desplegando un RecyclerView interactivo con las opciones de vínculo según el género del cuidador.
     */
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
                // Sincronización del vínculo seleccionado
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
