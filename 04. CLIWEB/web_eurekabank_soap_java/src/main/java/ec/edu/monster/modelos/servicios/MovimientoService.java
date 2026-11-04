package ec.edu.monster.modelos.servicios;

import ec.edu.monster.config.WsConfig;
import ec.edu.monster.modelos.dto.Formatos;
import ec.edu.monster.modelos.dto.HistorialCuenta;
import ec.edu.monster.ws.movimiento.ConsultaMovimientosResponseDTO;
import ec.edu.monster.ws.movimiento.CuentaDTO;
import ec.edu.monster.ws.movimiento.EstadosEnum;
import ec.edu.monster.ws.movimiento.MovimientoDTO;
import ec.edu.monster.ws.movimiento.MovimientoResponseDTO;
import ec.edu.monster.ws.movimiento.MovimientoWebService;
import ec.edu.monster.ws.movimiento.MovimientoWebService_Service;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Client-side access to the SOAP MovimientoWebService.
 * Every operation needs the token returned by LoginService.
 */
public class MovimientoService {
  // Address taken from config.properties, not from the WSDL soap:address
  private static final String ENDPOINT = WsConfig.getEndpoint("MovimientoWebService");
  private static final String SIN_CONEXION = "No se pudo conectar con el servidor: " + ENDPOINT;

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

  /** Deposit into any account (employee only); returns numeroMovimiento and nuevoSaldo. */
  public MovimientoResponseDTO registrarDeposito(String token, String cuenta, BigDecimal importe) {
    return call(port -> port.registrarDeposito(token, cuenta, importe));
  }

  /** Withdrawal; clave is the account key for an employee and "" for a Cliente. */
  public MovimientoResponseDTO registrarRetiro(String token, String cuenta, BigDecimal importe, String clave) {
    return call(port -> port.registrarRetiro(token, cuenta, importe, clave));
  }

  /** Transfer; clave is the source account key for an employee and "" for a Cliente. */
  public MovimientoResponseDTO registrarTransferencia(String token, String cuentaOrigen,
      String cuentaDestino, BigDecimal importe, String clave) {
    return call(port -> port.registrarTransferencia(token, cuentaOrigen, cuentaDestino, importe, clave));
  }

  /** Account data and movement history, formatted for the view. */
  public HistorialCuenta consultarMovimientos(String token, String cuenta) {
    try {
      return historial(createPort().consultarMovimientos(token, cuenta));
    } catch (WebServiceException e) {
      return HistorialCuenta.error(EstadosEnum.ERROR, SIN_CONEXION);
    }
  }

  /**
   * Shared logic of the movement operations: create the port, run the given
   * operation and turn a connection failure into an estado = ERROR response,
   * so the try/catch is written once.
   */
  private MovimientoResponseDTO call(Function<MovimientoWebService, MovimientoResponseDTO> operation) {
    try {
      return operation.apply(createPort());
    } catch (WebServiceException e) {
      // Server down, wrong IP/port in config.properties, network error...
      MovimientoResponseDTO error = new MovimientoResponseDTO();
      error.setEstado(EstadosEnum.ERROR);
      error.setMensaje(SIN_CONEXION);
      return error;
    }
  }

  // Generated DTOs -> view model; a failed query carries no account and no rows
  private static HistorialCuenta historial(ConsultaMovimientosResponseDTO r) {
    if (r == null) {
      return HistorialCuenta.error(EstadosEnum.ERROR, "No se pudieron consultar los movimientos");
    }
    if (r.getEstado() != EstadosEnum.OK) {
      return HistorialCuenta.error(EstadosEnum.ERROR, r.getMensaje());
    }
    List<HistorialCuenta.MovimientoConsultado> filas = new ArrayList<>();
    for (MovimientoDTO m : r.getMovimientos()) {
      if (m != null) {
        filas.add(new HistorialCuenta.MovimientoConsultado(
            Formatos.numeroMovimiento(m.getNumeroMovimiento()),
            Formatos.fecha(m.getFecha()),
            m.getTipoMovimiento(),
            m.getAccion(),
            m.getImporte(),
            Formatos.referencia(m.getCuentaReferencia())));
      }
    }
    return new HistorialCuenta(EstadosEnum.OK, r.getMensaje(), cuenta(r.getCuenta()), filas);
  }

  private static HistorialCuenta.CuentaConsultada cuenta(CuentaDTO c) {
    if (c == null) {
      return new HistorialCuenta.CuentaConsultada("", "", "", null, "");
    }
    return new HistorialCuenta.CuentaConsultada(
        c.getNumeroCuenta(), c.getTitular(), c.getMoneda(), c.getSaldo(), c.getEstadoCuenta());
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
