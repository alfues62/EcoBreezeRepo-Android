package com.m4gti.ecobreeze.models;

import java.sql.Time;
import java.util.Date;

public class Medicion {
    private int idMedicion;
    private double valor;
    private double lon;
    private double lat;
    private String fecha;
    private String hora;
    private String categoria;
    private int tipoGasId;
    private String tipoGas;

    // Constructor
    public Medicion(int idMedicion, double valor, double lon, double lat, String fecha, String hora, String categoria, int tipoGasId, String tipoGas) {
        this.idMedicion = idMedicion;
        this.valor = valor;
        this.lon = lon;
        this.lat = lat;
        this.fecha = fecha;
        this.hora = hora;
        this.categoria = categoria;
        this.tipoGasId = tipoGasId;
        this.tipoGas = tipoGas;
    }

    public int getIdMedicion() {
        return idMedicion;
    }

    public double getValor() {
        return valor;
    }

    public String getCategoria() {
        return categoria;  // Getter para categoría
    }
    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

}

