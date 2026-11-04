package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.w3c.dom.Element;

/** Respuesta de consultarMovimientos: los datos de la cuenta y la lista de sus movimientos. */
//revisado y aprovado por Marcos Escobar
public class ConsultaResponseDTO extends ResponseDTO {
  private String numeroCuenta = "";
  private String titular = "";
  private String moneda = "";
  private BigDecimal saldo;
  private String estadoCuenta = "";
  private final List<MovimientoDTO> movimientos = new ArrayList<>();

  public ConsultaResponseDTO(EstadosEnum estado, String mensaje) {
    super(estado, mensaje);
  }

  public String getNumeroCuenta() {
    return numeroCuenta;
  }

  public String getTitular() {
    return titular;
  }

  public String getMoneda() {
    return moneda;
  }

  public BigDecimal getSaldo() {
    return saldo;
  }

  public String getEstadoCuenta() {
    return estadoCuenta;
  }

  public List<MovimientoDTO> getMovimientos() {
    return movimientos;
  }

  //revisado y aprovado por Marcos Escobar
  /** Movimientos del más reciente al más antiguo (mayor número de movimiento primero): es el orden de la tabla. */
  public List<MovimientoDTO> getMovimientosRecientes() {
    List<MovimientoDTO> recientes = new ArrayList<>(movimientos);
    recientes.sort(Comparator.comparingInt(MovimientoDTO::getNumeroMovimiento).reversed());
    return recientes;
  }

  /**
   * JAXB envía cada movimiento como un elemento &lt;movimientos&gt; repetido; también se aceptan envueltos
   * en un contenedor. Se toma cada elemento que tenga numeroMovimiento y accion.
   */
  public static ConsultaResponseDTO fromXML(String xml) {
    Element cuerpo = cuerpo(xml);
    ConsultaResponseDTO dto = new ConsultaResponseDTO(leerEstado(cuerpo), leerTexto(cuerpo, "mensaje"));

    dto.numeroCuenta = leerTexto(cuerpo, "numeroCuenta");
    dto.titular = leerTexto(cuerpo, "titular");
    dto.moneda = leerTexto(cuerpo, "moneda");
    dto.saldo = leerDecimal(cuerpo, "saldo");
    dto.estadoCuenta = leerTexto(cuerpo, "estadoCuenta");

    for (Element m : descendientes(cuerpo)) {
      if (tieneHijo(m, "numeroMovimiento") && tieneHijo(m, "accion")) {
        dto.movimientos.add(new MovimientoDTO(
          leerEntero(m, "numeroMovimiento"),
          leerTexto(m, "fecha"),
          leerTexto(m, "tipoMovimiento"),
          leerTexto(m, "accion"),
          leerDecimal(m, "importe"),
          leerTexto(m, "cuentaReferencia")
        ));
      }
    }

    return dto;
  }
}
