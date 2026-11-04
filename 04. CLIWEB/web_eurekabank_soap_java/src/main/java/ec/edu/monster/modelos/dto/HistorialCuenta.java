package ec.edu.monster.modelos.dto;

import ec.edu.monster.ws.movimiento.EstadosEnum;
import java.math.BigDecimal;
import java.util.List;

/**
 * Answer of "Consultar movimientos": account details and movement history, already
 * formatted for the view. Built by MovimientoService from the generated SOAP DTOs.
 * movimientos.jsp reads it through EL, which (Jakarta EE 10) does not resolve record
 * accessors: every property the view uses has a JavaBean getter.
 */
public record HistorialCuenta(
    EstadosEnum estado,
    String mensaje,
    CuentaConsultada cuenta,
    List<MovimientoConsultado> movimientos) {

  public HistorialCuenta {
    movimientos = movimientos == null ? List.of() : List.copyOf(movimientos);
  }

  /** Failed query (server error or connection problem): no account, no rows. */
  public static HistorialCuenta error(EstadosEnum estado, String mensaje) {
    return new HistorialCuenta(estado, mensaje, null, List.of());
  }

  public CuentaConsultada getCuenta() {
    return cuenta;
  }

  public List<MovimientoConsultado> getMovimientos() {
    return movimientos;
  }

  public boolean isSinMovimientos() {
    return movimientos.isEmpty();
  }

  /** Account data shown above the table. */
  public record CuentaConsultada(
      String numero, String titular, String moneda, BigDecimal saldo, String estado) {

    public String getNumero() {
      return numero;
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

    public String getSaldoFormateado() {
      return Formatos.dinero(saldo);
    }

    public String getEstado() {
      return estado;
    }
  }

  /** One movement row: number, date and reference already formatted, amount as a BigDecimal. */
  public record MovimientoConsultado(
      String numero, String fecha, String tipo, String accion, BigDecimal importe, String referencia) {

    public String getNumero() {
      return numero;
    }

    public String getFecha() {
      return fecha;
    }

    public String getTipo() {
      return tipo;
    }

    public String getAccion() {
      return accion;
    }

    /** INGRESO is styled as a positive movement; the server sends INGRESO / SALIDA. */
    public boolean isIngreso() {
      return "INGRESO".equals(accion);
    }

    public BigDecimal getImporte() {
      return importe;
    }

    public String getImporteFormateado() {
      return Formatos.dinero(importe);
    }

    public String getReferencia() {
      return referencia;
    }
  }
}
