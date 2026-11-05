package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Client-side access to the SOAP MovimientoWebService. Every operation needs the
 * token returned by {@link LoginServicio#iniciarSesionEmpleado(String, String)}.
 */
public interface MovimientoServicio {

    /** ERS deposit: employee only, no account key. */
    ResultadoMovimiento registrarDeposito(String token, String cuenta, BigDecimal importe)
            throws ServicioException;

    /** ERS withdrawal: an employee sends the account key, a Cliente sends "" (own account). */
    ResultadoMovimiento registrarRetiro(String token, String cuenta, BigDecimal importe, String clave)
            throws ServicioException;

    /** ERS transfer: an employee sends the key of the origin account, a Cliente sends "". */
    ResultadoMovimiento registrarTransferencia(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal importe, String clave) throws ServicioException;

    /** Account data and movement history, already formatted for the view. */
    HistorialCuenta consultarMovimientos(String token, String cuenta) throws ServicioException;
}
