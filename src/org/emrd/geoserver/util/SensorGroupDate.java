/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.emrd.geoserver.util;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

/**
 * Clase que representa la media de los datos almacenados por todos sensores de una
 * base de datos de geo-sensores en un mes y año determinados. Cada dispositivo (BBDD)
 * contiene uno o varios sensores que miden diferentes propiedades.
 * @author Elena
 */
@Entity
public class SensorGroupDate {
    @Id
    public String id; //Identificador único del objeto
    public Integer year; //Año de los datos almacenados en un dispositivo (BBDD)
    public Integer month; //Mes de los datos almacenados en un dispositivo (BBDD)
    public Float total; //Media de los valores medidos por los sensores de un dispositivo (BBDD) en este mes/año

    @Override
    public String toString() {
        return "SensorGroupDate{" + id + ", year=" + year + ", month=" + month + ", total=" + total + '}';
    }

}
