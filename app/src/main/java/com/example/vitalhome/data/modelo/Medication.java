package com.example.vitalhome.data.modelo;

public class Medication {
    private String id;
    private String nombre;
    private String hora;
    private boolean isTaken;

    public Medication(String id, String nombre, String hora) {
        this.id = id;
        this.nombre = nombre;
        this.hora = hora;
        this.isTaken = false;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getHora() { return hora; }
    public boolean isTaken() { return isTaken; }
    public void setTaken(boolean taken) { isTaken = taken; }
}