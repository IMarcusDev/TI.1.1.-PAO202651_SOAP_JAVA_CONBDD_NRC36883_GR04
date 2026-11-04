package ec.edu.monster.dobles;

import ec.edu.monster.modelo.Configuracion;
import jakarta.xml.ws.Endpoint;

/**
 * Publishes a fake SOAP service at the address the configuration points to, so
 * tests exercise the real Java -> XML -> HTTP -> XML -> Java round trip without
 * Payara, the real server or the database.
 */
public final class FakeSoapServer {

    private FakeSoapServer() {
    }

    /** Starts the fake; call stop() on the returned Endpoint after the test. */
    public static Endpoint publicar(Configuracion configuracion, String servicio, Object implementacion) {
        return Endpoint.publish(configuracion.getEndpoint(servicio), implementacion);
    }
}
