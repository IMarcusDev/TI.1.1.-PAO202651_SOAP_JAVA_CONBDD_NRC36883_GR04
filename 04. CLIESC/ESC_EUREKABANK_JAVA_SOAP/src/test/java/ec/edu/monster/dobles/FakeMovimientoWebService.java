package ec.edu.monster.dobles;

import ec.edu.monster.ws.movimiento.ConsultaMovimientosResponseDTO;
import ec.edu.monster.ws.movimiento.CuentaDTO;
import ec.edu.monster.ws.movimiento.EstadosEnum;
import ec.edu.monster.ws.movimiento.MovimientoDTO;
import ec.edu.monster.ws.movimiento.MovimientoResponseDTO;
import ec.edu.monster.ws.movimiento.MovimientoWebService;
import jakarta.jws.WebService;
import java.math.BigDecimal;

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
    public static final String TOKEN_INVALIDO = "Token inválido o sesión expirada";
    public static final BigDecimal NUEVO_SALDO = new BigDecimal("1250.50");

    /**
     * Last registrar / consultar call, fields joined with "|", e.g.
     * "registrarRetiro|token|00100001|20.00|clave".
     */
    public volatile String ultimaLlamada;

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

    @Override
    public MovimientoResponseDTO registrarDeposito(String token, String cuenta, BigDecimal importe) {
        ultimaLlamada = "registrarDeposito|" + token + "|" + cuenta + "|" + plano(importe);
        return registrado(token, "Depósito registrado", 11);
    }

    @Override
    public MovimientoResponseDTO registrarRetiro(String token, String cuenta, BigDecimal importe, String clave) {
        ultimaLlamada = "registrarRetiro|" + token + "|" + cuenta + "|" + plano(importe) + "|" + clave;
        if ("clave-mala".equals(clave)) {
            MovimientoResponseDTO respuesta = new MovimientoResponseDTO();
            respuesta.setEstado(EstadosEnum.ERROR);
            respuesta.setMensaje("Clave de la cuenta incorrecta");
            return respuesta;
        }
        return registrado(token, "Retiro registrado", 12);
    }

    @Override
    public MovimientoResponseDTO registrarTransferencia(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal importe, String clave) {
        ultimaLlamada = "registrarTransferencia|" + token + "|" + cuentaOrigen + "|" + cuentaDestino
                + "|" + plano(importe) + "|" + clave;
        return registrado(token, "Transferencia registrada", 13);
    }

    @Override
    public ConsultaMovimientosResponseDTO consultarMovimientos(String token, String cuenta) {
        ultimaLlamada = "consultarMovimientos|" + token + "|" + cuenta;
        ConsultaMovimientosResponseDTO respuesta = new ConsultaMovimientosResponseDTO();
        if (!valido(token)) {
            respuesta.setEstado(EstadosEnum.ERROR);
            respuesta.setMensaje(TOKEN_INVALIDO);
            return respuesta;
        }
        if ("00100001".equals(cuenta)) {
            respuesta.setCuenta(cuenta("00100001", "Ana Torres", "1250.00"));
            respuesta.getMovimientos().add(movimiento(3, "2026-10-20T10:00:00", "Depósito", "INGRESO", "600.00", null));
            respuesta.getMovimientos().add(
                    movimiento(7, "2026-10-28T11:20:00", "Transferencia", "SALIDA", "200.00", "00100002"));
        } else if ("00100004".equals(cuenta)) {
            respuesta.setCuenta(cuenta("00100004", "Luis Mena", "0.00"));
        } else {
            respuesta.setEstado(EstadosEnum.ERROR);
            respuesta.setMensaje("La cuenta no existe");
            return respuesta;
        }
        respuesta.setEstado(EstadosEnum.OK);
        respuesta.setMensaje("Consulta de movimientos exitosa");
        return respuesta;
    }

    private static MovimientoResponseDTO registrado(String token, String mensaje, int numero) {
        MovimientoResponseDTO respuesta = new MovimientoResponseDTO();
        if (valido(token)) {
            respuesta.setEstado(EstadosEnum.OK);
            respuesta.setMensaje(mensaje);
            respuesta.setNumeroMovimiento(numero);
            respuesta.setNuevoSaldo(NUEVO_SALDO);
        } else {
            respuesta.setEstado(EstadosEnum.ERROR);
            respuesta.setMensaje(TOKEN_INVALIDO);
        }
        return respuesta;
    }

    private static boolean valido(String token) {
        return TOKEN.equals(token) || FakeLoginWebService.TOKEN_CLIENTE.equals(token);
    }

    private static String plano(BigDecimal importe) {
        return importe == null ? null : importe.toPlainString();
    }

    private static CuentaDTO cuenta(String numero, String titular, String saldo) {
        CuentaDTO cuenta = new CuentaDTO();
        cuenta.setNumeroCuenta(numero);
        cuenta.setTitular(titular);
        cuenta.setMoneda("USD");
        cuenta.setSaldo(new BigDecimal(saldo));
        cuenta.setEstadoCuenta("ACTIVO");
        return cuenta;
    }

    private static MovimientoDTO movimiento(int numero, String fecha, String tipo, String accion, String importe,
            String referencia) {
        MovimientoDTO movimiento = new MovimientoDTO();
        movimiento.setNumeroMovimiento(numero);
        movimiento.setFecha(fecha);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setAccion(accion);
        movimiento.setImporte(new BigDecimal(importe));
        movimiento.setCuentaReferencia(referencia);
        return movimiento;
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
