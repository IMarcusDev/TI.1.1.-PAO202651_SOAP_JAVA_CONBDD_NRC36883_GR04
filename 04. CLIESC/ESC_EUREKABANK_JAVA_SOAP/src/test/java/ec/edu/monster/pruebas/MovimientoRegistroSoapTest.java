package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.dobles.ConfiguracionDePrueba;
import ec.edu.monster.dobles.FakeLoginWebService;
import ec.edu.monster.dobles.FakeMovimientoWebService;
import ec.edu.monster.dobles.FakeSoapServer;
import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.HistorialCuenta;
import ec.edu.monster.modelo.MovimientoServicioSoap;
import ec.edu.monster.modelo.ResultadoMovimiento;
import ec.edu.monster.modelo.ServicioException;
import jakarta.xml.ws.Endpoint;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ERS operations (registrarDeposito / registrarRetiro / registrarTransferencia /
 * consultarMovimientos) of MovimientoServicioSoap against a fake SOAP server.
 */
class MovimientoRegistroSoapTest {

    private static final String TOKEN = FakeMovimientoWebService.TOKEN;

    private FakeMovimientoWebService fake;
    private Endpoint servidor;
    private MovimientoServicioSoap servicio;

    @BeforeEach
    void iniciar() {
        Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
        fake = new FakeMovimientoWebService();
        servidor = FakeSoapServer.publicar(configuracion, "MovimientoWebService", fake);
        servicio = new MovimientoServicioSoap(configuracion);
    }

    @AfterEach
    void detener() {
        if (servidor != null) {
            servidor.stop();
        }
    }

    @Test
    void depositSendsAccountAndAmountWithoutKey() throws ServicioException {
        ResultadoMovimiento resultado = servicio.registrarDeposito(TOKEN, "00100001", new BigDecimal("100.00"));

        assertEquals("registrarDeposito|" + TOKEN + "|00100001|100.00", fake.ultimaLlamada);
        assertEquals(11, resultado.idMovimiento());
        assertEquals("Depósito registrado", resultado.mensaje());
        assertEquals(FakeMovimientoWebService.NUEVO_SALDO, resultado.nuevoSaldo());
    }

    @Test
    void withdrawalSendsTheKey() throws ServicioException {
        ResultadoMovimiento resultado = servicio.registrarRetiro(TOKEN, "00100001", new BigDecimal("20.00"), "1234");

        assertEquals("registrarRetiro|" + TOKEN + "|00100001|20.00|1234", fake.ultimaLlamada);
        assertEquals(12, resultado.idMovimiento());
        assertEquals("Retiro registrado", resultado.mensaje());
        assertEquals(FakeMovimientoWebService.NUEVO_SALDO, resultado.nuevoSaldo());
    }

    @Test
    void transferSendsBothAccountsAndTheKey() throws ServicioException {
        ResultadoMovimiento resultado = servicio.registrarTransferencia(TOKEN, "00100001", "00100002",
                new BigDecimal("50.50"), "1234");

        assertEquals("registrarTransferencia|" + TOKEN + "|00100001|00100002|50.50|1234", fake.ultimaLlamada);
        assertEquals(13, resultado.idMovimiento());
        assertEquals("Transferencia registrada", resultado.mensaje());
    }

    @Test
    void wrongKeyThrowsTheServerMessage() {
        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.registrarRetiro(TOKEN, "00100001", new BigDecimal("20.00"), "clave-mala"));

        assertEquals("Clave de la cuenta incorrecta", error.getMessage());
    }

    @Test
    void historyIsFormattedForTheView() throws ServicioException {
        HistorialCuenta historial = servicio.consultarMovimientos(FakeLoginWebService.TOKEN_CLIENTE, "00100001");

        assertEquals("consultarMovimientos|" + FakeLoginWebService.TOKEN_CLIENTE + "|00100001", fake.ultimaLlamada);
        assertEquals(new HistorialCuenta.CuentaConsultada("00100001", "Ana Torres", "USD", "$ 1,250.00", "ACTIVO"),
                historial.cuenta());
        assertEquals(List.of(
                new HistorialCuenta.MovimientoConsultado("000003", "20/10/2026 10:00", "Depósito", "INGRESO",
                        "$ 600.00", "-"),
                new HistorialCuenta.MovimientoConsultado("000007", "28/10/2026 11:20", "Transferencia", "SALIDA",
                        "$ 200.00", "00100002")),
                historial.movimientos());
        assertTrue(historial.movimientos().get(0).esIngreso());
        assertFalse(historial.movimientos().get(1).esIngreso());
        assertFalse(historial.sinMovimientos());
    }

    @Test
    void accountWithoutMovementsReturnsAnEmptyList() throws ServicioException {
        HistorialCuenta historial = servicio.consultarMovimientos(TOKEN, "00100004");

        assertEquals("$ 0.00", historial.cuenta().saldo());
        assertTrue(historial.sinMovimientos());
    }

    @Test
    void unknownAccountThrowsTheServerMessage() {
        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.consultarMovimientos(TOKEN, "99999999"));

        assertEquals("La cuenta no existe", error.getMessage());
    }

    @Test
    void invalidTokenThrowsTheServerMessage() {
        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.consultarMovimientos("otro", "00100001"));

        assertEquals(FakeMovimientoWebService.TOKEN_INVALIDO, error.getMessage());
    }

    @Test
    void serverDownThrowsTheConnectionMessage() {
        servidor.stop();
        servidor = null;

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.consultarMovimientos(TOKEN, "00100001"));

        assertTrue(error.getMessage().startsWith("No se pudo conectar con el servidor: "));
    }
}
