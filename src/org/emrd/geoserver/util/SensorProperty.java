/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.emrd.geoserver.util;

import java.util.List;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

/**
 * Clase que almacena las propiedades que mide un sensor almacenado en una base de
 * datos de geo-sensores y su localización. Cada dispositivo (BBDD) contiene uno o
 * varios sensores que miden diferentes propiedades.
 * @author Elena
 */
@Entity
public class SensorProperty {
    @Id
    public String id; //Identificador único de un sensor en una base de datos de geo-sensores
    public String sensor; //Nombre de un sensor (o estación) que mide una o varias propiedades
    public List<String> properties; //Lista de todas las propiedades que es capaz de medir un sensor (en nuestro caso: O3, PM10...)
    public Float lat; //Latitud de la localización de un sensor
    public Float lon; //Longitud de la localización de un sensor

    @Override
    public String toString() {
        return "SensorProperty{" + "id=" + id + ", sensor=" + sensor + ", properties=" + properties + ", lat=" + lat + ", lon=" + lon + '}';
    }

}
