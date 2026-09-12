package com.example.vitalhome;

public class AlertEvent {
    private String tipo; // SOS, SALUD, MOVIMIENTO, MEDICINA
    private String data;
    private String loc;
    private String hora;

    public AlertEvent(String tipo, String data, String loc, String hora) {
        this.tipo = tipo;
        this.data = data;
        this.loc = loc;
        this.hora = hora;
    }

    public String getTipo() { return tipo; }
    public String getData() { return data; }
    public String getLoc() { return loc; }
    public String getHora() { return hora; }
}