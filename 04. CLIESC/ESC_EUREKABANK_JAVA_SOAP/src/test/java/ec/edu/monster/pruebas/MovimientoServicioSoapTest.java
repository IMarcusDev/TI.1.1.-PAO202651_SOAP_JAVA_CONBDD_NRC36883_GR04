package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ec.edu.monster.dobles.ConfiguracionDePrueba;
import ec.edu.monster.dobles.FakeLoginWebService;
import ec.edu.monster.dobles.FakeMovimientoWebService;
import ec.edu.monster.dobles.FakeSoapServer;
import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.MovimientoServicioSoap;
import ec.edu.monster.modelo.ResultadoMovimiento;
import ec.edu.monster.modelo.ServicioException;
import jakarta.xml.ws.Endpoint;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Exercises the real SOAP round trip against a fake MovimientoWebService
 * published on a free local port.
 */
class MovimientoServicioSoapTest {

    @Nested
    class WithServer {

        private Endpoint servidor;
        private MovimientoServicioSoap servicio;

        @BeforeEach
        void iniciar() {
            Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
            servidor = FakeSoapServer.publicar(configuracion, "MovimientoWebService",
                    new FakeMovimientoWebService());
            servicio = new MovimientoServicioSoap(configuracion);
        }

        @AfterEach
        void detener() {
            if (servidor != null) {
                servidor.stop();
            }
        }

        @Test
        void depositReturnsTheGeneratedId() throws ServicioException {
            ResultadoMovimiento resultado = servicio.deposito(FakeMovimientoWebService.TOKEN,
                    "0001", "0004", new BigDecimal("100"));

            assertEquals(101, resultado.idMovimiento());
            assertEquals("Depósito registrado", resultado.mensaje());
        }

        @Test
        void withdrawalReturnsTheGeneratedId() throws ServicioException {
            ResultadoMovimiento resultado = servicio.retiro(FakeMovimientoWebService.TOKEN,
                    "0004", "", new BigDecimal("50.5"));

            assertEquals(102, resultado.idMovimiento());
            assertEquals("Retiro registrado", resultado.mensaje());
        }

        @Test
        void transferReturnsTheGeneratedId() throws ServicioException {
            ResultadoMovimiento resultado = servicio.transferencia(FakeMovimientoWebService.TOKEN,
                    "0001", "0002", new BigDecimal("25"));

            assertEquals(103, resultado.idMovimiento());
            assertEquals("Transferencia registrada", resultado.mensaje());
        }

        @Test
        void invalidTokenThrowsTheServerMessage() {
            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.deposito("otro-token", "0001", "0004", new BigDecimal("100")));

            assertEquals("Token inválido", error.getMessage());
        }

        @Test
        void zeroAmountThrowsTheServerMessage() {
            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.deposito(FakeMovimientoWebService.TOKEN, "0001", "0004", BigDecimal.ZERO));

            assertEquals("El importe debe ser mayor que cero", error.getMessage());
        }
    }

    /** Nothing is listening at the configured address. */
    @Test
    void serverDownThrowsTheConnectionMessage() {
        Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
        MovimientoServicioSoap servicio = new MovimientoServicioSoap(configuracion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.deposito(FakeLoginWebService.TOKEN, "0001", "0004", new BigDecimal("100")));

        assertEquals("No se pudo conectar con el servidor: " + configuracion.getEndpoint("MovimientoWebService"),
                error.getMessage());
    }
}
