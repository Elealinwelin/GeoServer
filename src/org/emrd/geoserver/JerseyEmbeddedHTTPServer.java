/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.emrd.geoserver;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Clase que contiene la función principal que despliega los servicios de nuestra
 * API REST usando un servidor Jetty para ello.
 * @author Elena
 */
@SuppressWarnings("restriction")
public class JerseyEmbeddedHTTPServer {

    /**
     * Función principal que despliega los servicios REST usando un servidor Jetty
     * @param args Lista de argumentos del main
     * @throws Exception Si hay algún problema al lanzar el servidor
     */
    static public void main(String[] args) throws Exception {

        URI baseUri = UriBuilder.fromUri("http://192.168.0.24/").port(8092).build(); //IP del portátil
        ResourceConfig config = new ResourceConfig(MyGeoServlet.class);
        config.register(JacksonFeature.class);
        Server server = JettyHttpContainerFactory.createServer(baseUri, config);

        System.out.println("Server is active");
        server.start();
    }
    
}
