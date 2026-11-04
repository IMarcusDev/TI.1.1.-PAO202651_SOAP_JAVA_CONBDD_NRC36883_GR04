package ec.edu.monster.modelo;

import java.util.List;

/**
 * Answer of "Consultar movimientos": account data and movement history, every value
 * already formatted (see {@link Formatos}) so the Swing view only shows text.
 */
public record HistorialCuenta(CuentaConsultada cuenta, List<MovimientoConsultado> movimientos) {

    public HistorialCuenta {
        movimientos = movimientos == null ? List.of() : List.copyOf(movimientos);
    }

    public boolean sinMovimientos() {
        return movimientos.isEmpty();
    }

    /** Account data shown above the table. */
    public record CuentaConsultada(String numero, String titular, String moneda, String saldo, String estado) {
    }

    /** One table row. */
    public record MovimientoConsultado(String numero, String fecha, String tipo, String accion, String importe,
            String referencia) {

        /** INGRESO is shown as a positive movement; the server sends INGRESO / SALIDA. */
        public boolean esIngreso() {
            return "INGRESO".equals(accion);
        }
    }
}
