package com.m4gti.ecobreeze.models;

/**
 * @class Notificacion
 * @brief Clase que representa una notificación.
 *
 * Esta clase almacena los datos de una notificación, incluyendo el ID, el título, el cuerpo y la fecha de la notificación.
 *
 * Atributos:
 *   - `idNotificacion`: Identificador único de la notificación.
 *   - `titulo`: Título de la notificación.
 *   - `cuerpo`: Cuerpo o mensaje de la notificación.
 *   - `fecha`: Fecha en la que se recibió la notificación.
 *
 * @note Utiliza atributos para almacenar y acceder a los datos de la notificación.
 */
public class Notificacion {
    private int idNotificacion;
    private String titulo;
    private String cuerpo;
    private String fecha;

    public Notificacion(int idNotificacion, String titulo, String cuerpo, String fecha) {
        this.idNotificacion = idNotificacion;
        this.titulo = titulo;
        this.cuerpo = cuerpo;
        this.fecha = fecha;
    }

    public int getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(int idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

}

