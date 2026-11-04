package ec.edu.monster.dobles;

import ec.edu.monster.modelo.HistorialCuenta;
import ec.edu.monster.modelo.MovimientoServicio;
import ec.edu.monster.modelo.ResultadoMovimiento;
import ec.edu.monster.modelo.ServicioException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Movement service double: records the call and returns a fixed result or the
 * configured error.
 */
public class MovimientoServicioFalso implements MovimientoServicio {

    private ServicioException error;
    private int llamadas;
    private String tokenRecibido;
    private String origenRecibido;
    private String destinoRecibido;
    private BigDecimal valorRecibido;
    private String operacionRecibida;
    private String claveRecibida;
    private HistorialCuenta historial = new HistorialCuenta(
            new HistorialCuenta.CuentaConsultada("00100001", "Ana Torres", "USD", "$ 1,250.00", "ACTIVO"),
            List.of());

    /** History returned by the next consultarMovimientos. */
    public void responderCon(HistorialCuenta historial) {
        this.historial = historial;
    }

    public void fallarCon(String mensaje) {
        this.error = new ServicioException(mensaje);
    }

    @Override
    public ResultadoMovimiento registrarDeposito(String token, String cuenta, BigDecimal importe)
            throws ServicioException {
        operacionRecibida = "registrarDeposito";
        claveRecibida = null;
        return registrar(token, cuenta, null, importe);
    }

    @Override
    public ResultadoMovimiento registrarRetiro(String token, String cuenta, BigDecimal importe, String clave)
            throws ServicioException {
        operacionRecibida = "registrarRetiro";
        claveRecibida = clave;
        return registrar(token, cuenta, null, importe);
    }

    @Override
    public ResultadoMovimiento registrarTransferencia(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal importe, String clave) throws ServicioException {
        operacionRecibida = "registrarTransferencia";
        claveRecibida = clave;
        return registrar(token, cuentaOrigen, cuentaDestino, importe);
    }

    @Override
    public HistorialCuenta consultarMovimientos(String token, String cuenta) throws ServicioException {
        operacionRecibida = "consultarMovimientos";
        llamadas++;
        tokenRecibido = token;
        origenRecibido = cuenta;
        if (error != null) {
            throw error;
        }
        return historial;
    }

    /** Name of the last operation called. */
    public String getOperacionRecibida() {
        return operacionRecibida;
    }

    /** Account key received by the last registrarRetiro / registrarTransferencia. */
    public String getClaveRecibida() {
        return claveRecibida;
    }

    private ResultadoMovimiento registrar(String token, String cuentaOrigen, String cuentaDestino, BigDecimal valor)
            throws ServicioException {
        llamadas++;
        tokenRecibido = token;
        origenRecibido = cuentaOrigen;
        destinoRecibido = cuentaDestino;
        valorRecibido = valor;
        if (error != null) {
            throw error;
        }
        return new ResultadoMovimiento(777, "Movimiento registrado");
    }

    public int getLlamadas() {
        return llamadas;
    }

    public String getTokenRecibido() {
        return tokenRecibido;
    }

    public String getOrigenRecibido() {
        return origenRecibido;
    }

    public String getDestinoRecibido() {
        return destinoRecibido;
    }

    public BigDecimal getValorRecibido() {
        return valorRecibido;
    }
}
