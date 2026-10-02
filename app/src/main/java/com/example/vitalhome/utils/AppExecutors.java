package com.example.vitalhome.utils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Gestor centralizado de hilos para implementar la Teoría de Hilos (Multi-threading/Concurrencia).
 * Administra la ejecución de tareas intensivas de I/O en hilos de fondo y la publicación de resultados en el hilo principal de la UI.
 */
public class AppExecutors {

    // --- Instancia Singleton del gestor de hilos ---
    private static AppExecutors instance;

    private final Executor diskIO;
    private final Executor mainThread;

    private AppExecutors(Executor diskIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.mainThread = mainThread;
    }

    /**
     * Obtiene la instancia única del gestor de hilos de la aplicación.
     *
     * @return Instancia de AppExecutors.
     */
    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    instance = new AppExecutors(
                            Executors.newSingleThreadExecutor(),
                            new MainThreadExecutor()
                    );
                }
            }
        }
        return instance;
    }

    /**
     * Hilo secundario de trabajo (Background Worker Thread) para I/O, SharedPreferences y serialización JSON.
     */
    public Executor diskIO() {
        return diskIO;
    }

    /**
     * Hilo principal de la interfaz de usuario (Main UI Thread).
     */
    public Executor mainThread() {
        return mainThread;
    }

    /**
     * Executor interno para publicar resultados en el Hilo Principal usando Looper.
     */
    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
