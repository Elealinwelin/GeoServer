/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.emrd.geoserver.loaddata;

import org.emrd.geoserver.util.SensorGeoData;
import dev.morphia.Datastore;
import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.emrd.geoserver.MyGeoServlet;

/**
 * Clase que contiene las clases y métodos necesarios para introducir los datos de los
 * geo-sensores de un dispositivo (o ciudad) contenidos en archivos csv, dentro de una
 * carpeta, en la base de datos MongoDB con el nombre del dispositivo. Hay que tener en
 * cuenta que los archivos csv contienen los datos en bruto con una estructura muy
 * específica que habría que adaptar en caso de querer agregar datos de otras fuentes.
 * @author Elena
 */
public class LoadData {
    static public String city_name = "madrid-air"; //Nombre del dispositivo (ciudad en nuestro caso) que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    
    /**
     * Clase que almacena la información relevante de los sensores o estaciones de
     * un dispositivo que hay en el correspondiente archivo csv.
     */
    static public class Station {
        public String code;
        public String name;
        public float lat;
        public float lon;

        Station(String code, String name, float lat, float lon) {
            this.code = code;
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public String toString() {
            return "Station{" + "name=" + name + ", code=" + code + ", lat=" + lat + ", lon=" + lon + '}';
        }

    }
    
    /**
     * Función que carga los sensores o estaciones de un archivo csv en un objeto hashmap.
     * @param file Ruta de la carpeta donde se encuentra el archivo csv con los datos en bruto de los sensores o estaciones
     * @return Devuelve un objeto hashmap con los datos relevantes de las estaciones del archivo csv cargados
     * @throws Exception Si hay algún problema al leer el archivo
     */
    static public HashMap<String, Station> loadStation(String file) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
        String line = null;
        int count = 0;
        HashMap<String, Station> mapStations = new HashMap<String, Station>();
        //Recorremos el archivo csv línea a línea...
        while ((line = br.readLine()) != null) {
            if (count == 0) { //Nos saltamos la primera línea de cabecera
                count++;
                continue; //Forzamos la siguiente iteración del while
            }
            //Separamos cada dato de una línea
            String[] datos = line.trim().split(";");
            //Guardamos en el hashmap los datos que nos interesan de la línea
            mapStations.put(datos[1], new Station(datos[1], datos[1], Float.parseFloat(datos[25]), Float.parseFloat(datos[24])));
            count++;
        }
        return mapStations;
    }

    /**
     * Función que traduce el código asignado a una propiedad o magnitud medida por un
     * sensor en los datos en bruto de un archivo csv a una abreviatura o fórmula legible.
     * @param code Código asignado a una propiedad o magnitud medida por un sensor en los datos en bruto de un archivo csv
     * @param count Número de línea del archivo donde se ha leído el código
     * @return Devuelve la abreviatura correspondiente al código de propiedad pasado
     * @throws Exception Si el código del archivo es inválido o no está registrado en el programa
     */
    static public String getNameMagnitude(String code, int count) throws Exception {
        switch(code){
//            case "1":
//                return "SO2";
//            case "6":
//                return "CO";
//            case "7":
//                return "NO";
//            case "8":
//                return "NO2";
//            case "9":
//                return "PM2.5";
            case "10":
                return "PM10";
//            case "12":
//                return "NOx";
            case "14":
                return "O3";
//            case "20":
//                return "TOL";
//            case "30":
//                return "BEN";
//            case "35":
//                return "EBE";
//            case "37":
//                return "MXY";
//            case "38":
//                return "PXY";
//            case "39":
//                return "OXY";
//            case "42":
//                return "TCH";
//            case "43":
//                return "CH4";
//            case "44":
//                return "NMHC";
            default:
                System.out.println("Error bad code of magnitude "+code+" #"+count);
        }
        
        return null;
    }

    /**
     * Función que introduce los datos de los sensores contenidos en el archivo csv
     * pasado en la base de datos del dispositivo o ciudad predeterminado.
     * @param file Ruta del archivo csv que contiene los datos en bruto de los sensores o estaciones
     * @param stations Hashmap cargado con la información relevante de los sensores o estaciones del dispositivo (ciudad)
     * @throws Exception Si hay algún problema al leer el archivo o si se encuentra algún dato inválido en el archivo
     */
    static public void loadSensorData(String file, HashMap<String, Station> stations) throws Exception {
        //Recuperamos la base de datos de nuestro dispositivo (ciudad)
        Datastore datastore = MyGeoServlet.getMongoDataStore(city_name);

        System.out.println("Cargando datos de sensores del archivo: " + file + " ...");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
        String line = null;

        List<SensorGeoData> listaDatosSensores = new LinkedList<SensorGeoData>();
        int count = 0;
        //Recorremos el archivo csv línea a línea...
        while ((line = br.readLine()) != null) {
            if (count == 0) { //Nos saltamos la primera línea de cabecera
                count++;
                continue; //Forzamos la siguiente iteración del while
            }
            //Separamos cada dato de una línea
            String[] datos = line.trim().split(";");
            //Guardamos los datos de la línea
            String provincia = datos[0];
            String municipio = datos[1];
            String estacion = datos[2];
            String magnitud = getNameMagnitude(datos[3], count);

            //Validamos los datos y filtramos los que puedan ocasionar errores
            if (!provincia.equals("28")) {
                throw new Exception("Bad code 0 : " + provincia + " #" + count);
            }
            if (!municipio.equals("079")) {
                throw new Exception("Bad code 1 : " + municipio + " #" + count);
            }

            System.out.println("propiedad: " + magnitud + " #" + count);
            if (magnitud == null) {
                count++;
                continue; //Forzamos la siguiente iteración del while
            }
            
            Station station = stations.get(estacion);
            System.out.println("estación: " + station + " #" + count);
            if (station == null) {
                count++;
                continue; //Forzamos la siguiente iteración del while
            }

            //Guardamos la fecha
            String s_date = datos[5] + "/" + datos[6] + "/" + datos[7] + " ";

            //Recorremos los valores de la línea medidos por los sensores cada hora en esa fecha...
            for (int h = 0; h < 24; h++) {
                int iH = 8 + 2 * h; //Posición donde empiezan los valores en la línea
                //Validamos que haya algún valor
                if (!datos[iH + 1].equals("V")) {
                    System.out.println("\t Error hour validation code " + (iH + 1));
                    continue; //Forzamos la siguiente iteración del for
                }
                //Guardamos la hora
                String h_date = s_date + (h + 1) + ":00";

                //Guardamos el valor
                float valor = Float.parseFloat(datos[iH]);
                
                //Creamos un objeto donde guardamos los datos del sensor
                SensorGeoData sensor = new SensorGeoData(city_name, estacion, magnitud, valor, dateFormat.parse(h_date).getTime(), station.lat, station.lon);
                //Lo añadimos a la lista
                listaDatosSensores.add(sensor);
                System.out.println("\t" + dateFormat.parse(h_date) + ": " + sensor);
            }

            //Guardamos los datos de los sensores del archivo en la base de datos MongoDB de nuestro dispositivo
            datastore.save(listaDatosSensores);

            count++;
        }
        
    }

    /**
     * Función principal que introduce los datos de geo-sensores contenidos en archivos csv,
     * dentro de una carpeta, en la base de datos en MongoDB del dispositivo (ciudad) elegido.
     * @param args Lista de argumentos del main
     * @throws Exception Si hay algún problema con la lectura de archivos csv o con datos inválidos
     */
    static public void main(String[] args) throws Exception {
        String path = "D:\\aire\\"; //Ruta de la carpeta raíz donde se guardan los datos de los sensores de un dispositivo, en nuestro caso, la ciudad de Madrid
        String csvInfoEstaciones = "informacion_estaciones_red_calidad_aire.csv"; //Nombre del archivo csv que contiene la información en bruto de los sensores o estaciones del dispositivo
        
        //Cargamos los datos relevantes de los sensores del archivo cvs a un hashmap
        HashMap<String, Station> stations = loadStation(path + csvInfoEstaciones);
        System.out.println("Estaciones (o sensores) del archivo csv cargadas: " + stations);

        File carpetaDispositivo = new File(path);
        String[] archivosCarpeta = carpetaDispositivo.list();
        //Validamos que la carpeta de la ruta principal no esté vacía
        if (archivosCarpeta == null || archivosCarpeta.length == 0) {
            System.out.println("No hay datos dentro de la carpeta " + path);
            return;
        }
        else {
            //Recorremos las subcarpetas de la carpeta raíz...
            for (int i = 0; i < archivosCarpeta.length; i++) {
                File carpetaAnio = new File(path + archivosCarpeta[i]);
                if(carpetaAnio.isDirectory()){ //Comprobamos que es una carpeta
                    //Recorremos los archivos csv de la subcarpeta...
                    FilenameFilter filter = (File f, String name) -> name.endsWith(".csv");
                    for (File file : carpetaAnio.listFiles(filter)) {
                        //Cargamos los datos de los sensores del csv en la BBDD
                        loadSensorData(path + archivosCarpeta[i] + "\\" + file.getName(), stations);
                    }
                }
            }
        }

    }
    
}
