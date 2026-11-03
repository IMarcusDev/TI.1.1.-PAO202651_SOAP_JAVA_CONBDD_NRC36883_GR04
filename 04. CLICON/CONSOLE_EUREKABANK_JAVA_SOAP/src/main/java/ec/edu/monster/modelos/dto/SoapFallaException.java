package ec.edu.monster.modelos.dto;

/** Un SOAP Fault: el servidor no pudo procesar la petición. */
//revisado y aprovado por Marcos Escobar
public class SoapFallaException extends RuntimeException {
  public SoapFallaException(String mensaje) {
    super(mensaje);
  }
}
