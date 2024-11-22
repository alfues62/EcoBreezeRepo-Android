package com.m4gti.ecobreeze.models;

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