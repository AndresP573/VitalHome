package com.example.vitalhome.data.modelo;

/**
 * Clase de modelo que representa un medicamento dentro del plan de medicación del usuario.
 * Contiene identificador, nombre, hora programada y estado de toma.
 */
public class Medication {
    // --- Bloque 1: Atributos de la clase ---
    private String id;
    private String nombre;
    private String hora;
    private boolean isTaken;

    // --- Bloque 2: Constructor ---
    /**
     * Constructor de Medication.
     *
     * @param id Identificador único de la medicación
     * @param nombre Nombre de la medicina
     * @param hora Hora asignada para el consumo
     */
    public Medication(String id, String nombre, String hora) {
        this.id = id;
        this.nombre = nombre;
        this.hora = hora;
        this.isTaken = false;
    }

    // --- Bloque 3: Métodos Getters y Setters ---
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getHora() { return hora; }
    public boolean isTaken() { return isTaken; }
    public void setTaken(boolean taken) { isTaken = taken; }
}
