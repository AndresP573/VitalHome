package com.example.vitalhome.utils;

/**
 * Umbrales clínicos geriátricos estándar para la evaluación de signos vitales en adultos mayores.
 * Utilizados para determinar si un valor registrado debe generar una alerta clínica o mantenerse
 * exclusivamente como control rutinario en la pulsera.
 */
public class VitalSignsThresholds {

    // Pulso (LPM)
    public static final int PULSO_MIN_NORMAL = 60;
    public static final int PULSO_MAX_NORMAL = 100;

    // Presión arterial (mmHg)
    public static final int SIS_MAX_NORMAL = 139;
    public static final int DIA_MAX_NORMAL = 89;

    // Saturación de oxígeno (%)
    public static final int OXI_MIN_NORMAL = 92;

    /**
     * Evalúa si el pulso se encuentra fuera del rango normal (bajo o elevado).
     */
    public static boolean isPulsoAnomalo(int pulso) {
        return pulso < PULSO_MIN_NORMAL || pulso > PULSO_MAX_NORMAL;
    }

    /**
     * Evalúa si la presión arterial se encuentra elevada.
     */
    public static boolean isPresionAnomala(int sis, int dia) {
        return sis > SIS_MAX_NORMAL || dia > DIA_MAX_NORMAL;
    }

    /**
     * Evalúa si la saturación de oxígeno se encuentra por debajo del rango normal.
     */
    public static boolean isOxigenoAnomalo(int oxi) {
        return oxi < OXI_MIN_NORMAL;
    }
}
