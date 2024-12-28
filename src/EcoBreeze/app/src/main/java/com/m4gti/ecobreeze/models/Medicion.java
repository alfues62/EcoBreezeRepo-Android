package com.m4gti.ecobreeze.models;

/**
 * @class Medicion
 * @brief Clase que representa una medición ambiental.
 *
 * Esta clase almacena los datos de una medición realizada por los sensores, incluyendo el ID de la medición, el valor,
 * la ubicación geográfica (longitud y latitud), la fecha y hora de la medición, la categoría y el tipo de gas medido.
 *
 * Atributos:
 *   - `idMedicion`: ID único de la medición.
 *   - `valor`: Valor numérico de la medición.
 *   - `lon`: Longitud geográfica de la medición.
 *   - `lat`: Latitud geográfica de la medición.
 *   - `fecha`: Fecha de la medición.
 *   - `hora`: Hora de la medición.
 *   - `categoria`: Categoría de la medición.
 *   - `tipoGasId`: ID del tipo de gas medido.
 *   - `tipoGas`: Nombre del tipo de gas medido.
 *
 * @note Utiliza atributos para almacenar y acceder a los datos de la medición.
 */
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
    public double getLon() {return lon;}
    public double getLat() {return lat;}
}

