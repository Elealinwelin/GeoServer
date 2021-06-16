/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.emrd.geoserver.loaddata;

import org.emrd.geoserver.util.SensorGroupDate;
import org.emrd.geoserver.util.SensorGeoData;
import dev.morphia.Datastore;
import static dev.morphia.aggregation.experimental.expressions.DateExpressions.month;
import static dev.morphia.aggregation.experimental.expressions.DateExpressions.year;
import static dev.morphia.aggregation.experimental.expressions.Expressions.field;
import static dev.morphia.aggregation.experimental.expressions.AccumulatorExpressions.*;
import dev.morphia.aggregation.experimental.stages.Group;
import static dev.morphia.aggregation.experimental.stages.Group.id;
import dev.morphia.aggregation.experimental.stages.Projection;
import java.util.List;
import org.emrd.geoserver.MyGeoServlet;

/**
 * Clase que contiene la función principal que permite realizar una consulta a la
 * base de datos de geo-sensores del dispositivo (o ciudad) elegido para chequear
 * el mes y año de los datos que se han introducido.
 * @author Elena
 */
public class TimeCheck {

    /**
     * Función principal que realiza una consulta a la base de datos de geo-sensores
     * del dispositivo (o ciudad) elegido para chequear el mes y año de los datos que
     * se han introducido.
     * @param args Lista de argumentos del main
     * @throws Exception Si hay algún problema para acceder a la base de datos
     */
    static public void main(String[] args) throws Exception {
        //Recuperamos la base de datos de nuestro dispositivo (ciudad)
        Datastore datastore = MyGeoServlet.getMongoDataStore(LoadData.city_name);

        {
            List<SensorGroupDate> results = datastore
                    .aggregate(SensorGeoData.class)
                    .project(Projection.of()
                            .include("year", year(field("date")))
                            .include("month", month(field("date")))
                            .include("total", (field("value")))
                    )
                    .group(
                            Group.of(
                                    id()
                                            .field("year")
                                            .field("month")
                            )
                                    .field("total", avg(field("total")))
                                    .field("month", first(field("month")))
                                    .field("year", first(field("year")))
                    )
                    .execute(SensorGroupDate.class).toList();
            
            System.out.println(results.size() + " agrupaciones de sensores por mismo mes y año:");
            for (SensorGroupDate sgd : results) {
                System.out.println(sgd);
            }

        }

    }

}
