package ec.edu.monster.modelos.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.config.WsConfig;
import ec.edu.monster.fakes.FakeLoginWebService;
import ec.edu.monster.fakes.FakeSoapServer;
import ec.edu.monster.ws.login.EstadosEnum;
import ec.edu.monster.ws.login.LoginResponseDTO;
import jakarta.xml.ws.Endpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginServiceTest {
  private final LoginService service = new LoginService();

  /** A fake server is running at the configured address. */
  @Nested
  class WithServer {
    private Endpoint server;

    @BeforeEach
    void start() {
      server = FakeSoapServer.publish("LoginWebService", new FakeLoginWebService());
    }

    @AfterEach
    void stop() {
      server.stop();
    }

    @Test
    void loginEmpleadoReturnsTokenAndEmployeeData() {
      LoginResponseDTO r = service.loginEmpleado("cajero", "cajero123");

      assertEquals(EstadosEnum.OK, r.getEstado());
      assertEquals("Ingreso de sesión exitoso", r.getMensaje());
      assertEquals(FakeLoginWebService.TOKEN, r.getToken());
      assertEquals("Carla Andrade", r.getNombre());
      assertEquals("Empleado", r.getTipoUsuario());
      assertEquals("EMPLEADO", r.getPerfil());
    }

    @Test
    void loginEmpleadoWithWrongKeyReturnsServerError() {
      LoginResponseDTO r = service.loginEmpleado("cajero", "mala");

      assertEquals(EstadosEnum.ERROR, r.getEstado());
      assertEquals("Credenciales incorrectas", r.getMensaje());
      assertEquals("", r.getToken());
    }

    @Test
    void loginClienteReturnsTokenAndClientData() {
      LoginResponseDTO r = service.loginCliente("00100001", "123456");

      assertEquals(EstadosEnum.OK, r.getEstado());
      assertEquals("Ingreso de sesión exitoso", r.getMensaje());
      assertEquals(FakeLoginWebService.TOKEN_CLIENTE, r.getToken());
      assertEquals("Ana Torres", r.getNombre());
      assertEquals("Cliente", r.getTipoUsuario());
      assertEquals("CLIENTE", r.getPerfil());
    }

    @Test
    void loginClienteWithWrongKeyReturnsServerError() {
      LoginResponseDTO r = service.loginCliente("00100001", "000000");

      assertEquals(EstadosEnum.ERROR, r.getEstado());
      assertEquals("Credenciales incorrectas", r.getMensaje());
      assertEquals("", r.getToken());
    }
  }

  /** Nothing is listening: both role logins share the same error handling. */
  @Test
  void serverDownReturnsConnectionErrorForEveryRole() {
    String expected = "No se pudo conectar con el servidor: " + WsConfig.getEndpoint("LoginWebService");

    for (LoginResponseDTO r : new LoginResponseDTO[] {
        service.loginEmpleado("cajero", "cajero123"),
        service.loginCliente("00100001", "123456")}) {
      assertEquals(EstadosEnum.ERROR, r.getEstado());
      assertEquals(expected, r.getMensaje());
    }
  }
}
