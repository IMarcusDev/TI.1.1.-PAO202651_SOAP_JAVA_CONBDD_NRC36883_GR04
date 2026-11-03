package ec.edu.monster.dobles;

import ec.edu.monster.ws.movimiento.EstadosEnum;
import ec.edu.monster.ws.movimiento.MovimientoResponseDTO;
import ec.edu.monster.ws.movimiento.MovimientoWebService;
import jakarta.jws.WebService;

/**
 * Fake MovimientoWebService that follows the same contract as the real server:
 * every operation requires the token returned by the login, and the amount must
 * be greater than zero.
 */
@WebService(
        endpointInterface = "ec.edu.monster.ws.movimiento.MovimientoWebService",
        serviceName = "MovimientoWebService",
        portName = "MovimientoWebServicePort",
        targetNamespace = "http://ws.monster.edu.ec/")
public class FakeMovimientoWebService implements MovimientoWebService {

    public static final String TOKEN = FakeLoginWebService.TOKEN;

    @Override
    public MovimientoResponseDTO deposito(String token, String cuentaOrigen, String cuentaDestino, double valor) {
        return responder(token, cuentaDestino, valor, 101, "Depósito registrado");
    }

    @Override
    public MovimientoResponseDTO retiro(String token, String cuentaOrigen, String cuentaDestino, double valor) {
        return responder(token, cuentaOrigen, valor, 102, "Retiro registrado");
    }

    @Override
    public MovimientoResponseDTO transferencia(String token, String cuentaOrigen, String cuentaDestino,
            double valor) {
        return responder(token, cuentaOrigen, valor, 103, "Transferencia registrada");
    }

    private MovimientoResponseDTO responder(String token, String cuenta, double valor, int id, String mensaje) {
        MovimientoResponseDTO respuesta = new MovimientoResponseDTO();
        if (!TOKEN.equals(token)) {
            respuesta.setEstado(EstadosEnum.ERROR);
            respuesta.setMensaje("Token inválido");
            return respuesta;
        }
        if (valor <= 0) {
            respuesta.setEstado(EstadosEnum.ERROR);
            respuesta.setMensaje("El importe debe ser mayor que cero");
            return respuesta;
        }
        if (cuenta == null || cuenta.isBlank()) {
            respuesta.setEstado(EstadosEnum.ERROR);
            respuesta.setMensaje("La cuenta es obligatoria");
            return respuesta;
        }
        respuesta.setEstado(EstadosEnum.OK);
        respuesta.setMensaje(mensaje);
        respuesta.setIdMovimiento(id);
        return respuesta;
    }
}
