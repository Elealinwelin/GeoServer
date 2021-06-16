/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.emrd.geoserver.util;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

/**
 * Clase que representa un sensor que ha sido actualizado en una base de datos de geo-sensores
 * y almacena tanto la propiedad medida como el tiempo de la última medición. Cada
 * dispositivo (BBDD) contiene uno o varios sensores que miden diferentes propiedades.
 * @author Elena
 */
@Entity
public class LastSensorData {
    public String user; //Dispositivo (ciudad en nuestro caso) que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
    public String sensor; //Nombre de un sensor (o estación) que mide una o varias propiedades
    @Id
    public String property; //Propiedad medida por un sensor (en nuestro caso: O3, PM10...)
    public long maxtime; //Tiempo en milisegundos (desde 1970) en el que un sensor ha realizado su última medición
    
    LastSensorData() {
    }

    public LastSensorData(String user, String sensor, String property, long maxtime) {
        this.user = user;
        this.sensor = sensor;
        this.property = property;
        this.maxtime = maxtime;
    }

    @Override
    public String toString() {
        return "SensorData{" + "user=" + user + ", sensor=" + sensor + ", property=" + property + ", timestamp=" + maxtime + " '}'";
    }

}
