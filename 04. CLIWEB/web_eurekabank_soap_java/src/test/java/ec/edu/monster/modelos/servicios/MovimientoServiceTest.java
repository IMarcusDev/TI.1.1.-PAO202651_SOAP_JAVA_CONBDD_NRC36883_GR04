package ec.edu.monster.modelos.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.config.WsConfig;
import ec.edu.monster.fakes.FakeLoginWebService;
import ec.edu.monster.fakes.FakeMovimientoWebService;
import ec.edu.monster.fakes.FakeSoapServer;
import ec.edu.monster.ws.movimiento.EstadosEnum;
import ec.edu.monster.ws.movimiento.MovimientoResponseDTO;
import jakarta.xml.ws.Endpoint;
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
}
