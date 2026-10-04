package com.example.vitalhome.utils;

/**
 * Utilidad de seguridad y saneamiento de entradas (Input Sanitization)
 * para prevenir inyecciones NoSQL/SQL, desbordamientos y corrupción de datos en Room y Cloud Firestore.
 */
public class SecurityUtils {

    /**
     * Sanea y limpia una cadena de entrada de texto eliminando caracteres de control o inyecciones maliciosas.
     *
     * @param input Texto original ingresado por el usuario.
     * @return Texto limpio y seguro.
     */
    public static String sanitizeString(String input) {
        if (input == null) return "";
        return input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                    .replace("$", "")
                    .replace("{", "")
                    .replace("}", "")
                    .trim();
    }

    /**
     * Sanea un número telefónico permitiendo únicamente dígitos y símbolos válidos de marcación.
     */
    public static String sanitizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9+\\-\\s()]", "").trim();
    }
}
