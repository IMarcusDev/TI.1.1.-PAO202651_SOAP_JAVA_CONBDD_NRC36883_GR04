package ec.edu.monster.modelos.servicios;

import ec.edu.monster.config.WsConfig;
import ec.edu.monster.ws.movimiento.EstadosEnum;
import ec.edu.monster.ws.movimiento.MovimientoResponseDTO;
import ec.edu.monster.ws.movimiento.MovimientoWebService;
import ec.edu.monster.ws.movimiento.MovimientoWebService_Service;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import java.util.function.Function;

/**
 * Client-side access to the SOAP MovimientoWebService.
 * Every operation needs the token returned by LoginService.login.
 */
public class MovimientoService {
  // Address taken from config.properties, not from the WSDL soap:address
  private static final String ENDPOINT = WsConfig.getEndpoint("MovimientoWebService");

  // Created once and shared: parsing the WSDL is expensive, the Service is thread-safe
  private static final MovimientoWebService_Service SERVICE = new MovimientoWebService_Service();

  public MovimientoResponseDTO deposito(String token, String cuentaOrigen, String cuentaDestino, double valor) {
    return call(port -> port.deposito(token, cuentaOrigen, cuentaDestino, valor));
  }

  public MovimientoResponseDTO retiro(String token, String cuentaOrigen, String cuentaDestino, double valor) {
    return call(port -> port.retiro(token, cuentaOrigen, cuentaDestino, valor));
  }

  public MovimientoResponseDTO transferencia(String token, String cuentaOrigen, String cuentaDestino, double valor) {
    return call(port -> port.transferencia(token, cuentaOrigen, cuentaDestino, valor));
  }

  /**
   * Shared logic of the three operations: create the port, run the given
   * operation and turn a connection failure into an estado = ERROR response,
   * so the try/catch is written once instead of three times.
   */
  private MovimientoResponseDTO call(Function<MovimientoWebService, MovimientoResponseDTO> operation) {
    try {
      return operation.apply(createPort());
    } catch (WebServiceException e) {
      // Server down, wrong IP/port in config.properties, network error...
      MovimientoResponseDTO error = new MovimientoResponseDTO();
      error.setEstado(EstadosEnum.ERROR);
      error.setMensaje("No se pudo conectar con el servidor: " + ENDPOINT);
      return error;
    }
  }

  // A port (proxy) is not guaranteed to be thread-safe, so each call gets its own
  private MovimientoWebService createPort() {
    MovimientoWebService port = SERVICE.getMovimientoWebServicePort();
    // Overrides the address hardcoded in the WSDL with the configured one
    ((BindingProvider) port).getRequestContext()
        .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT);
    return port;
  }
}
