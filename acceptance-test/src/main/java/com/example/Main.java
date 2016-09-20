package com.example;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import java.io.IOException;
import java.net.URI;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:9080/myapp/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {

        SystemRepository.load(new DiContainer(
                new XmlComponentDefinitionLoader("com/example/componentes.xml")));

        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc
                = new ResourceConfig().packages("com.example")
                                      .property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        String url = BASE_URI + "myresource";
        System.out.println(url + "?kanaName=123456");
        System.out.println(url + "?kanaName=%E3%82%A2%E3%82%A4%E3%82%A6%E3%82%A8%E3%82%AA&score=1000");
        System.out.println(url + "?kanaName=%E3%82%A2%E3%82%A4%E3%82%A6%E3%82%A8%E3%82%AA&list=one&list=two&list=three");
        System.in.read();
        server.shutdownNow();
    }
}

