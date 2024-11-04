package com.m4gti.ecobreeze.models;

import java.sql.Time;
import java.util.Date;

public class Medicion {
    int id;
    float valorO3;
    String Lon;
    String Lat;
    Date fecha;
    Time hora;
    int tipoGas;

    public Medicion(int id, float valorO3, String lon, String lat, Date fecha, Time hora, int tipoGas) {
        this.id = id;
        this.valorO3 = valorO3;
        Lon = lon;
        Lat = lat;
        this.fecha = fecha;
        this.hora = hora;
        this.tipoGas = tipoGas;
    }

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public float getValorO3() {return valorO3;}

    public void setValorO3(float valorO3) {this.valorO3 = valorO3;}

    public String getLon() {return Lon;}

    public void setLon(String lon) {Lon = lon;}

    public String getLat() {return Lat;}

    public void setLat(String lat) {Lat = lat;}

    public Date getFecha() {return fecha;}

    public void setFecha(Date fecha) {this.fecha = fecha;}

    public Time getHora() {return hora;}

    public void setHora(Time hora) {this.hora = hora;}

    public int getTipoGas() {return tipoGas;}

    public void setTipoGas(int tipoGas) {this.tipoGas = tipoGas;}

    @Override
    public String toString() {
        return "Medicion{" +
                "id=" + id +
                ", valorO3=" + valorO3 +
                ", Lon='" + Lon + '\'' +
                ", Lat='" + Lat + '\'' +
                ", fecha=" + fecha +
                ", hora=" + hora +
                ", tipoGas=" + tipoGas +
                '}';
    }
}
