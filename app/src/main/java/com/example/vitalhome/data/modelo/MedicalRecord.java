package com.example.vitalhome.data.modelo;

/**
 * Modelo de datos que representa una entrada del historial clínico del paciente.
 * Almacena información de consultas anteriores, diagnósticos y observaciones médicas.
 */
public class MedicalRecord {

    // --- Atributos inmutables del registro médico ---
    private final String fecha;
    private final String titulo;
    private final String doctor;
    private final String detalle;

    /**
     * Constructor para la creación de un registro médico.
     *
     * @param fecha Fecha de la consulta o estudio realizado.
     * @param titulo Título de la consulta o nombre de la especialidad.
     * @param doctor Nombre del profesional o centro médico.
     * @param detalle Diagnóstico, receta u observaciones registradas.
     */
    public MedicalRecord(String fecha, String titulo, String doctor, String detalle) {
        this.fecha = fecha;
        this.titulo = titulo;
        this.doctor = doctor;
        this.detalle = detalle;
    }

    // --- Métodos Getters ---
    public String getFecha() { return fecha; }
    public String getTitulo() { return titulo; }
    public String getDoctor() { return doctor; }
    public String getDetalle() { return detalle; }
}
