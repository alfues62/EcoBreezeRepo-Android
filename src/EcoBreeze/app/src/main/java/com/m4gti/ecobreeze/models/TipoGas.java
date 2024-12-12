package com.m4gti.ecobreeze.models;

/**
 * @enum TipoGas
 * @brief Enum que representa los diferentes tipos de gases medidos.
 *
 * Este enum contiene los tipos de gases como C02, CH4, O3 y SO2. Cada tipo de gas tiene una representación textual asociada.
 *
 * Atributos:
 *   - `texto`: Texto que representa el tipo de gas.
 *
 * Métodos:
 *   - `getTexto()`: Obtiene la representación textual del tipo de gas.
 *   - `getNombres()`: Devuelve una lista de las representaciones textuales de todos los tipos de gas.
 *
 * @note Los valores del enum pueden utilizarse para identificar los distintos tipos de gases en la aplicación.
 */
public enum TipoGas {
    C02("CO₂"),
    CH4("CH₄"),
    O3("O₃"),
    SO2("SO₂");

    private final String texto;

    TipoGas(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    public static String[] getNombres() {
        String[] resultado = new String[TipoGas.values().length];
        for (TipoGas tipo : TipoGas.values()) {
            resultado[tipo.ordinal()] = tipo.texto;
        }
        return resultado;
    }
}