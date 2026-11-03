package ec.edu.monster.fakes;

import ec.edu.monster.ws.movimiento.EstadosEnum;
import ec.edu.monster.ws.movimiento.MovimientoResponseDTO;
import ec.edu.monster.ws.movimiento.MovimientoWebService;
import jakarta.jws.WebService;

/**
 * Fake MovimientoWebService. It records the last call it received so tests
 * can check that the client sent the right operation and parameters.
 */
@WebService(
    endpointInterface = "ec.edu.monster.ws.movimiento.MovimientoWebService",
    serviceName = "MovimientoWebService",
    portName = "MovimientoWebServicePort",
    targetNamespace = "http://ws.monster.edu.ec/")
public class FakeMovimientoWebService implements MovimientoWebService {
  /** Last call as "operation|token|origen|destino|valor", e.g. "deposito|t|1|2|10.5". */
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
}
