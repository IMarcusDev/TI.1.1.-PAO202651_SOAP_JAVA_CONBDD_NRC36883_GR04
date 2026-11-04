package ec.edu.monster.modelo;

import ec.edu.monster.ws.login.EstadosEnum;
import ec.edu.monster.ws.login.LoginResponseDTO;
import ec.edu.monster.ws.login.LoginWebService;
import ec.edu.monster.ws.login.LoginWebService_Service;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;

/**
 * Real implementation of {@link LoginServicio}: calls the SOAP LoginWebService
 * and turns its answer (or a connection failure) into a session or an exception.
 */
public class LoginServicioSoap implements LoginServicio {

    private static final String SERVICIO = "LoginWebService";
    private static final String SIN_CONEXION = "No se pudo conectar con el servidor: ";
    private static final String SIN_RESPUESTA = "El servidor no devolvió respuesta";
    private static final String SIN_TOKEN = "El servidor no devolvió un token de sesión";

    private final String endpoint;
    // Parsing the WSDL is expensive and the Service is thread-safe: created once
    private final LoginWebService_Service servicio = new LoginWebService_Service();

    public LoginServicioSoap(Configuracion configuracion) {
        this.endpoint = configuracion.getEndpoint(SERVICIO);
    }

    @Override
    public DatosSesion iniciarSesion(String usuario, String clave) throws ServicioException {
        LoginResponseDTO respuesta;
        try {
            respuesta = crearPuerto().login(usuario, clave);
        } catch (WebServiceException e) {
            // Server down, wrong host/port in config.properties, network error...
            throw new ServicioException(SIN_CONEXION + endpoint);
        }
        if (respuesta == null) {
            throw new ServicioException(SIN_RESPUESTA);
        }
        if (respuesta.getEstado() != EstadosEnum.OK) {
            throw new ServicioException(ServicioException.mensaje(respuesta.getMensaje()));
        }
        if (respuesta.getToken() == null || respuesta.getToken().isBlank()) {
            throw new ServicioException(SIN_TOKEN);
        }
        return new DatosSesion(respuesta.getNombre(), respuesta.getTipoUsuario(), respuesta.getToken());
    }

    // A port (proxy) is not guaranteed to be thread-safe, so each call gets its own
    private LoginWebService crearPuerto() {
        LoginWebService puerto = servicio.getLoginWebServicePort();
        // Overrides the address hardcoded in the WSDL with the configured one
        ((BindingProvider) puerto).getRequestContext()
                .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
        return puerto;
    }
}
