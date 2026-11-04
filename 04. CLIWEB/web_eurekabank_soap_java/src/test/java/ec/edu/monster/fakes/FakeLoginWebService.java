package ec.edu.monster.fakes;

import ec.edu.monster.ws.login.EstadosEnum;
import ec.edu.monster.ws.login.LoginResponseDTO;
import ec.edu.monster.ws.login.LoginWebService;
import jakarta.jws.WebService;

/**
 * Fake LoginWebService that follows the same contract (generated interface)
 * as the real server. Accepted credentials:
 *   login         -> admin / 1234 (legacy operation)
 *   loginEmpleado -> cajero / cajero123
 *   loginCliente  -> account 00100001 / 123456
 */
@WebService(
    endpointInterface = "ec.edu.monster.ws.login.LoginWebService",
    serviceName = "LoginWebService",
    portName = "LoginWebServicePort",
    targetNamespace = "http://ws.monster.edu.ec/")
public class FakeLoginWebService implements LoginWebService {
  public static final String TOKEN = "token-de-prueba";
  public static final String TOKEN_CLIENTE = "token-cliente-de-prueba";

  @Override
  public LoginResponseDTO login(String username, String password) {
    if ("admin".equals(username) && "1234".equals(password)) {
      return ok(TOKEN, "Administrador", "ADMIN", null, "Ingreso de Sesión Exitoso!");
    }
    // Same shape the real server returns for wrong credentials
    return error("Contraseña incorrecta");
  }

  @Override
  public LoginResponseDTO loginEmpleado(String usuario, String clave) {
    if ("cajero".equals(usuario) && "cajero123".equals(clave)) {
      return ok(TOKEN, "Carla Andrade", "Empleado", "EMPLEADO", "Ingreso de sesión exitoso");
    }
    return error("Credenciales incorrectas");
  }

  @Override
  public LoginResponseDTO loginCliente(String cuenta, String clave) {
    if ("00100001".equals(cuenta) && "123456".equals(clave)) {
      return ok(TOKEN_CLIENTE, "Ana Torres", "Cliente", "CLIENTE", "Ingreso de sesión exitoso");
    }
    return error("Credenciales incorrectas");
  }

  @Override
  public LoginResponseDTO logout(String token) {
    // The real server answers OK even when the token no longer exists
    LoginResponseDTO r = new LoginResponseDTO();
    r.setEstado(EstadosEnum.OK);
    r.setMensaje("Sesión cerrada");
    return r;
  }

  private static LoginResponseDTO ok(String token, String nombre, String tipoUsuario,
      String perfil, String mensaje) {
    LoginResponseDTO r = new LoginResponseDTO();
    r.setEstado(EstadosEnum.OK);
    r.setMensaje(mensaje);
    r.setToken(token);
    r.setNombre(nombre);
    r.setUsername(nombre);
    r.setEstadoUsuario("ACTIVO");
    r.setTipoUsuario(tipoUsuario);
    r.setPerfil(perfil);
    return r;
  }

  private static LoginResponseDTO error(String mensaje) {
    LoginResponseDTO r = new LoginResponseDTO();
    r.setEstado(EstadosEnum.ERROR);
    r.setMensaje(mensaje);
    r.setToken("");
    r.setNombre("");
    r.setEstadoUsuario("INACTIVO");
    r.setTipoUsuario("");
    return r;
  }
}
