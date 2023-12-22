package com.example;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class MyResourceTest {

    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {

        // start the server
        server = Main.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and Main.startServer())
        // --
        // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());


        target = c.target(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdownNow();
        SystemRepository.clear();
    }

    @Test
    public void testInvalidParam() {
        try {
            target.path("myresource")
                    .queryParam("kanaName", "123456") // 文字数オーバー、文字種違反
                    .request()
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            Response response = e.getResponse();
            assertThat(response.getStatus(), is(400));
        }
    }

    @Test
    public void testValidParam() {

        String responseMsg = target.path("myresource")
                .queryParam("kanaName", "アイウエオ")
                .request()
                .get(String.class);

        assertThat(responseMsg, startsWith("all parameters are valid."));
    }

    @Test
    public void testList() {
        target.path("myresource")
              .queryParam("kanaName", "アイウエオ")
              .queryParam("list", "one")
              .queryParam("list", "two")
              .request()
              .get(String.class);
    }

    @Test(expected = BadRequestException.class)
    public void testListInvalid() {
        target.path("myresource")
              .queryParam("kanaName", "アイウエオ")
              .queryParam("list", "one")
              .queryParam("list", "two")
              .queryParam("list", "three")
              .request()
              .get(String.class);
    }
}
