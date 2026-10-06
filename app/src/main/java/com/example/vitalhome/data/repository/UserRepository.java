package com.example.vitalhome.data.repository;

import android.util.Log;
import com.example.vitalhome.data.local.UserEntity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * Repositorio de datos para la sincronización de usuarios entre SQLite local y Cloud Firestore.
 * Omite campos innecesarios (teléfono propio, vínculo y contraseña) para el perfil del Adulto Mayor.
 */
public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        // Conectamos a la base de datos predeterminada de Firestore
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Sincroniza un usuario de SQLite local en Firestore.
     */
    public void syncUserToFirestore(UserEntity user) {
        if (user == null || user.email == null) return;

        Map<String, Object> userData = new HashMap<>();
        userData.put("id_local", user.id); // Vinculamos el ID autoincremental de SQLite
        userData.put("nombre", user.nombreCompleto);
        userData.put("email", user.email);
        userData.put("rol", user.rol);
        userData.put("edad", user.edad);
        userData.put("genero", user.genero);
        userData.put("createdAt", user.createdAt);

        if ("cliente".equals(user.rol)) {
            // Adulto Mayor: Sin teléfono propio, sin vínculo y sin contraseña en la BD
            userData.put("telefono", null);
            userData.put("vinculo", null);
            userData.put("password", null);
        } else {
            // Cuidador: Con teléfono, vínculo y contraseña registrados
            userData.put("telefono", user.telefono);
            userData.put("vinculo", user.vinculo);
            userData.put("password", user.password);
        }

        // ID del documento en Firestore usando el correo modificado
        String docId = user.email.replace(".", "_");

        db.collection("users")
            .document(docId)
            .set(userData)
            .addOnSuccessListener(aVoid -> Log.d("UserRepository", "✓ Sincronizado en la nube: " + docId))
            .addOnFailureListener(e -> Log.e("UserRepository", "❌ Error de sincronización", e));
    }

    /**
     * Sincronizador Genérico de Subcolecciones en Firestore.
     * Permite subir medicamentos, signos vitales, alertas o historial médico.
     */
    public void syncSubcollectionData(String pacienteEmail, String subcollection, String docId, Map<String, Object> data) {
        if (pacienteEmail == null || subcollection == null || docId == null) return;

        db.collection("users")
            .document(pacienteEmail.replace(".", "_"))
            .collection(subcollection)
            .document(docId)
            .set(data)
            .addOnSuccessListener(aVoid -> Log.d("Sync", "✓ Sincronizado en " + subcollection))
            .addOnFailureListener(e -> Log.e("Sync", "❌ Error al sincronizar " + subcollection, e));
    }
}
