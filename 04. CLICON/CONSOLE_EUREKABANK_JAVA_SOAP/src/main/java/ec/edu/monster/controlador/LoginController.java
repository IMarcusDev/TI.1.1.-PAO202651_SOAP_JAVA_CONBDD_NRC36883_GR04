package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Endpoints;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.dto.ResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.modelos.http.SOAPClient;

//revisado y aprovado por Marcos Escobar
public class LoginController {
  /** ECUD1: el empleado ingresa con su usuario y clave. */
  public LoginResponseDTO loginEmpleado(String usuario, String clave) {
    return iniciarSesion(Endpoints.LOGIN_EMPLEADO, null, "usuario", usuario, "clave", clave);
  }

  /** ECUD1: el cliente ingresa con el número de su cuenta y la clave de esa cuenta. */
  public LoginResponseDTO loginCliente(String cuenta, String clave) {
    return iniciarSesion(Endpoints.LOGIN_CLIENTE, cuenta, "cuenta", cuenta, "clave", clave);
  }

  /** Cierra la sesión en el servidor; los datos locales se borran aunque el servidor no responda. */
  public ResponseDTO logout() {
    try {
      String xml = new SOAPClient().post(Endpoints.LOGIN, Endpoints.LOGOUT, "token", ConfigHTTP.getToken());
      return ResponseDTO.fromXML(xml);
    } catch (Exception e) {
      return new ResponseDTO(EstadosEnum.ERROR, ErroresCliente.mensaje(e));
    } finally {
      ConfigHTTP.limpiar();
    }
  }

  private LoginResponseDTO iniciarSesion(String operacion, String cuenta, String... parametros) {
    try {
      String xml = new SOAPClient().post(Endpoints.LOGIN, operacion, parametros);
      LoginResponseDTO dto = LoginResponseDTO.fromXML(xml);

      if (dto.getEstado() == EstadosEnum.OK) {
        ConfigHTTP.setToken(dto.getToken());
        ConfigHTTP.setPerfil(dto.getPerfil());
        ConfigHTTP.setNombre(dto.getNombre());
        ConfigHTTP.setCuenta(cuenta);
      }

      return dto;
    } catch (Exception e) {
      return new LoginResponseDTO(EstadosEnum.ERROR, ErroresCliente.mensaje(e), "");
    }
  }
}
