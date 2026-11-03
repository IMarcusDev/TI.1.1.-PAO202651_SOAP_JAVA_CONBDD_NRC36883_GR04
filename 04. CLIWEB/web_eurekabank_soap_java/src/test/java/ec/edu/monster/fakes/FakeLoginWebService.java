package ec.edu.monster.fakes;

import ec.edu.monster.ws.login.EstadosEnum;
import ec.edu.monster.ws.login.LoginResponseDTO;
import ec.edu.monster.ws.login.LoginWebService;
import jakarta.jws.WebService;

/**
 * Fake LoginWebService that follows the same contract (generated interface)
 * as the real server. Accepts only user "admin" with password "1234".
 */
@WebService(
    endpointInterface = "ec.edu.monster.ws.login.LoginWebService",
    serviceName = "LoginWebService",
    portName = "LoginWebServicePort",
    targetNamespace = "http://ws.monster.edu.ec/")
public class FakeLoginWebService implements LoginWebService {
  public static final String TOKEN = "token-de-prueba";

  @Override
  public LoginResponseDTO login(String username, String password) {
    LoginResponseDTO r = new LoginResponseDTO();
    if ("admin".equals(username) && "1234".equals(password)) {
      r.setEstado(EstadosEnum.OK);
      r.setMensaje("Ingreso de Sesión Exitoso!");
      r.setToken(TOKEN);
      r.setNombre("Administrador");
      r.setEstadoUsuario("ACTIVO");
      r.setTipoUsuario("ADMIN");
    } else {
      // Same shape the real server returns for wrong credentials
      r.setEstado(EstadosEnum.ERROR);
      r.setMensaje("Contraseña incorrecta");
      r.setToken("");
    }
    return r;
  }
}
