/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.emrd.geoserver.loaddata;

import org.emrd.geoserver.util.SensorProperty;
import org.emrd.geoserver.util.Property;
import org.emrd.geoserver.util.LastSensorData;
import org.emrd.geoserver.util.SensorGeoData;
import dev.morphia.Datastore;
import static dev.morphia.aggregation.experimental.expressions.AccumulatorExpressions.*;
import static dev.morphia.aggregation.experimental.expressions.Expressions.*;
import dev.morphia.aggregation.experimental.stages.Group;
import dev.morphia.query.experimental.filters.Filters;
import static dev.morphia.aggregation.experimental.stages.Group.*;
import dev.morphia.query.experimental.filters.Filter;
import java.util.*;
import java.util.List;
import org.emrd.geoserver.MyGeoServlet;

/**
 * Clase que contiene la función principal que permite realizar una serie de consultas
 * a la base de datos de geo-sensores del dispositivo (o ciudad) elegido para chequear
 * que funciona bien y que los datos se han introducido correctamente.
 * @author Elena
 */
public class LoadDataCheck {

    /**
     * Función principal que realiza una serie de consultas a la base de datos de
     * geo-sensores del dispositivo (o ciudad) elegido para chequear que funciona
     * bien y que los datos se han introducido correctamente.
     * @param args Lista de argumentos del main
     * @throws Exception Si hay algún problema para acceder a la base de datos
     */
    static public void main(String[] args) throws Exception {
        //Recuperamos la base de datos de nuestro dispositivo (ciudad)
        Datastore datastore = MyGeoServlet.getMongoDataStore(LoadData.city_name);

        //Obtenemos las propiedades de los sensores de la BBDD y sus clases
        {
            List<Property> properties = datastore
                    .aggregate(SensorGeoData.class)
                    .group(of(id("property"))
                            .field("property", first(field("property"))))
                    .execute(Property.class)
                    .toList();

            System.out.println("\n----Propiedades de los sensores de la BBDD y sus clases----");
            for (Property p : properties) {
                System.out.println("'" + p.property + "' @" + p.getClass());
            }
        }

        //Obtenemos los sensores y sus propiedades
        {
            List<SensorProperty> properties = datastore
                    .aggregate(SensorGeoData.class)
                    .group(
                            of(id("sensor"))
                                    .field("sensor", first(field("sensor")))
                                    .field("properties", addToSet(field("property")))
                                    .field("lat", first(field("lat")))
                                    .field("lon", first(field("lon")))
                    )
                    .execute(SensorProperty.class)
                    .toList();

            System.out.println("\n----------------Sensores y sus propiedades----------------");
            for (SensorProperty p : properties) {
                System.out.println("'" + p + "'");
                for (String s : p.properties) {
                    System.out.println("\t'" + s + "'");
                }
            }
        }

        //Obtenemos la última actualización de las propiedades de los sensores
        {
            List<LastSensorData> properties = datastore
                    .aggregate(SensorGeoData.class)
                    .group(
                            Group.of(id().field("device").field("sensor").field("property"))
                                    .field("maxtime", max(field("timestamp")))
                                    .field("sensor", first(field("sensor")))
                                    .field("property", first(field("property")))
                                    .field("user", first(field("device")))
                    )
                    .execute(LastSensorData.class)
                    .toList();

            System.out.println("\n----Última actualización por propiedad de los sensores----");
            Filter[] fList = new Filter[properties.size()];
            int i = 0;
            for (LastSensorData p : properties) {
                System.out.println("sensor: " + p.sensor + ", property: '" + p.property + "' -> " + new Date(p.maxtime));
                fList[i] = Filters.and(
                        Filters.eq("sensor", p.sensor),
                        Filters.eq("property", p.property),
                        Filters.eq("timestamp", p.maxtime)
                );
                i++;
            }

            List<SensorGeoData> sensors = datastore
                    .find(SensorGeoData.class)
                    .filter(
                            Filters.or(
                                    fList
                            )
                    )
                    .iterator()
                    .toList();
            System.out.println("Datos de los sensores actualizados: " + sensors);
        }
        System.exit(0);
    }

}
