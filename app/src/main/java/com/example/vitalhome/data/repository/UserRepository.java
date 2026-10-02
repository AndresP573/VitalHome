package com.example.vitalhome.data.repository;

import android.util.Log;
import com.example.vitalhome.data.local.UserEntity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        // Conectamos apuntando específicamente a tu base de datos "default"
        this.db = FirebaseFirestore.getInstance("default");
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
        userData.put("telefono", user.telefono);
        if (user.vinculo != null) {
            userData.put("vinculo", user.vinculo);
        }
        userData.put("edad", user.edad);
        userData.put("genero", user.genero);
        userData.put("createdAt", user.createdAt);

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
