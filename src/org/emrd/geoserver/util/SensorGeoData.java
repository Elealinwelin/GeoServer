/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.emrd.geoserver.util;

import java.util.Objects;
import org.bson.types.ObjectId;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.Indexes;
import java.util.Date;

/**
 * Clase que almacena los datos de un sensor de una base de datos de geo-sensores.
 * Cada dispositivo (BBDD) contiene uno o varios sensores que miden diferentes propiedades.
 * @author Elena
 */
@Entity
@Indexes({
    @Index(fields = {
        @Field("timestamp"),
        @Field("sensor"),
        @Field("property"),
        @Field("lat"),
        @Field("lon")})})
public class SensorGeoData {
    @Id
    private ObjectId id; //Identificador único de un sensor en una base de datos de geo-sensores
    public String device; //Dispositivo (ciudad en nuestro caso) que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
    public String sensor; //Nombre de un sensor (o estación) que mide una o varias propiedades
    public String property; //Propiedad medida por un sensor (en nuestro caso: O3, PM10...)
    public float value; //Valor medido por un sensor de una propiedad concreta
    public long timestamp; //Tiempo en milisegundos (desde 1970) en el que un sensor ha realizado una medición
    public Date date; //Fecha y hora en el que un sensor ha realizado una medición
    public Float lat; //Latitud de la localización de un sensor
    public Float lon; //Longitud de la localización de un sensor

    public SensorGeoData() {}

    public SensorGeoData(String device, String sensor, String property, float value, long timestamp, float lat, float lon) {
        this.device = device;
        this.sensor = sensor;
        this.property = property;
        this.value = value;
        this.date = new Date(timestamp);
        this.timestamp = timestamp;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "SensorData{" + "device=" + device + ", sensor=" + sensor + ", property=" + property + ", value=" + value + ", timestamp=" + timestamp + ", date=" + date + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.device);
        hash = 89 * hash + Objects.hashCode(this.sensor);
        hash = 89 * hash + Objects.hashCode(this.property);
        hash = 89 * hash + Float.floatToIntBits(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SensorGeoData other = (SensorGeoData) obj;
        if (Float.floatToIntBits(this.value) != Float.floatToIntBits(other.value)) {
            return false;
        }
        if (!Objects.equals(this.device, other.device)) {
            return false;
        }
        if (!Objects.equals(this.sensor, other.sensor)) {
            return false;
        }
        if (!Objects.equals(this.property, other.property)) {
            return false;
        }
        return true;
    }

}
