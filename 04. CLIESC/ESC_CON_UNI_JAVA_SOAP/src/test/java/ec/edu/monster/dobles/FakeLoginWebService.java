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
}
