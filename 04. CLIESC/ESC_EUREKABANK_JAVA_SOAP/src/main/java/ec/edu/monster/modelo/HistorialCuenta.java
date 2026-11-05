package ec.edu.monster.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Answer of "Consultar movimientos": account data and movement history, every value
 * already formatted (see {@link Formatos}) so the Swing view only shows text.
 */
public record HistorialCuenta(CuentaConsultada cuenta, List<MovimientoConsultado> movimientos) {

    public HistorialCuenta {
        movimientos = movimientos == null ? List.of() : recientesPrimero(movimientos);
    }

    /**
     * Newest first, which is the order every screen shows. The movement number is the server's
     * global ID, which grows with every record, so the largest one is the newest. It does not depend
     * on the response order; numbers that cannot be read go last, in reverse arrival order.
     */
    private static List<MovimientoConsultado> recientesPrimero(List<MovimientoConsultado> movimientos) {
        List<MovimientoConsultado> copia = new ArrayList<>(movimientos);
        Collections.reverse(copia);
        copia.sort(Comparator.comparingLong(HistorialCuenta::numeroDe).reversed());
        return List.copyOf(copia);
    }

    private static long numeroDe(MovimientoConsultado movimiento) {
        try {
            return Long.parseLong(movimiento.numero().trim());
        } catch (RuntimeException e) {
            return Long.MIN_VALUE;
        }
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
