package com.example.vitalhome.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Clase de utilidad centralizada para el formateo y manipulación de fechas y horas.
 * Garantiza thread-safety utilizando ThreadLocal para reutilizar instancias de SimpleDateFormat.
 */
public final class DateUtils {

    private DateUtils() {
        // Constructor privado para prevenir instanciación
    }

    private static final ThreadLocal<SimpleDateFormat> TIME_FORMATTER = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("HH:mm", Locale.getDefault())
    );

    private static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMATTER = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    );

    /**
     * Formatea un objeto Date al formato de hora "HH:mm".
     *
     * @param date Objeto Date a formatear.
     * @return Cadena formateada de hora o "00:00" si el objeto es nulo.
     */
    public static String formatTime(Date date) {
        if (date == null) return "00:00";
        return TIME_FORMATTER.get().format(date);
    }

    /**
     * Formatea un timestamp milisegundos al formato de hora "HH:mm".
     *
     * @param timestamp Milisegundos epoch.
     * @return Cadena formateada de hora.
     */
    public static String formatTime(long timestamp) {
        return formatTime(new Date(timestamp));
    }

    /**
     * Formatea la fecha y hora actual en formato "HH:mm".
     *
     * @return Hora actual formateada.
     */
    public static String getCurrentTimeFormatted() {
        return formatTime(new Date());
    }

    /**
     * Formatea un objeto Date al formato completo "dd/MM/yyyy HH:mm".
     *
     * @param date Objeto Date a formatear.
     * @return Cadena formateada de fecha y hora.
     */
    public static String formatDateTime(Date date) {
        if (date == null) return "";
        return DATE_TIME_FORMATTER.get().format(date);
    }
}
