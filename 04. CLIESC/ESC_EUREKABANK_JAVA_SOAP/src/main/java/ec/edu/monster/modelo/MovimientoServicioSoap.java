package ec.edu.monster.modelo;

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
 * Real implementation of {@link MovimientoServicio}: calls the SOAP
 * MovimientoWebService and turns its answer (or a connection failure) into a
 * result or an exception.
 */
public class MovimientoServicioSoap implements MovimientoServicio {

    private static final String SERVICIO = "MovimientoWebService";
    private static final String SIN_CONEXION = "No se pudo conectar con el servidor: ";
    private static final String SIN_RESPUESTA = "El servidor no devolvió respuesta";

    private final String endpoint;
    // Parsing the WSDL is expensive and the Service is thread-safe: created once
    private final MovimientoWebService_Service servicio = new MovimientoWebService_Service();

    public MovimientoServicioSoap(Configuracion configuracion) {
        this.endpoint = configuracion.getEndpoint(SERVICIO);
    }

    @Override
    public ResultadoMovimiento registrarDeposito(String token, String cuenta, BigDecimal importe)
            throws ServicioException {
        return ejecutar(puerto -> puerto.registrarDeposito(token, cuenta, importe));
    }

    @Override
    public ResultadoMovimiento registrarRetiro(String token, String cuenta, BigDecimal importe, String clave)
            throws ServicioException {
        return ejecutar(puerto -> puerto.registrarRetiro(token, cuenta, importe, clave));
    }

    @Override
    public ResultadoMovimiento registrarTransferencia(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal importe, String clave) throws ServicioException {
        return ejecutar(puerto -> puerto.registrarTransferencia(token, cuentaOrigen, cuentaDestino, importe, clave));
    }

    @Override
    public HistorialCuenta consultarMovimientos(String token, String cuenta) throws ServicioException {
        ConsultaMovimientosResponseDTO respuesta;
        try {
            respuesta = crearPuerto().consultarMovimientos(token, cuenta);
        } catch (WebServiceException e) {
            throw new ServicioException(SIN_CONEXION + endpoint);
        }
        if (respuesta == null) {
            throw new ServicioException(SIN_RESPUESTA);
        }
        if (respuesta.getEstado() != EstadosEnum.OK) {
            throw new ServicioException(ServicioException.mensaje(respuesta.getMensaje()));
        }
        List<HistorialCuenta.MovimientoConsultado> filas = new ArrayList<>();
        for (MovimientoDTO m : respuesta.getMovimientos()) {
            if (m != null) {
                filas.add(new HistorialCuenta.MovimientoConsultado(
                        Formatos.numeroMovimiento(m.getNumeroMovimiento()),
                        Formatos.fecha(m.getFecha()),
                        m.getTipoMovimiento(),
                        m.getAccion(),
                        Formatos.dinero(m.getImporte()),
                        Formatos.referencia(m.getCuentaReferencia())));
            }
        }
        return new HistorialCuenta(cuenta(respuesta.getCuenta()), filas);
    }

    private static HistorialCuenta.CuentaConsultada cuenta(CuentaDTO c) {
        if (c == null) {
            return new HistorialCuenta.CuentaConsultada("", "", "", "", "");
        }
        return new HistorialCuenta.CuentaConsultada(c.getNumeroCuenta(), c.getTitular(), c.getMoneda(),
                Formatos.dinero(c.getSaldo()), c.getEstadoCuenta());
    }

    // Shared logic of the movement operations: connect, call, validate the answer
    private ResultadoMovimiento ejecutar(Function<MovimientoWebService, MovimientoResponseDTO> operacion)
            throws ServicioException {
        MovimientoResponseDTO respuesta;
        try {
            respuesta = operacion.apply(crearPuerto());
        } catch (WebServiceException e) {
            // Server down, wrong host/port in config.properties, network error...
            throw new ServicioException(SIN_CONEXION + endpoint);
        }
        if (respuesta == null) {
            throw new ServicioException(SIN_RESPUESTA);
        }
        if (respuesta.getEstado() != EstadosEnum.OK) {
            throw new ServicioException(ServicioException.mensaje(respuesta.getMensaje()));
        }
        // The ERS operations answer numeroMovimiento + nuevoSaldo
        return new ResultadoMovimiento(respuesta.getNumeroMovimiento(),
                ServicioException.mensaje(respuesta.getMensaje()), respuesta.getNuevoSaldo());
    }

    // A port (proxy) is not guaranteed to be thread-safe, so each call gets its own
    private MovimientoWebService crearPuerto() {
        MovimientoWebService puerto = servicio.getMovimientoWebServicePort();
        // Overrides the address hardcoded in the WSDL with the configured one
        ((BindingProvider) puerto).getRequestContext()
                .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
        return puerto;
    }
}
