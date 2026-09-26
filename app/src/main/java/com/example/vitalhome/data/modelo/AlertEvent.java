package com.example.vitalhome.data.modelo;

/**
 * Clase que representa un evento de alerta en el sistema VitalHome.
 * Almacena la información de tipo, datos adicionales, ubicación y hora del evento.
 */
public class AlertEvent {
    // --- Bloque 1: Atributos de la clase ---
    private String tipo;
    private String data;
    private String loc;
    private String hora;

    // --- Bloque 2: Constructor ---
    /**
     * Constructor de AlertEvent.
     *
     * @param tipo Tipo de alerta (ej. SOS, SALUD, MEDICINA)
     * @param data Descripción o detalle del evento
     * @param loc Ubicación geográfica o contextual
     * @param hora Hora de ocurrencia del evento
     */
    public AlertEvent(String tipo, String data, String loc, String hora) {
        this.tipo = tipo;
        this.data = data;
        this.loc = loc;
        this.hora = hora;
    }

    // --- Bloque 3: Métodos Getters ---
    public String getTipo() { return tipo; }
    public String getData() { return data; }
    public String getLoc() { return loc; }
    public String getHora() { return hora; }
}
