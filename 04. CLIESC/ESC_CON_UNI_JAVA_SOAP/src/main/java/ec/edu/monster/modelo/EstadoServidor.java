package ec.edu.monster.modelo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Asks the SOAP server whether it is up by requesting its WSDL. It asks on every
 * call, because the server may go down at any moment while the client is open.
 */
public class EstadoServidor {

    private static final Duration ESPERA = Duration.ofSeconds(2);

    private final URI wsdl;
    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(ESPERA).build();

    public EstadoServidor(Configuracion configuracion) {
        this(URI.create(configuracion.getEndpoint("LoginWebService") + "?wsdl"));
    }

    /** Points the availability check at the given WSDL address. */
    public EstadoServidor(URI wsdl) {
        this.wsdl = wsdl;
    }

    /** True when the server answers its WSDL right now. */
    public boolean disponible() {
        HttpRequest peticion = HttpRequest.newBuilder(wsdl).timeout(ESPERA).GET().build();
        try {
            HttpResponse<Void> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.discarding());
            return respuesta.statusCode() == 200;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
