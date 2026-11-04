package ec.edu.monster.fakes;

import ec.edu.monster.ws.movimiento.ConsultaMovimientosResponseDTO;
import ec.edu.monster.ws.movimiento.CuentaDTO;
import ec.edu.monster.ws.movimiento.EstadosEnum;
import ec.edu.monster.ws.movimiento.MovimientoDTO;
import ec.edu.monster.ws.movimiento.MovimientoResponseDTO;
import ec.edu.monster.ws.movimiento.MovimientoWebService;
import jakarta.jws.WebService;
import java.math.BigDecimal;

/**
 * Fake MovimientoWebService. It records the last call it received so tests
 * can check that the client sent the right operation and parameters.
 * Accounts known by consultarMovimientos: 00100001 (two movements) and
 * 00100004 (no movements); any other account does not exist.
 */
@WebService(
    endpointInterface = "ec.edu.monster.ws.movimiento.MovimientoWebService",
    serviceName = "MovimientoWebService",
    portName = "MovimientoWebServicePort",
    targetNamespace = "http://ws.monster.edu.ec/")
public class FakeMovimientoWebService implements MovimientoWebService {
  // Message of the real server for the ERS operations (legacy ones say "Token inválido")
  public static final String TOKEN_INVALIDO = "Token inválido o sesión expirada";
  public static final BigDecimal NUEVO_SALDO = new BigDecimal("1250.50");

  /**
   * Last call, fields joined with "|":
   *   legacy    -> "operation|token|origen|destino|valor", e.g. "deposito|t|1|2|10.5"
   *   registrar -> "operation|token|cuenta(s)...|importe|clave", e.g. "registrarRetiro|t|1|20.00|k"
   */
  public volatile String lastCall;

  @Override
  public MovimientoResponseDTO deposito(String token, String cuentaOrigen, String cuentaDestino, double valor) {
    return answer("deposito", token, cuentaOrigen, cuentaDestino, valor, 1);
  }

  @Override
  public MovimientoResponseDTO retiro(String token, String cuentaOrigen, String cuentaDestino, double valor) {
    return answer("retiro", token, cuentaOrigen, cuentaDestino, valor, 2);
  }

  @Override
  public MovimientoResponseDTO transferencia(String token, String cuentaOrigen, String cuentaDestino, double valor) {
    return answer("transferencia", token, cuentaOrigen, cuentaDestino, valor, 3);
  }

  @Override
  public MovimientoResponseDTO registrarDeposito(String token, String cuenta, BigDecimal importe) {
    lastCall = "registrarDeposito|" + token + "|" + cuenta + "|" + plain(importe);
    return registrado(token, "Depósito registrado", 11);
  }

  @Override
  public MovimientoResponseDTO registrarRetiro(String token, String cuenta, BigDecimal importe, String clave) {
    lastCall = "registrarRetiro|" + token + "|" + cuenta + "|" + plain(importe) + "|" + clave;
    return registrado(token, "Retiro registrado", 12);
  }

  @Override
  public MovimientoResponseDTO registrarTransferencia(String token, String cuentaOrigen,
      String cuentaDestino, BigDecimal importe, String clave) {
    lastCall = "registrarTransferencia|" + token + "|" + cuentaOrigen + "|" + cuentaDestino
        + "|" + plain(importe) + "|" + clave;
    return registrado(token, "Transferencia registrada", 13);
  }

  @Override
  public ConsultaMovimientosResponseDTO consultarMovimientos(String token, String cuenta) {
    lastCall = "consultarMovimientos|" + token + "|" + cuenta;
    ConsultaMovimientosResponseDTO r = new ConsultaMovimientosResponseDTO();
    if (!valido(token)) {
      r.setEstado(EstadosEnum.ERROR);
      r.setMensaje(TOKEN_INVALIDO);
      return r;
    }
    if ("00100001".equals(cuenta)) {
      r.setCuenta(cuenta("00100001", "Ana Torres", "1250.00"));
      r.getMovimientos().add(movimiento(3, "2026-10-20T10:00:00", "Depósito", "INGRESO", "600.00", null));
      r.getMovimientos().add(movimiento(7, "2026-10-28T11:20:00", "Transferencia", "SALIDA", "200.00", "00100002"));
    } else if ("00100004".equals(cuenta)) {
      r.setCuenta(cuenta("00100004", "Luis Mena", "0.00"));
    } else {
      r.setEstado(EstadosEnum.ERROR);
      r.setMensaje("La cuenta no existe");
      return r;
    }
    r.setEstado(EstadosEnum.OK);
    r.setMensaje("Consulta de movimientos exitosa");
    return r;
  }

  // Valid token -> OK with an id per operation; anything else -> "Token inválido" like the real server
  private MovimientoResponseDTO answer(String operation, String token, String origen,
      String destino, double valor, int id) {
    lastCall = operation + "|" + token + "|" + origen + "|" + destino + "|" + valor;
    MovimientoResponseDTO r = new MovimientoResponseDTO();
    if (FakeLoginWebService.TOKEN.equals(token)) {
      r.setEstado(EstadosEnum.OK);
      r.setMensaje(operation + " registrado");
      r.setIdMovimiento(id);
    } else {
      r.setEstado(EstadosEnum.ERROR);
      r.setMensaje("Token inválido");
      r.setIdMovimiento(0);
    }
    return r;
  }

  private static MovimientoResponseDTO registrado(String token, String mensaje, int numero) {
    MovimientoResponseDTO r = new MovimientoResponseDTO();
    if (valido(token)) {
      r.setEstado(EstadosEnum.OK);
      r.setMensaje(mensaje);
      r.setNumeroMovimiento(numero);
      r.setNuevoSaldo(NUEVO_SALDO);
    } else {
      r.setEstado(EstadosEnum.ERROR);
      r.setMensaje(TOKEN_INVALIDO);
    }
    return r;
  }

  private static boolean valido(String token) {
    return FakeLoginWebService.TOKEN.equals(token) || FakeLoginWebService.TOKEN_CLIENTE.equals(token);
  }

  private static String plain(BigDecimal importe) {
    return importe == null ? null : importe.toPlainString();
  }

  private static CuentaDTO cuenta(String numero, String titular, String saldo) {
    CuentaDTO c = new CuentaDTO();
    c.setNumeroCuenta(numero);
    c.setTitular(titular);
    c.setMoneda("USD");
    c.setSaldo(new BigDecimal(saldo));
    c.setEstadoCuenta("ACTIVO");
    return c;
  }

  private static MovimientoDTO movimiento(int numero, String fecha, String tipo, String accion,
      String importe, String referencia) {
    MovimientoDTO m = new MovimientoDTO();
    m.setNumeroMovimiento(numero);
    m.setFecha(fecha);
    m.setTipoMovimiento(tipo);
    m.setAccion(accion);
    m.setImporte(new BigDecimal(importe));
    m.setCuentaReferencia(referencia);
    return m;
  }
}
