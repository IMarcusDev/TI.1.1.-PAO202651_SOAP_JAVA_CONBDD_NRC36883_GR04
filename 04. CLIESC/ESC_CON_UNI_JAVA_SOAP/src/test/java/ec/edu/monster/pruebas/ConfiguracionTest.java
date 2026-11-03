package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ec.edu.monster.dobles.ConfiguracionDePrueba;
import ec.edu.monster.modelo.Configuracion;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ConfiguracionTest {

    @Test
    void buildsTheBaseUrlAndEndpoints() {
        Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(9090);

        assertEquals("http://localhost:9090/WS_CON_UNI_JAVA_SOAP-1.0-SNAPSHOT", configuracion.getUrlBase());
        assertEquals("http://localhost:9090/WS_CON_UNI_JAVA_SOAP-1.0-SNAPSHOT/LoginWebService",
                configuracion.getEndpoint("LoginWebService"));
        assertEquals("localhost", configuracion.getHost());
        assertEquals(9090, configuracion.getPuerto());
    }

    @Test
    void missingKeyFailsWithAClearMessage() {
        Properties propiedades = new Properties();
        propiedades.setProperty("ws.host", "localhost");
        propiedades.setProperty("ws.port", "8080");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new Configuracion(propiedades).getUrlBase());
        assertEquals("Falta 'ws.context' en config.properties", error.getMessage());
    }

    @Test
    void blankValueCountsAsMissing() {
        Properties propiedades = new Properties();
        propiedades.setProperty("ws.host", " ");
        propiedades.setProperty("ws.port", "8080");
        propiedades.setProperty("ws.context", "contexto");

        assertThrows(IllegalStateException.class, () -> new Configuracion(propiedades).getUrlBase());
    }

    @Test
    void nonNumericPortFailsWithAClearMessage() {
        Properties propiedades = new Properties();
        propiedades.setProperty("ws.host", "localhost");
        propiedades.setProperty("ws.port", "ocho mil");
        propiedades.setProperty("ws.context", "contexto");

        assertThrows(IllegalStateException.class, () -> new Configuracion(propiedades).getPuerto());
    }

    @Test
    void readsTheFileFromTheClasspath() {
        // src/test/resources/config.properties shadows the application one
        assertEquals("http://localhost:18080/WS_CON_UNI_JAVA_SOAP-1.0-SNAPSHOT",
                Configuracion.desdeClasspath().getUrlBase());
    }
}
