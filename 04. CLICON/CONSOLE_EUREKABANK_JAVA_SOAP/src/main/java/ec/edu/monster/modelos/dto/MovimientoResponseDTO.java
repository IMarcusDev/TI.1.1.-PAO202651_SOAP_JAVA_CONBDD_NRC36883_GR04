package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;
import java.math.BigDecimal;
import org.w3c.dom.Element;

/** Resultado de una operación; el comprobante (RNF-010) muestra el número de movimiento y el nuevo saldo. */
//revisado y aprovado por Marcos Escobar
public class MovimientoResponseDTO extends ResponseDTO {
  private int idMovimiento;
  private int numeroMovimiento;
  private BigDecimal nuevoSaldo;

  public MovimientoResponseDTO(EstadosEnum estado, String mensaje, int idMovimiento) {
    super(estado, mensaje);
    this.idMovimiento = idMovimiento;
  }

  public int getIdMovimiento() {
    return idMovimiento;
  }

  public int getNumeroMovimiento() {
    return numeroMovimiento;
  }

  public BigDecimal getNuevoSaldo() {
    return nuevoSaldo;
  }

  public static MovimientoResponseDTO fromXML(String xml) {
    Element cuerpo = cuerpo(xml);

    MovimientoResponseDTO dto = new MovimientoResponseDTO(
      leerEstado(cuerpo), leerTexto(cuerpo, "mensaje"), leerEntero(cuerpo, "idMovimiento"));
    dto.numeroMovimiento = leerEntero(cuerpo, "numeroMovimiento");
    dto.nuevoSaldo = leerDecimal(cuerpo, "nuevoSaldo");
    return dto;
  }
}
