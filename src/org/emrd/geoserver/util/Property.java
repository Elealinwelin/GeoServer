/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.emrd.geoserver.util;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

/**
 * Clase que representa una propiedad de un sensor de una base de datos de geo-sensores.
 * Cada dispositivo (BBDD) contiene uno o varios sensores que miden diferentes propiedades.
 * @author Elena
 */
@Entity
public class Property {

    @Id
    public String property; //Propiedad medida por un sensor (en nuestro caso: O3, PM10...)

    @Override
    public String toString() {
        return "Property [property=" + property + "]";
    }

}
