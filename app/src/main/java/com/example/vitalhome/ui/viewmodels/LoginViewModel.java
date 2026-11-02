package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.local.AppDatabase;
import com.example.vitalhome.data.local.UserEntity;
import com.example.vitalhome.data.repository.AppRepo;
import com.example.vitalhome.utils.AppExecutors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

/**
 * ViewModel responsable de la gestión de la lógica de autenticación e inicio de sesión.
 * Implementa una máquina de estados mediante el enum EstadoLogin.
 * Desacopla a la Vista (LoginActivity) de cualquier acceso directo a Room, Firestore, FirebaseAuth y SharedPreferences.
 */
public class LoginViewModel extends AndroidViewModel {

    private static final String TAG = "LoginViewModel";

    public enum EstadoLogin {
        ESPERANDO,
        CARGANDO,
        EXITO,
        ERROR_CREDENTIALS,
        ERROR_CAMPOS_VACIOS,
        ERROR_RED
    }

    public static class UserSessionInfo {
        public final String email;
        public final String nombre;
        public final String role;
        public final String phone;

        public UserSessionInfo(String email, String nombre, String role, String phone) {
            this.email = email;
            this.nombre = nombre;
            this.role = role;
            this.phone = phone;
        }
    }

    private final AppRepo repo;
    private final SharedPreferences prefs;
    private final MutableLiveData<EstadoLogin> estadoLogin = new MutableLiveData<>(EstadoLogin.ESPERANDO);
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>("");
    private final MutableLiveData<UserSessionInfo> sesionExitosa = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeRecuperacion = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        this(application, new AppRepo(application));
    }

    public LoginViewModel(@NonNull Application application, @NonNull AppRepo repo) {
        super(application);
        this.repo = repo;
        this.prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
    }

    public LiveData<EstadoLogin> getEstadoLogin() {
        return estadoLogin;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public LiveData<UserSessionInfo> getSesionExitosa() {
        return sesionExitosa;
    }

    public LiveData<String> getMensajeRecuperacion() {
        return mensajeRecuperacion;
    }

    public boolean isRegistered() {
        return prefs.getBoolean("isRegistered", false);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("isLoggedIn", false);
    }

    public String getSavedEmail(String roleKey) {
        return prefs.getString(roleKey, prefs.getString("email", ""));
    }

    public String getSavedPhone() {
        return prefs.getString("phone_cuidador", "");
    }

    /**
     * Inicia el proceso de autenticación híbrido (Room + Firestore) en hilos de fondo.
     */
    public void iniciarSesion(String mail, String pass, String tel, String selectedRole) {
        if (mail == null || mail.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            mensajeError.setValue("Por favor completa todos los campos requeridos");
            estadoLogin.setValue(EstadoLogin.ERROR_CAMPOS_VACIOS);
            return;
        }

        estadoLogin.setValue(EstadoLogin.CARGANDO);

        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                // Iniciar sesión en Firebase Auth de forma transparente
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() == null) {
                    auth.signInAnonymously();
                }
                auth.signInWithEmailAndPassword(mail, pass);
            } catch (Exception e) {
                Log.e(TAG, "Excepción en FirebaseAuth signInWithEmailAndPassword", e);
            }

            // 1. Consultar base de datos local (Room)
            UserEntity localUser = null;
            try {
                localUser = AppDatabase.getInstance(getApplication())
                        .userDao().getUserByEmail(mail);
            } catch (Exception e) {
                Log.e(TAG, "Error consultando usuario en Room", e);
            }

            if (localUser != null) {
                if (pass.equals(localUser.password) || pass.equals(prefs.getString("password", ""))) {
                    String nombreUsr = Objects.requireNonNullElse(localUser.nombreCompleto, "Usuario");
                    finalizarSesionExitosa(mail, pass, localUser.rol != null ? localUser.rol : selectedRole, tel, nombreUsr);
                } else {
                    mensajeError.postValue("Contraseña incorrecta");
                    estadoLogin.postValue(EstadoLogin.ERROR_CREDENTIALS);
                }
            } else {
                // 2. Si no está en local, verificar en Firestore
                verificarEnCloudFirestore(mail, pass, selectedRole, tel);
            }
        });
    }

    private void verificarEnCloudFirestore(String mail, String pass, String selectedRole, String tel) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String docId = mail.replace(".", "_");

            db.collection("users").document(docId).get()
                    .addOnSuccessListener(doc -> {
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            if (doc.exists()) {
                                String nombreCloud = doc.getString("nombre");
                                String rolCloud = doc.getString("rol");
                                String telCloud = doc.getString("telefono");

                                UserEntity newUser = new UserEntity();
                                newUser.email = mail;
                                newUser.password = pass;
                                newUser.nombreCompleto = nombreCloud;
                                newUser.rol = rolCloud != null ? rolCloud : selectedRole;
                                newUser.telefono = telCloud;
                                newUser.createdAt = System.currentTimeMillis();

                                try {
                                    AppDatabase.getInstance(getApplication()).userDao().insert(newUser);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error insertando usuario descargado de Firestore en Room", e);
                                }

                                String nombreUsr = Objects.requireNonNullElse(nombreCloud, "Usuario");
                                finalizarSesionExitosa(mail, pass, newUser.rol, tel, nombreUsr);
                            } else {
                                // Fallback SharedPreferences
                                String passOk = prefs.getString("password", "");
                                if (!passOk.isEmpty() && Objects.equals(pass, passOk)) {
                                    String nombreUsr = prefs.getString("nombre", "Usuario");
                                    finalizarSesionExitosa(mail, pass, selectedRole, tel, nombreUsr);
                                } else {
                                    mensajeError.postValue("Contraseña incorrecta o usuario no encontrado");
                                    estadoLogin.postValue(EstadoLogin.ERROR_CREDENTIALS);
                                }
                            }
                        });
                    })
                    .addOnFailureListener(err -> {
                        Log.e(TAG, "Error consultando Firestore para login", err);
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            String passOk = prefs.getString("password", "");
                            if (!passOk.isEmpty() && Objects.equals(pass, passOk)) {
                                String nombreUsr = prefs.getString("nombre", "Usuario");
                                finalizarSesionExitosa(mail, pass, selectedRole, tel, nombreUsr);
                            } else {
                                mensajeError.postValue("Error de red o contraseña incorrecta");
                                estadoLogin.postValue(EstadoLogin.ERROR_RED);
                            }
                        });
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error en lectura de Cloud Firestore", e);
            mensajeError.postValue("Error en el servicio de autenticación");
            estadoLogin.postValue(EstadoLogin.ERROR_RED);
        }
    }

    private void finalizarSesionExitosa(String mail, String pass, String role, String tel, String nombre) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("isRegistered", true);
        edit.putBoolean("isLoggedIn", true);
        edit.putString("rol", role);
        edit.putString("email", mail);
        edit.putString("password", pass);
        edit.putString("nombre", nombre);

        if ("cliente".equals(role)) {
            edit.putString("email_cliente", mail);
            edit.putString("nombreAdultoMayor", nombre);
        } else {
            edit.putString("email_cuidador", mail);
            edit.putString("phone_cuidador", tel);
            edit.putString("nombre", nombre);
        }
        edit.apply();

        sesionExitosa.postValue(new UserSessionInfo(mail, nombre, role, tel));
        estadoLogin.postValue(EstadoLogin.EXITO);
    }

    /**
     * Solicita el restablecimiento de la contraseña por correo vía Firebase Auth o consulta Firestore en fallback.
     */
    public void solicitarRecuperacionPassword(String mail) {
        if (mail == null || mail.trim().isEmpty()) {
            mensajeRecuperacion.setValue("Por favor ingrese su correo electrónico");
            return;
        }

        try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(mail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mensajeRecuperacion.postValue("✉️ Correo de recuperación enviado a " + mail + ". Revise su bandeja de entrada.");
                        } else {
                            consultarPasswordEnFirestore(mail);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error enviando correo de recuperación con FirebaseAuth", e);
            consultarPasswordEnFirestore(mail);
        }
    }

    private void consultarPasswordEnFirestore(String mail) {
        try {
            String docId = mail.replace(".", "_");
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(docId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && doc.contains("password")) {
                            String passRecuperada = doc.getString("password");
                            mensajeRecuperacion.postValue("🔑 Cuenta verificada. Tu contraseña es: " + passRecuperada);
                        } else {
                            mensajeRecuperacion.postValue("✉️ Correo de recuperación enviado a " + mail + ". Revise su bandeja de entrada.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error en consulta de contraseña de recuperación en Firestore", e);
                        mensajeRecuperacion.postValue("✉️ Correo de recuperación enviado a " + mail + ". Revise su bandeja de entrada.");
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error en Firestore recuperación", e);
            mensajeRecuperacion.postValue("✉️ Correo de recuperación enviado a " + mail + ". Revise su bandeja de entrada.");
        }
    }
}
