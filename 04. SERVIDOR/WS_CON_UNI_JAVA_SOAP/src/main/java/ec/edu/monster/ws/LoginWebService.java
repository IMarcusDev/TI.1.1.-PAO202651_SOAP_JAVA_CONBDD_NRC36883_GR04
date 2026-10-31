/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/WebServices/WebService.java to edit this template
 */
package ec.edu.monster.ws;

import ec.edu.monster.controladores.LoginController;
import ec.edu.monster.modelos.data.Perfil;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.servicios.HashService;
import ec.edu.monster.servicios.TokenService;
import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;

/**
 *
 * @author Mateo Sosa
 */
@WebService(serviceName = "LoginWebService")
public class LoginWebService {  
  @WebMethod(operationName = "login")
  public LoginResponseDTO login(
    @WebParam(name = "username") String username,
    @WebParam(name = "password") String password
  ) {
    try {
      Perfil p = LoginController.getPerfil(username);
      Usuario u = LoginController.getUsuarioById(p.getId());
      
      if (!HashService.verifyPassword(password, p.getPasswordHash()))
        throw new Exception("Contraseña incorrecta");
      
      String token = TokenService.generateToken();
      
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
}
