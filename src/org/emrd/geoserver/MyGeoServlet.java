/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.emrd.geoserver;

import org.emrd.geoserver.util.SensorProperty;
import org.emrd.geoserver.util.Property;
import org.emrd.geoserver.util.LastSensorData;
import org.emrd.geoserver.util.SensorGeoData;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import static dev.morphia.aggregation.experimental.expressions.AccumulatorExpressions.*;
import static dev.morphia.aggregation.experimental.expressions.Expressions.*;
import dev.morphia.aggregation.experimental.stages.Group;
import dev.morphia.aggregation.experimental.stages.Unset;
import dev.morphia.query.experimental.filters.Filters;
import static dev.morphia.aggregation.experimental.stages.Group.*;
import dev.morphia.query.experimental.filters.Filter;
import java.util.*;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

/**
 * Clase que integra los servicios REST de nuestra API usando Jersey.
 * @author Elena
 */
@Path("MyGeoServlet")
public class MyGeoServlet {

    /**
     * Servicio para insertar nuevos datos de un sensor.
     * @param device Dispositivo que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
     * @param sensor Nombre de un sensor que mide una o varias propiedades
     * @param property Propiedad medida por un sensor
     * @param value Valor medido por un sensor de una propiedad concreta
     * @param timestamp Tiempo en milisegundos en el que un sensor ha realizado una medición
     * @param lat Latitud de la localización de un sensor
     * @param lon Longitud de la localización de un sensor
     * @return Respuesta del servidor HTTP
     */
    @GET
    @Path("/insert/{device}/{sensor}/{property}/{value}/{timestamp}/{lat}/{lon}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertSensorData(@PathParam("device") String device,
            @PathParam("sensor") String sensor,
            @PathParam("property") String property,
            @PathParam("value") Float value,
            @PathParam("timestamp") Long timestamp,
            @PathParam("lat") Float lat,
            @PathParam("lon") Float lon
    ) {

        try {
            SensorGeoData data = new SensorGeoData(device, sensor, property, value, timestamp, lat, lon);
            getMongoDataStore(device).save(data);
            System.out.println("Insert data: " + data);
            
            return Response.ok()
                    .entity(data)
                    .build();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return Response.status(Response.Status.NOT_FOUND)
                .build();
    }

    /**
     * Servicio para consultar los datos de un dispositivo (ciudad) concreto actualizados
     * en los últimos X milisegundos.
     * @param device Dispositivo que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
     * @param offtime Período de tiempo atrás en milisegundos a partir del cual queremos mostrar los datos consultados
     * @return Respuesta del servidor HTTP
     */
    @GET
    @Path("/query_device_time/{device}/{offtime}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response querySensorDataTime(
            @PathParam("device") String device,
            @PathParam("offtime") Long offtime
    ) {

        try {
            List<SensorGeoData> sensors = getMongoDataStore(device)
                    .find(SensorGeoData.class)
                    .filter(
                            Filters.gt("timestamp", getCurrentTime() - offtime),
                            Filters.eq("device", device)
                    )
                    .iterator()
                    .toList();
            System.out.println("Call to /query_device_time/" + device + "/" + offtime);

            return Response.ok()
                    .entity(sensors)
                    .build();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return Response.status(Response.Status.NOT_FOUND)
                .build();
    }

    /**
     * Servicio para consultar los datos de una propiedad concreta de un determinado
     * sensor perteneciente a un dispositivo (ciudad) específico actualizado en los
     * últimos X milisegundos.
     * @param device Dispositivo que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
     * @param sensor Nombre de un sensor que mide una o varias propiedades
     * @param property Propiedad medida por un sensor
     * @param offtime Período de tiempo atrás en milisegundos a partir del cual queremos mostrar los datos consultados
     * @return Respuesta del servidor HTTP
     */
    @GET
    @Path("/query_device_sensor_property_time/{device}/{sensor}/{property}/{offtime}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response querySensorData(
            @PathParam("device") String device,
            @PathParam("sensor") String sensor,
            @PathParam("property") String property,
            @PathParam("offtime") Long offtime
    ) {

        try {
            List<SensorGeoData> sensors = getMongoDataStore(device)
                    .find(SensorGeoData.class)
                    .filter(
                            Filters.eq("sensor", sensor)
                    )
                    .filter(
                            Filters.eq("property", property)
                    )
                    .filter(
                            Filters.gte("timestamp", getCurrentTime() - offtime)
                    )
                    .filter(
                            Filters.lte("timestamp", getCurrentTime())
                    )
                    .iterator()
                    .toList();
            System.out.println("Call to /query_device_sensor_property_time/" + device + "/" + sensor + "/" + property + "/" + offtime);

            return Response.ok()
                    .entity(sensors)
                    .build();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return Response.status(Response.Status.NOT_FOUND)
                .build();
    }

    /**
     * Servicio para consultar las propiedades de un sensor concreto perteneciente
     * a un determinado dispositivo (ciudad).
     * @param device Dispositivo que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
     * @param sensor Nombre de un sensor que mide una o varias propiedades
     * @return Respuesta del servidor HTTP
     */
    @GET
    @Path("/query_sensor_properties/{device}/{sensor}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response querySensorData(
            @PathParam("device") String device,
            @PathParam("sensor") String sensor
    ) {

        try {
            Datastore datastore = getMongoDataStore(device);

            List<Property> properties = datastore.aggregate(SensorGeoData.class)    //cogemos todos los sensores de la BBDD
                    .match(Filters.eq("device", device))                            //filtramos por mismo device
                    .match(Filters.eq("sensor", sensor))                            //y sensor de los parámetros
                    .group(of(id("property"))                                       //agrupamos por las diferentes propiedades
                            .field("property", addToSet(field("property"))))        //lo añadimos a la clase Property (con sus campos)
                    .unset(Unset.fields("property"))                                //deshacemos el set de propiedades para separarlas
                    .execute(Property.class)                                        //creamos la clase Property
                    .toList();                                                      //lo devolvemos en una lista
            
            List<String> ret = new LinkedList<>();
            System.out.print("Call to /query_sensor_properties/" + device + "/" + sensor + ": ");
            for (Property p : properties) {
                ret.add(p.property);
                System.out.print("'" + p.property + "' ");
            }
            System.out.println("");
            
            return Response.ok()
                    .entity(ret)
                    .build();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return Response.status(Response.Status.NOT_FOUND)
                .build();
    }

    /**
     * Servicio para consultar los datos de las últimas actualizaciones de los sensores
     * de un determinado dispositivo (ciudad).
     * @param device Dispositivo que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
     * @return Respuesta del servidor HTTP
     */
    @GET
    @Path("/query_snapshot/{device}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSnapshot(
            @PathParam("device") String device) {

        try {
            Datastore datastore = getMongoDataStore(device);
            List<LastSensorData> lastData = datastore
                    .aggregate(SensorGeoData.class)
                    .match(Filters.lte("timestamp", getCurrentTime()))  //Este filtro no hace falta si el tiempo no es simulado
                    .group(
                            Group.of(id().field("device").field("sensor").field("property"))
                                    .field("maxtime", max(field("timestamp")))
                                    .field("sensor", first(field("sensor")))
                                    .field("property", first(field("property")))
                                    .field("user", first(field("device")))
                    )
                    .execute(LastSensorData.class)
                    .toList();

            List<SensorGeoData> sensors = getSensorFromTimestamp(datastore, lastData);
            System.out.println("Call to /query_snapshot/" + device);

            return Response.ok()
                    .entity(sensors)
                    .build();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return Response.status(Response.Status.NOT_FOUND)
                .build();
    }

    /**
     * Servicio para consultar los datos de las últimas actualizaciones de aquellos
     * sensores que miden una propiedad concreta en un determinado dispositivo (ciudad).
     * @param device Dispositivo que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
     * @param property Propiedad medida por un sensor
     * @return Respuesta del servidor HTTP
     */
    @GET
    @Path("/query_snapshot/{device}/{property}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSnapshot(
            @PathParam("device") String device,
            @PathParam("property") String property
    ) {

        try {
            Datastore datastore = getMongoDataStore(device);
            List<LastSensorData> lastData = datastore
                    .aggregate(SensorGeoData.class)
                    .match(Filters.eq("property", property))
                    .group(
                            Group.of(id().field("device").field("sensor").field("property"))
                                    .field("maxtime", max(field("timestamp")))
                                    .field("sensor", first(field("sensor")))
                                    .field("property", first(field("property")))
                                    .field("user", first(field("device")))
                    )
                    .execute(LastSensorData.class)
                    .toList();

            List<SensorGeoData> sensors = getSensorFromTimestamp(datastore, lastData);
            System.out.println(" Call to /query_snapshot/" + device + "/" + property);

            return Response.ok()
                    .entity(sensors)
                    .build();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return Response.status(Response.Status.NOT_FOUND)
                .build();
    }
    
    /**
     * Servicio para consultar las propiedades de los sensores de un determinado
     * dispositivo (ciudad).
     * @param device Dispositivo que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
     * @return Respuesta del servidor HTTP
     */
    @GET
    @Path("/query_sensor_properties/{device}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response querySensorProperty(
            @PathParam("device") String device
    ) {

        try {
            Datastore datastore = getMongoDataStore(device);
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
            System.out.println("Call to /query_sensor_properties/" + device);

            return Response.ok()
                    .entity(properties)
                    .build();
            
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
        return Response.status(Response.Status.NOT_FOUND)
                .build();
    }
    
    /**
     * Función que devuelve el tiempo actual (simulado).
     * @return Devuelve el tiempo actual (simulado) en milisegundos
     */
    public static long getCurrentTime(){
        //return System.currentTimeMillis(); // Tiempo actual (no simulado)

        //Simulamos el tiempo actual
        Calendar tiempo = Calendar.getInstance();
        //tiempo.set(2020, 0, 22, 0, 0, 0); //22 de enero de 2020 a las 00:00:00
        //tiempo.set(2019, 5, 4, 12, 0, 0); //4 de junio de 2019 a las 12:00:00
        tiempo.set(2020, 3, 30, 14, 0, 0); //30 de abril de 2020 a las 00:00:00
        System.out.println("Simulated current time: " + tiempo.getTime());
        
        return tiempo.getTimeInMillis();
    }

    /**
     * Función que, dada una lista con los últimos sensores actualizados en la base
     * de datos de un dispositivo determinado, extrae los últimos datos introducidos
     * por dichos sensores.
     * @param datastore Objeto que permite manejar la base de datos del dispositivo al que pertenecen los sensores pasados
     * @param lastData Lista de los últimos sensores actualizados de un dispositivo determinado
     * @return Devuelve una lista de SensorGeoData con los últimos datos introducidos en la base de datos por los sensores pasados
     */
    private List<SensorGeoData> getSensorFromTimestamp(Datastore datastore, List<LastSensorData> lastData) {

        Filter[] fList = new Filter[lastData.size()];
        int i = 0;
        for (LastSensorData p : lastData) {
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

        return sensors;
    }

    /**
     * Función que crea el objeto que da acceso a la base de datos de MongoDB con
     * el nombre del dispositivo pasado por parámetro.
     * @param device Dispositivo que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
     * @return Devuelve un objeto Datastore que permite manejar la base de datos del dispositivo especificado
     * @throws Exception Si hay algún problema con la BBDD
     */
    public static Datastore getMongoDataStore(String device) throws Exception {
        Datastore datastore = Morphia.createDatastore(MongoClients.create(), device + "Z0");
        datastore.getMapper().mapPackage("org.jmq.sensordata");
        datastore.ensureIndexes();

        return datastore;
    }
    
    /**
     * Función que genera un texto de bienvenida para la ruta principal de los servicios REST.
     * @return Devuelve un texto de bienvenida
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessage() {
        return "Hi from REST services in geo data sensor. \n";
    }
    
}
