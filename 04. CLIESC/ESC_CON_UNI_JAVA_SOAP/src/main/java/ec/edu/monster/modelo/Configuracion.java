package ec.edu.monster.modelo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads the SOAP server location from config.properties (classpath) and builds
 * the endpoint URLs used by the client services.
 */
public final class Configuracion {

    public static final String ARCHIVO = "config.properties";

    private final Properties propiedades;

    /** Builds a configuration from an in-memory set of properties (used by the tests). */
    public Configuracion(Properties propiedades) {
        this.propiedades = propiedades;
    }

    /** Reads config.properties from the classpath, so it works inside the packaged jar. */
    public static Configuracion desdeClasspath() {
        Properties props = new Properties();
        try (InputStream entrada = Configuracion.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (entrada == null) {
                throw new IllegalStateException(ARCHIVO + " no está en el classpath");
            }
            props.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO, e);
        }
        return new Configuracion(props);
    }

    /** e.g. http://localhost:8080/WS_CON_UNI_JAVA_SOAP-1.0-SNAPSHOT */
    public String getUrlBase() {
        return "http://" + valor("ws.host") + ":" + valor("ws.port") + "/" + valor("ws.context");
    }

    /** Full address of one service, e.g. getEndpoint("LoginWebService"). */
    public String getEndpoint(String servicio) {
        return getUrlBase() + "/" + servicio;
    }

    public String getHost() {
        return valor("ws.host");
    }

    public int getPuerto() {
        String texto = valor("ws.port");
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("'" + "ws.port" + "' no es un número en " + ARCHIVO + ": " + texto, e);
        }
    }

    // Returns a required key, failing with a clear message when it is missing or empty
    private String valor(String clave) {
        String valor = propiedades.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta '" + clave + "' en " + ARCHIVO);
        }
        return valor.trim();
    }
}
