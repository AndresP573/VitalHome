package com.example.vitalhome.data.modelo;

/**
 * Modelo de datos que representa un evento registrado en el historial de la aplicación.
 * Registra alertas de salud, emergencias SOS, avisos de medicamentos y sensores del hogar.
 */
public class AlertEvent {

    // --- Atributos inmutables del evento ---
    private final String tipo;
    private final String data;
    private final String loc;
    private final String hora;

    /**
     * Constructor principal para crear una nueva instancia de evento de alerta.
     *
     * @param tipo Categoria del evento ("SOS", "SALUD", "MEDICINA", "HOGAR").
     * @param data Descripción detallada de la alerta o evento registrado.
     * @param loc Ubicación física del evento (ej. "Casa").
     * @param hora Hora formateada del registro (HH:mm).
     */
    public AlertEvent(String tipo, String data, String loc, String hora) {
        this.tipo = tipo;
        this.data = data;
        this.loc = loc;
        this.hora = hora;
    }

    // --- Métodos Getters ---
    public String getTipo() { return tipo; }
    public String getData() { return data; }
    public String getLoc() { return loc; }
    public String getHora() { return hora; }
}
