package ec.edu.monster.modelos.servicios;

import ec.edu.monster.config.WsConfig;
import ec.edu.monster.ws.login.EstadosEnum;
import ec.edu.monster.ws.login.LoginResponseDTO;
import ec.edu.monster.ws.login.LoginWebService;
import ec.edu.monster.ws.login.LoginWebService_Service;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import java.util.function.Function;

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
   * Employee (bank teller) login with username and key.
   * Wrong credentials come back from the server as estado = ERROR;
   * a connection problem is turned into the same shape, so callers
   * only need to check getEstado().
   */
  public LoginResponseDTO loginEmpleado(String usuario, String clave) {
    return call(port -> port.loginEmpleado(usuario, clave));
  }

  /** Cliente login with the account number and its key; the token is tied to that account. */
  public LoginResponseDTO loginCliente(String cuenta, String clave) {
    return call(port -> port.loginCliente(cuenta, clave));
  }

  // Shared logic: create the port, run the operation, turn a connection failure into ERROR
  private LoginResponseDTO call(Function<LoginWebService, LoginResponseDTO> operation) {
    try {
      return operation.apply(createPort());
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
