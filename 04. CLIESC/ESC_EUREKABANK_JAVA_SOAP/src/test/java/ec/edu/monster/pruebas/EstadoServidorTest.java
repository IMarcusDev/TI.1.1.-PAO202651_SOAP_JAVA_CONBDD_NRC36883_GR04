package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.dobles.ConfiguracionDePrueba;
import ec.edu.monster.modelo.EstadoServidor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class EstadoServidorTest {

    @Test
    void availableWhenTheWsdlAnswers200() throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/wsdl", intercambio -> responder(intercambio, 200));
        servidor.start();
        try {
            URI wsdl = URI.create("http://localhost:" + servidor.getAddress().getPort() + "/wsdl");

            assertTrue(new EstadoServidor(wsdl).disponible());
        } finally {
            servidor.stop(0);
        }
    }

    @Test
    void unavailableWhenTheWsdlDoesNotAnswer200() throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/wsdl", intercambio -> responder(intercambio, 500));
        servidor.start();
        try {
            URI wsdl = URI.create("http://localhost:" + servidor.getAddress().getPort() + "/wsdl");

            assertFalse(new EstadoServidor(wsdl).disponible());
        } finally {
            servidor.stop(0);
        }
    }

    @Test
    void unavailableWhenNothingIsListening() {
        URI wsdl = URI.create("http://localhost:" + ConfiguracionDePrueba.puertoLibre() + "/wsdl");

        assertFalse(new EstadoServidor(wsdl).disponible());
    }

    private static void responder(com.sun.net.httpserver.HttpExchange intercambio, int estado)
            throws IOException {
        byte[] cuerpo = "wsdl".getBytes(StandardCharsets.UTF_8);
        intercambio.sendResponseHeaders(estado, cuerpo.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(cuerpo);
        }
    }
}
