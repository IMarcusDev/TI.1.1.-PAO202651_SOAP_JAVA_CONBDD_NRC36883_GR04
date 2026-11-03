/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/WebServices/WebService.java to edit this template
 */
package ec.edu.monster.ws;

import ec.edu.monster.controladores.LoginController;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.servicios.TokenService;
import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
//revisado y aprovado por Marcos Escobar
import ec.edu.monster.modelos.data.Sesion;

/**
 *
 * @author Mateo Sosa
 */
@WebService(serviceName = "LoginWebService")
public class LoginWebService {
  //revisado y aprovado por Marcos Escobar
  // Ya no indica si falló el usuario o la clave, revisa que el usuario esté activo
  // y el token queda asociado al perfil y al código del usuario.
  @WebMethod(operationName = "login")
  public LoginResponseDTO login(
    @WebParam(name = "username") String username,
    @WebParam(name = "password") String password
  ) {
    try {
      Usuario u = LoginController.autenticar(username, password);

      String token = TokenService.generateToken(LoginController.perfilDe(u), u.getId());

      return new LoginResponseDTO(
        EstadosEnum.OK,
        "Ingreso de Sesión Exitoso!",
        token,
        u.getNombre(),
        u.getEstado(),
        u.getTipo()
      );
    } catch (Exception e) {
      // e.printStackTrace();

      return new LoginResponseDTO(
        EstadosEnum.ERROR,
        e.getLocalizedMessage(),
        "",
        "",
        "INACTIVO",
        ""
      );
    }
  }

  //revisado y aprovado por Marcos Escobar
  @WebMethod(operationName = "loginEmpleado")
  public LoginResponseDTO loginEmpleado(
    @WebParam(name = "usuario") String usuario,
    @WebParam(name = "clave") String clave
  ) {
    try {
      Usuario u = LoginController.autenticarEmpleado(usuario, clave);
      return sesionIniciada(u, Sesion.EMPLEADO, u.getId());
    } catch (Exception e) {
      return error(e.getLocalizedMessage());
    }
  }

  //revisado y aprovado por Marcos Escobar
  @WebMethod(operationName = "loginCliente")
  public LoginResponseDTO loginCliente(
    @WebParam(name = "cuenta") String cuenta,
    @WebParam(name = "clave") String clave
  ) {
    try {
      Usuario u = LoginController.autenticarCliente(cuenta, clave);
      return sesionIniciada(u, Sesion.CLIENTE, cuenta);
    } catch (Exception e) {
      return error(e.getLocalizedMessage());
    }
  }

  //revisado y aprovado por Marcos Escobar
  // Elimina el token. Responde OK aunque ya no exista, porque la sesión queda cerrada igual.
  @WebMethod(operationName = "logout")
  public LoginResponseDTO logout(@WebParam(name = "token") String token) {
    TokenService.removeToken(token);
    return new LoginResponseDTO(EstadosEnum.OK, "Sesión cerrada", "", "", "", "");
  }

  //revisado y aprovado por Marcos Escobar
  private static LoginResponseDTO sesionIniciada(Usuario u, String perfil, String codigo) {
    LoginResponseDTO respuesta = new LoginResponseDTO(
      EstadosEnum.OK,
      "Ingreso de sesión exitoso",
      TokenService.generateToken(perfil, codigo),
      u.getNombre(),
      u.getEstado(),
      u.getTipo()
    );
    respuesta.setPerfil(perfil);
    return respuesta;
  }

  //revisado y aprovado por Marcos Escobar
  private static LoginResponseDTO error(String mensaje) {
    return new LoginResponseDTO(EstadosEnum.ERROR, mensaje, "", "", "INACTIVO", "");
  }
}
