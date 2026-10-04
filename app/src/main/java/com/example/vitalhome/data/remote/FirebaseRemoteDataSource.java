package com.example.vitalhome.data.remote;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

/**
 * Fuente de datos remota para la interacción directa con Cloud Firestore.
 * Pertenece a la capa data.remote del proyecto.
 */
public class FirebaseRemoteDataSource {

    private final FirebaseFirestore db;

    public FirebaseRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance("default");
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void pushDocument(String collection, String docId, Map<String, Object> data) {
        if (collection == null || docId == null || data == null) return;
        
        db.collection(collection)
            .document(docId)
            .set(data)
            .addOnSuccessListener(aVoid -> Log.d("FirebaseRemote", "✓ Documento subido a " + collection + "/" + docId))
            .addOnFailureListener(e -> Log.e("FirebaseRemote", "❌ Error subiendo a " + collection, e));
    }
}
