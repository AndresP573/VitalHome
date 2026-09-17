package com.example.vitalhome.modelo;

public class MedicalRecord {
    private String fecha;
    private String titulo;
    private String doctor;
    private String detalle;

    public MedicalRecord(String fecha, String titulo, String doctor, String detalle) {
        this.fecha = fecha;
        this.titulo = titulo;
        this.doctor = doctor;
        this.detalle = detalle;
    }

    public String getFecha() { return fecha; }
    public String getTitulo() { return titulo; }
    public String getDoctor() { return doctor; }
    public String getDetalle() { return detalle; }
}