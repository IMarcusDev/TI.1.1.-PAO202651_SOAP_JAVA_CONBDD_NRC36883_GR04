package ec.edu.monster.modelos.servicios;

import ec.edu.monster.config.WsConfig;
import ec.edu.monster.ws.login.EstadosEnum;
import ec.edu.monster.ws.login.LoginResponseDTO;
import ec.edu.monster.ws.login.LoginWebService;
import ec.edu.monster.ws.login.LoginWebService_Service;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;

/**
 * Client-side access to the SOAP LoginWebService.
 * Controllers use this class and never touch the generated stubs directly.
 */
public class LoginService {
  // Address taken from config.properties, not from the WSDL soap:address
  private static final String ENDPOINT = WsConfig.getEndpoint("LoginWebService");

  // Parsing the local WSDL is expensive and the Service is thread-safe,
  // so it is created once and shared
  private static final LoginWebService_Service SERVICE = new LoginWebService_Service();

  /**
   * Calls login on the server.
   * Wrong credentials come back from the server as estado = ERROR;
   * a connection problem is turned into the same shape, so callers
   * only need to check getEstado().
   */
  public LoginResponseDTO login(String username, String password) {
    try {
      return createPort().login(username, password);
    } catch (WebServiceException e) {
      // Server down, wrong IP/port in config.properties, network error...
      LoginResponseDTO error = new LoginResponseDTO();
      error.setEstado(EstadosEnum.ERROR);
      error.setMensaje("No se pudo conectar con el servidor: " + ENDPOINT);
      return error;
    }
  }

  // A port (proxy) is not guaranteed to be thread-safe, so each call gets its own
  private LoginWebService createPort() {
    LoginWebService port = SERVICE.getLoginWebServicePort();
    // Overrides the address hardcoded in the WSDL with the configured one
    ((BindingProvider) port).getRequestContext()
        .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT);
    return port;
  }
}
