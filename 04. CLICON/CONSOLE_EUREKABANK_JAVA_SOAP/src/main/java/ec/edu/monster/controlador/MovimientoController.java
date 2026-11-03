package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Endpoints;
import ec.edu.monster.modelos.dto.ConsultaResponseDTO;
import ec.edu.monster.modelos.dto.MovimientoResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.modelos.http.SOAPClient;
import java.math.BigDecimal;

//revisado y aprovado por Marcos Escobar
public class MovimientoController {
  /** ECUD2: datos de la cuenta y lista de sus movimientos. */
  public ConsultaResponseDTO consultar(String cuenta) {
    try {
      String xml = new SOAPClient().post(Endpoints.MOVIMIENTO, Endpoints.CONSULTAR,
        "token", ConfigHTTP.getToken(),
        "cuenta", cuenta);
      return ConsultaResponseDTO.fromXML(xml);
    } catch (Exception e) {
      return new ConsultaResponseDTO(EstadosEnum.ERROR, ErroresCliente.mensaje(e));
    }
  }

  /** ECUD3: el depósito lo registra el empleado de la sesión; no lleva clave. */
  public MovimientoResponseDTO deposito(String cuenta, BigDecimal importe) {
    return registrar(Endpoints.DEPOSITO,
      "token", ConfigHTTP.getToken(),
      "cuenta", cuenta,
      "importe", SOAPClient.importe(importe));
  }

  /** ECUD3: el empleado envía la clave de la cuenta; el cliente opera su cuenta y la clave va vacía. */
  public MovimientoResponseDTO retiro(String cuenta, BigDecimal importe, String clave) {
    return registrar(Endpoints.RETIRO,
      "token", ConfigHTTP.getToken(),
      "cuenta", cuenta,
      "importe", SOAPClient.importe(importe),
      "clave", clave);
  }

  public MovimientoResponseDTO transferencia(String cuentaOrigen, String cuentaDestino, BigDecimal importe,
      String clave) {
    return registrar(Endpoints.TRANSFERENCIA,
      "token", ConfigHTTP.getToken(),
      "cuentaOrigen", cuentaOrigen,
      "cuentaDestino", cuentaDestino,
      "importe", SOAPClient.importe(importe),
      "clave", clave);
  }

  private MovimientoResponseDTO registrar(String operacion, String... parametros) {
    try {
      return MovimientoResponseDTO.fromXML(new SOAPClient().post(Endpoints.MOVIMIENTO, operacion, parametros));
    } catch (Exception e) {
      return new MovimientoResponseDTO(EstadosEnum.ERROR, ErroresCliente.mensaje(e), 0);
    }
  }
}
