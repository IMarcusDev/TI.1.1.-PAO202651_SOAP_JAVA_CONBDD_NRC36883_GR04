package ec.edu.monster.modelo;

import ec.edu.monster.ws.movimiento.EstadosEnum;
import ec.edu.monster.ws.movimiento.MovimientoResponseDTO;
import ec.edu.monster.ws.movimiento.MovimientoWebService;
import ec.edu.monster.ws.movimiento.MovimientoWebService_Service;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import java.math.BigDecimal;
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
    public ResultadoMovimiento deposito(String token, String cuentaOrigen, String cuentaDestino, BigDecimal valor)
            throws ServicioException {
        return ejecutar(puerto -> puerto.deposito(token, cuentaOrigen, cuentaDestino, valor.doubleValue()));
    }

    @Override
    public ResultadoMovimiento retiro(String token, String cuentaOrigen, String cuentaDestino, BigDecimal valor)
            throws ServicioException {
        return ejecutar(puerto -> puerto.retiro(token, cuentaOrigen, cuentaDestino, valor.doubleValue()));
    }

    @Override
    public ResultadoMovimiento transferencia(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal valor) throws ServicioException {
        return ejecutar(puerto -> puerto.transferencia(token, cuentaOrigen, cuentaDestino, valor.doubleValue()));
    }

    // Shared logic of the three operations: connect, call, validate the answer
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
        return new ResultadoMovimiento(respuesta.getIdMovimiento(),
                ServicioException.mensaje(respuesta.getMensaje()));
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
