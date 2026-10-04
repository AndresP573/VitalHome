# Arquitectura de Sincronización Offline-First (Sin pérdida de datos y tiempo real)

Este documento detalla la estrategia recomendada y probada por los estándares de ingeniería de Google Cloud / Firebase para garantizar que **nunca se pierdan datos, no queden registros residuales y la sincronización entre el Adulto Mayor y el Cuidador sea instantánea y robusta**.

## 1. Persistencia Offline Nativa de Cloud Firestore
El SDK de Cloud Firestore cuenta con un motor de caché local en disco. Al configurarlo explícitamente, todas las escrituras y lecturas funcionan sin conexión a internet y se sincronizan automáticamente al recuperar señal.

### Configuración en el inicio (`MainActivity` o clase `Application`):
```java
FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .build();
FirebaseFirestore.getInstance("default").setFirestoreSettings(settings);
```

## 2. Patrón Repository con Actualización Optimista (Optimistic UI)
Para evitar que el usuario deba esperar "la llamada de la red" (latencia):
1. **Acción Local Inmediata:** La app actualiza la base de datos local (Room / SharedPreferences) y refresca la UI al instante (0ms de retraso).
2. **Sincronización en Segundo Plano:** En paralelo, se despacha la orden a Cloud Firestore a través de `AppExecutors` o `UserRepository`.
3. **Cola de Reintentos Automática:** Si el dispositivo está offline, Firestore encola la operación en disco y la ejecuta en cuanto se recupera la conectividad.

## 3. Limpieza de Listeners (Cero Fugas de Memoria)
Cada `addSnapshotListener` abre un canal de red persistente (WebSocket). Si no se remueven al salir de la pantalla, generan consumo innecesario de batería y llamadas residuales.
- **Regla de Oro:** Guardar cada `ListenerRegistration` y removerlo en `onStop()` o `onDestroyView()`:
```java
@Override
protected void onStop() {
    super.onStop();
    if (listenerRegistration != null) {
        listenerRegistration.remove();
        listenerRegistration = null;
    }
}
```

## 4. Borrado Atómico de Datos Residuales
Cuando se elimina un ítem (ej. un medicamento o evento), la operación debe ser bidireccional:
1. Eliminar localmente del repositorio.
2. Ejecutar `.delete()` en Firestore sobre el documento exacto:
```java
db.collection("medications").document(medId).delete();
```
