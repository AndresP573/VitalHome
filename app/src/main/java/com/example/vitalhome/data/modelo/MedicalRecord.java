package com.example.vitalhome.data.modelo;

/**
 * Clase de modelo que representa un registro médico en el historial clínico del usuario.
 * Contiene información sobre la fecha, título, médico tratante y detalles de la consulta o diagnóstico.
 */
public class MedicalRecord {
    // --- Bloque 1: Atributos de la clase ---
    private String fecha;
    private String titulo;
    private String doctor;
    private String detalle;

    // --- Bloque 2: Constructor ---
    /**
     * Constructor de MedicalRecord.
     *
     * @param fecha Fecha de la atención o registro médico
     * @param titulo Título o diagnóstico principal
     * @param doctor Nombre del médico responsable
     * @param detalle Descripción detallada o indicaciones del registro
     */
    public MedicalRecord(String fecha, String titulo, String doctor, String detalle) {
        this.fecha = fecha;
        this.titulo = titulo;
        this.doctor = doctor;
        this.detalle = detalle;
    }

    // --- Bloque 3: Métodos Getters ---
    public String getFecha() { return fecha; }
    public String getTitulo() { return titulo; }
    public String getDoctor() { return doctor; }
    public String getDetalle() { return detalle; }
}
