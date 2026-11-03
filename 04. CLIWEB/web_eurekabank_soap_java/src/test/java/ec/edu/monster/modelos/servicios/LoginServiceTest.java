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
    void validCredentialsReturnTokenAndUserData() {
      LoginResponseDTO r = service.login("admin", "1234");

      assertEquals(EstadosEnum.OK, r.getEstado());
      assertEquals("Ingreso de Sesión Exitoso!", r.getMensaje());
      assertEquals(FakeLoginWebService.TOKEN, r.getToken());
      assertEquals("Administrador", r.getNombre());
      assertEquals("ADMIN", r.getTipoUsuario());
    }

    @Test
    void wrongCredentialsReturnServerError() {
      LoginResponseDTO r = service.login("admin", "mala");

      assertEquals(EstadosEnum.ERROR, r.getEstado());
      assertEquals("Contraseña incorrecta", r.getMensaje());
      assertEquals("", r.getToken());
    }
  }

  /** Nothing is listening at the configured address. */
  @Test
  void serverDownReturnsConnectionError() {
    LoginResponseDTO r = service.login("admin", "1234");

    assertEquals(EstadosEnum.ERROR, r.getEstado());
    assertEquals("No se pudo conectar con el servidor: "
        + WsConfig.getEndpoint("LoginWebService"), r.getMensaje());
  }
}
