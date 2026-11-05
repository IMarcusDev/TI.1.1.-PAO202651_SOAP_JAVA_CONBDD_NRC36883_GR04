package ec.edu.monster.modelos.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.config.WsConfig;
import ec.edu.monster.fakes.FakeLoginWebService;
import ec.edu.monster.fakes.FakeMovimientoWebService;
import ec.edu.monster.fakes.FakeSoapServer;
import ec.edu.monster.modelos.dto.HistorialCuenta;
import ec.edu.monster.ws.movimiento.EstadosEnum;
import ec.edu.monster.ws.movimiento.MovimientoResponseDTO;
import jakarta.xml.ws.Endpoint;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MovimientoServiceTest {
  private static final String TOKEN = FakeLoginWebService.TOKEN;
  private final MovimientoService service = new MovimientoService();

  /** A fake server is running at the configured address. */
  @Nested
  class WithServer {
    private final FakeMovimientoWebService fake = new FakeMovimientoWebService();
    private Endpoint server;

    @BeforeEach
    void start() {
      server = FakeSoapServer.publish("MovimientoWebService", fake);
    }

    @AfterEach
    void stop() {
      server.stop();
    }

    @Test
    void depositoSendsItsParametersAndReturnsServerAnswer() {
      MovimientoResponseDTO r = service.deposito(TOKEN, "001", "001", 10.5);

      assertEquals("deposito|" + TOKEN + "|001|001|10.5", fake.lastCall);
      assertEquals(EstadosEnum.OK, r.getEstado());
      assertEquals("deposito registrado", r.getMensaje());
      assertEquals(1, r.getIdMovimiento());
    }

    @Test
    void retiroSendsItsParametersAndReturnsServerAnswer() {
      MovimientoResponseDTO r = service.retiro(TOKEN, "001", "001", 20.0);

      assertEquals("retiro|" + TOKEN + "|001|001|20.0", fake.lastCall);
      assertEquals(EstadosEnum.OK, r.getEstado());
      assertEquals("retiro registrado", r.getMensaje());
      assertEquals(2, r.getIdMovimiento());
    }

    @Test
    void transferenciaSendsItsParametersAndReturnsServerAnswer() {
      MovimientoResponseDTO r = service.transferencia(TOKEN, "001", "002", 30.0);

      assertEquals("transferencia|" + TOKEN + "|001|002|30.0", fake.lastCall);
      assertEquals(EstadosEnum.OK, r.getEstado());
      assertEquals("transferencia registrado", r.getMensaje());
      assertEquals(3, r.getIdMovimiento());
    }

    @Test
    void invalidTokenReturnsServerError() {
      MovimientoResponseDTO r = service.deposito("token-falso", "001", "001", 10.0);

      assertEquals(EstadosEnum.ERROR, r.getEstado());
      assertEquals("Token inválido", r.getMensaje());
      assertEquals(0, r.getIdMovimiento());
    }

    @Test
    void registrarDepositoSendsItsParametersAndReturnsNumberAndBalance() {
      MovimientoResponseDTO r = service.registrarDeposito(TOKEN, "00100001", new BigDecimal("10.50"));

      assertEquals("registrarDeposito|" + TOKEN + "|00100001|10.50", fake.lastCall);
      assertEquals(EstadosEnum.OK, r.getEstado());
      assertEquals("Depósito registrado", r.getMensaje());
      assertEquals(11, r.getNumeroMovimiento());
      assertEquals(FakeMovimientoWebService.NUEVO_SALDO, r.getNuevoSaldo());
    }

    @Test
    void registrarRetiroSendsTheKeyAndReturnsNumberAndBalance() {
      MovimientoResponseDTO r = service.registrarRetiro(TOKEN, "00100001", new BigDecimal("20"), "123456");

      assertEquals("registrarRetiro|" + TOKEN + "|00100001|20|123456", fake.lastCall);
      assertEquals(EstadosEnum.OK, r.getEstado());
      assertEquals("Retiro registrado", r.getMensaje());
      assertEquals(12, r.getNumeroMovimiento());
      assertEquals(FakeMovimientoWebService.NUEVO_SALDO, r.getNuevoSaldo());
    }

    @Test
    void registrarTransferenciaSendsTheKeyAndReturnsNumberAndBalance() {
      MovimientoResponseDTO r = service.registrarTransferencia(
          TOKEN, "00100001", "00100002", new BigDecimal("30.25"), "123456");

      assertEquals("registrarTransferencia|" + TOKEN + "|00100001|00100002|30.25|123456", fake.lastCall);
      assertEquals(EstadosEnum.OK, r.getEstado());
      assertEquals("Transferencia registrada", r.getMensaje());
      assertEquals(13, r.getNumeroMovimiento());
      assertEquals(FakeMovimientoWebService.NUEVO_SALDO, r.getNuevoSaldo());
    }

    @Test
    void registrarRetiroOfAClienteSendsAnEmptyKey() {
      MovimientoResponseDTO r = service.registrarRetiro(
          FakeLoginWebService.TOKEN_CLIENTE, "00100001", new BigDecimal("5"), "");

      assertEquals("registrarRetiro|" + FakeLoginWebService.TOKEN_CLIENTE + "|00100001|5|", fake.lastCall);
      assertEquals(EstadosEnum.OK, r.getEstado());
    }

    @Test
    void registrarWithInvalidTokenReturnsServerError() {
      MovimientoResponseDTO r = service.registrarRetiro("token-falso", "00100001", BigDecimal.ONE, "123456");

      assertEquals(EstadosEnum.ERROR, r.getEstado());
      assertEquals(FakeMovimientoWebService.TOKEN_INVALIDO, r.getMensaje());
      assertEquals(0, r.getNumeroMovimiento());
      assertNull(r.getNuevoSaldo());
    }

    @Test
    void consultarMovimientosReturnsAccountAndFormattedRows() {
      HistorialCuenta h = service.consultarMovimientos(TOKEN, "00100001");

      assertEquals("consultarMovimientos|" + TOKEN + "|00100001", fake.lastCall);
      assertEquals(EstadosEnum.OK, h.estado());
      assertEquals("Consulta de movimientos exitosa", h.mensaje());

      HistorialCuenta.CuentaConsultada c = h.cuenta();
      assertEquals("00100001", c.getNumero());
      assertEquals("Ana Torres", c.getTitular());
      assertEquals("USD", c.getMoneda());
      assertEquals(new BigDecimal("1250.00"), c.getSaldo());
      assertEquals("$ 1,250.00", c.getSaldoFormateado());
      assertEquals("ACTIVO", c.getEstado());

      assertFalse(h.isSinMovimientos());
      List<HistorialCuenta.MovimientoConsultado> filas = h.getMovimientos();
      assertEquals(2, filas.size());

      // Newest first: the server sends 3 then 7.
      HistorialCuenta.MovimientoConsultado deposito = filas.get(1);
      assertEquals("000003", deposito.getNumero());
      assertEquals("20/10/2026 10:00", deposito.getFecha());
      assertEquals("Depósito", deposito.getTipo());
      assertEquals("INGRESO", deposito.getAccion());
      assertTrue(deposito.isIngreso());
      assertEquals(new BigDecimal("600.00"), deposito.getImporte());
      assertEquals("$ 600.00", deposito.getImporteFormateado());
      assertEquals("-", deposito.getReferencia());

      HistorialCuenta.MovimientoConsultado transferencia = filas.get(0);
      assertEquals("000007", transferencia.getNumero());
      assertEquals("28/10/2026 11:20", transferencia.getFecha());
      assertEquals("Transferencia", transferencia.getTipo());
      assertEquals("SALIDA", transferencia.getAccion());
      assertFalse(transferencia.isIngreso());
      assertEquals("$ 200.00", transferencia.getImporteFormateado());
      assertEquals("00100002", transferencia.getReferencia());
    }

    @Test
    void consultarMovimientosOfAnAccountWithoutMovementsReturnsAnEmptyList() {
      HistorialCuenta h = service.consultarMovimientos(TOKEN, "00100004");

      assertEquals(EstadosEnum.OK, h.estado());
      assertEquals("00100004", h.cuenta().getNumero());
      assertEquals("$ 0.00", h.cuenta().getSaldoFormateado());
      assertTrue(h.isSinMovimientos());
      assertEquals(List.of(), h.getMovimientos());
    }

    @Test
    void consultarMovimientosOfAnUnknownAccountReturnsServerError() {
      HistorialCuenta h = service.consultarMovimientos(TOKEN, "99999999");

      assertEquals(EstadosEnum.ERROR, h.estado());
      assertEquals("La cuenta no existe", h.mensaje());
      assertNull(h.cuenta());
      assertTrue(h.isSinMovimientos());
    }

    @Test
    void consultarMovimientosWithInvalidTokenReturnsServerError() {
      HistorialCuenta h = service.consultarMovimientos("token-falso", "00100001");

      assertEquals(EstadosEnum.ERROR, h.estado());
      assertEquals(FakeMovimientoWebService.TOKEN_INVALIDO, h.mensaje());
      assertNull(h.cuenta());
      assertTrue(h.isSinMovimientos());
    }
  }

  /** Nothing is listening: all three operations share the same error handling. */
  @Test
  void serverDownReturnsConnectionErrorForEveryOperation() {
    String expected = "No se pudo conectar con el servidor: "
        + WsConfig.getEndpoint("MovimientoWebService");

    for (MovimientoResponseDTO r : new MovimientoResponseDTO[] {
        service.deposito(TOKEN, "001", "001", 1),
        service.retiro(TOKEN, "001", "001", 1),
        service.transferencia(TOKEN, "001", "002", 1)}) {
      assertEquals(EstadosEnum.ERROR, r.getEstado());
      assertEquals(expected, r.getMensaje());
    }
  }

  /** Nothing is listening: the ERS operations and the query share the same error handling. */
  @Test
  void serverDownReturnsConnectionErrorForEveryErsOperation() {
    String expected = "No se pudo conectar con el servidor: "
        + WsConfig.getEndpoint("MovimientoWebService");

    for (MovimientoResponseDTO r : new MovimientoResponseDTO[] {
        service.registrarDeposito(TOKEN, "001", BigDecimal.ONE),
        service.registrarRetiro(TOKEN, "001", BigDecimal.ONE, "k"),
        service.registrarTransferencia(TOKEN, "001", "002", BigDecimal.ONE, "k")}) {
      assertEquals(EstadosEnum.ERROR, r.getEstado());
      assertEquals(expected, r.getMensaje());
    }

    HistorialCuenta h = service.consultarMovimientos(TOKEN, "001");
    assertEquals(EstadosEnum.ERROR, h.estado());
    assertEquals(expected, h.mensaje());
    assertNull(h.cuenta());
    assertTrue(h.isSinMovimientos());
  }
}
