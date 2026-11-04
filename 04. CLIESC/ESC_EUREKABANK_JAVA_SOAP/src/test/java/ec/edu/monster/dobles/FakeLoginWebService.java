package ec.edu.monster.dobles;

import ec.edu.monster.ws.login.EstadosEnum;
import ec.edu.monster.ws.login.LoginResponseDTO;
import ec.edu.monster.ws.login.LoginWebService;
import jakarta.jws.WebService;

/**
 * Fake LoginWebService that follows the same contract (generated interface) as
 * the real server. Accepts only the user "admin" with password "1234".
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
        LoginResponseDTO respuesta = new LoginResponseDTO();
        if ("admin".equals(username) && "1234".equals(password)) {
            respuesta.setEstado(EstadosEnum.OK);
            respuesta.setMensaje("Ingreso de Sesión Exitoso!");
            respuesta.setToken(TOKEN);
            respuesta.setNombre("Administrador");
            respuesta.setEstadoUsuario("ACTIVO");
            respuesta.setTipoUsuario("Empleado");
        } else {
            // Same shape the real server returns for wrong credentials
            respuesta.setEstado(EstadosEnum.ERROR);
            respuesta.setMensaje("Contraseña incorrecta");
            respuesta.setToken("");
        }
        return respuesta;
    }

    @Override
    public LoginResponseDTO loginEmpleado(String usuario, String clave) {
        if ("cajero".equals(usuario) && "cajero123".equals(clave)) {
            return ok(TOKEN, "Carla Andrade", "Empleado");
        }
        return error("Credenciales incorrectas");
    }

    @Override
    public LoginResponseDTO loginCliente(String cuenta, String clave) {
        if ("00100001".equals(cuenta) && "123456".equals(clave)) {
            return ok(TOKEN_CLIENTE, "Ana Torres", "Cliente");
        }
        if ("00100003".equals(cuenta)) {
            return error("La cuenta no se encuentra activa");
        }
        return error("Credenciales incorrectas");
    }

    @Override
    public LoginResponseDTO logout(String token) {
        LoginResponseDTO respuesta = new LoginResponseDTO();
        respuesta.setEstado(EstadosEnum.OK);
        respuesta.setMensaje("Sesión cerrada");
        return respuesta;
    }

    private static LoginResponseDTO ok(String token, String nombre, String tipoUsuario) {
        LoginResponseDTO respuesta = new LoginResponseDTO();
        respuesta.setEstado(EstadosEnum.OK);
        respuesta.setMensaje("Ingreso de sesión exitoso");
        respuesta.setToken(token);
        respuesta.setNombre(nombre);
        respuesta.setEstadoUsuario("ACTIVO");
        respuesta.setTipoUsuario(tipoUsuario);
        return respuesta;
    }

    private static LoginResponseDTO error(String mensaje) {
        LoginResponseDTO respuesta = new LoginResponseDTO();
        respuesta.setEstado(EstadosEnum.ERROR);
        respuesta.setMensaje(mensaje);
        respuesta.setToken("");
        return respuesta;
    }
}
