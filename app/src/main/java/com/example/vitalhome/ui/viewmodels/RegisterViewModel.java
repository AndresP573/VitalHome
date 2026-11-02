package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Patterns;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.local.AppDatabase;
import com.example.vitalhome.data.local.UserEntity;
import com.example.vitalhome.data.repository.AppRepo;
import com.example.vitalhome.data.repository.UserRepository;
import com.example.vitalhome.utils.AppExecutors;
import com.example.vitalhome.utils.SecurityUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ViewModel encargado del registro de nuevos usuarios en la aplicación.
 * Administra el estado mediante el enum EstadoRegistro y desacopla la actividad de operaciones directas de E/S.
 */
public class RegisterViewModel extends AndroidViewModel {

    private static final String TAG = "RegisterViewModel";

    public enum EstadoRegistro {
        ESPERANDO,
        CARGANDO,
        EXITO,
        ERROR_USUARIO_EXISTENTE,
        ERROR_CAMPOS_INVALIDOS,
        ERROR_RED
    }

    private final AppRepo repo;
    private final SharedPreferences prefs;
    private final UserRepository userRepository;
    private final MutableLiveData<EstadoRegistro> estadoRegistro = new MutableLiveData<>(EstadoRegistro.ESPERANDO);
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>("");
    private final MutableLiveData<String> nombreRegistroExitoso = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        this(application, new AppRepo(application));
    }

    public RegisterViewModel(@NonNull Application application, @NonNull AppRepo repo) {
        super(application);
        this.repo = repo;
        this.userRepository = new UserRepository();
        this.prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
    }

    public LiveData<EstadoRegistro> getEstadoRegistro() {
        return estadoRegistro;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public LiveData<String> getNombreRegistroExitoso() {
        return nombreRegistroExitoso;
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("isLoggedIn", false);
    }

    public boolean isRegistered() {
        return prefs.getBoolean("isRegistered", false);
    }

    /**
     * Procesa la solicitud de registro del usuario validando campos y realizando las verificaciones y persistencias en hilos de fondo.
     */
    public void registrarUsuario(String nombreRaw, String emailRaw, String passwordRaw, boolean isAdulto,
                                 String edadStr, String generoStr, String vinculoTextoRaw, String telStrRaw) {

        String n = SecurityUtils.sanitizeString(nombreRaw);
        String e = SecurityUtils.sanitizeString(emailRaw);
        String p = passwordRaw != null ? passwordRaw.trim() : "";
        String role = isAdulto ? "cliente" : "cuidador";

        if (n.length() < 3 || e.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(e).matches() || p.length() < 4) {
            mensajeError.setValue("Por favor complete los campos requeridos con un formato válido.");
            estadoRegistro.setValue(EstadoRegistro.ERROR_CAMPOS_INVALIDOS);
            return;
        }

        String vinculoTexto = vinculoTextoRaw != null ? vinculoTextoRaw.trim() : "";
        String telStr = telStrRaw != null ? telStrRaw.trim() : "";

        if (!isAdulto) {
            if (vinculoTexto.length() < 2 || telStr.length() < 6) {
                mensajeError.setValue("Por favor especifique un vínculo y teléfono válidos.");
                estadoRegistro.setValue(EstadoRegistro.ERROR_CAMPOS_INVALIDOS);
                return;
            }
        }

        estadoRegistro.setValue(EstadoRegistro.CARGANDO);

        AppExecutors.getInstance().diskIO().execute(() -> {
            UserEntity userLocal = null;
            try {
                userLocal = AppDatabase.getInstance(getApplication()).userDao().getUserByEmail(e);
            } catch (Exception ex) {
                Log.e(TAG, "Error consultando usuario local en Room", ex);
            }

            if (userLocal != null) {
                mensajeError.postValue("El usuario ya se encuentra registrado en el sistema.");
                estadoRegistro.postValue(EstadoRegistro.ERROR_USUARIO_EXISTENTE);
            } else {
                verificarYRegistrarEnCloud(n, e, p, role, isAdulto, edadStr, generoStr, vinculoTexto, telStr);
            }
        });
    }

    private void verificarYRegistrarEnCloud(String n, String e, String p, String role, boolean isAdulto,
                                           String edadStr, String generoStr, String vinculoTexto, String telStr) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance("default");
            String docId = e.replace(".", "_");

            db.collection("users").document(docId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    mensajeError.postValue("El usuario ya se encuentra registrado.");
                    estadoRegistro.postValue(EstadoRegistro.ERROR_USUARIO_EXISTENTE);
                } else {
                    realizarPersistenciaCompleta(n, e, p, role, isAdulto, edadStr, generoStr, vinculoTexto, telStr);
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "Error consultando Firestore para verificación de registro", ex);
            realizarPersistenciaCompleta(n, e, p, role, isAdulto, edadStr, generoStr, vinculoTexto, telStr);
        }
    }

    private void realizarPersistenciaCompleta(String n, String e, String p, String role, boolean isAdulto,
                                              String edadStr, String generoStr, String vinculoTexto, String telStr) {
        UserEntity userEntity = new UserEntity();
        userEntity.nombreCompleto = n;
        userEntity.email = e;
        userEntity.password = p;
        userEntity.rol = role;
        userEntity.createdAt = System.currentTimeMillis();

        try {
            userEntity.edad = Integer.parseInt(edadStr);
        } catch (Exception ex) {
            Log.e(TAG, "Error parseando edad en registro", ex);
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

        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                AppDatabase.getInstance(getApplication()).userDao().insert(userEntity);
            } catch (Exception ex) {
                Log.e(TAG, "Error guardando nuevo usuario en Room", ex);
            }

            try {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.createUserWithEmailAndPassword(e, p)
                        .addOnSuccessListener(authResult -> userRepository.syncUserToFirestore(userEntity))
                        .addOnFailureListener(ex -> {
                            Log.w(TAG, "Error creando usuario en FirebaseAuth: " + ex.getMessage());
                            auth.signInWithEmailAndPassword(e, p)
                                    .addOnSuccessListener(signInResult -> userRepository.syncUserToFirestore(userEntity))
                                    .addOnFailureListener(signInEx -> {
                                        Log.w(TAG, "Error iniciando sesión en FirebaseAuth: " + signInEx.getMessage());
                                        auth.signInAnonymously()
                                                .addOnSuccessListener(anonResult -> userRepository.syncUserToFirestore(userEntity))
                                                .addOnFailureListener(anonEx -> {
                                                    Log.e(TAG, "Error signing in anonymously", anonEx);
                                                    userRepository.syncUserToFirestore(userEntity);
                                                });
                                    });
                        });
            } catch (Exception ex) {
                Log.e(TAG, "Error sincronizando con FirebaseAuth / Firestore", ex);
            }

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

            nombreRegistroExitoso.postValue(n);
            estadoRegistro.postValue(EstadoRegistro.EXITO);
        });
    }
}
