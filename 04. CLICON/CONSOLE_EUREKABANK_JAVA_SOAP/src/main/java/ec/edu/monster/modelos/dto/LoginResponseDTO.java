package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;
import org.w3c.dom.Element;

//revisado y aprovado por Marcos Escobar
public class LoginResponseDTO extends ResponseDTO {
  private String token;
  private String perfil = "";
  private String nombre = "";

  public LoginResponseDTO(EstadosEnum estado, String mensaje, String token) {
    super(estado, mensaje);
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public String getPerfil() {
    return perfil;
  }

  public String getNombre() {
    return nombre;
  }

  public static LoginResponseDTO fromXML(String xml) {
    Element cuerpo = cuerpo(xml);

    LoginResponseDTO dto = new LoginResponseDTO(leerEstado(cuerpo), leerTexto(cuerpo, "mensaje"), leerTexto(cuerpo, "token"));
    dto.perfil = leerTexto(cuerpo, "perfil");
    dto.nombre = leerTexto(cuerpo, "nombre");
    return dto;
  }
}
