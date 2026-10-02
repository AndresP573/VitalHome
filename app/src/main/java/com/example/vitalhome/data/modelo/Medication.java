package com.example.vitalhome.data.modelo;

/**
 * Modelo de datos para representar un medicamento dentro del plan de tomas diario.
 * Administra el identificador único, nombre, horario de toma y estado de realización.
 */
public class Medication {

    // --- Atributos del medicamento ---
    private final String id;
    private final String nombre;
    private final String hora;
    private boolean taken;

    /**
     * Constructor completo para inicializar un medicamento.
     *
     * @param id Identificador único del registro.
     * @param nombre Nombre del fármaco o remedio.
     * @param hora Horario programado para la toma (HH:mm).
     * @param taken Estado que indica si ya fue ingerido.
     */
    public Medication(String id, String nombre, String hora, boolean taken) {
        this.id = id;
        this.nombre = nombre;
        this.hora = hora;
        this.taken = taken;
    }

    // --- Métodos Getters y Setters ---
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getHora() { return hora; }
    public boolean isTaken() { return taken; }
    public void setTaken(boolean taken) { this.taken = taken; }
}
