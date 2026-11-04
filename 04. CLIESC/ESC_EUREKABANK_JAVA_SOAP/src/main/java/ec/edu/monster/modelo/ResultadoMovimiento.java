package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Result of a movement accepted by the server: the movement number, its message and
 * the new balance (null when the server does not send it).
 */
public record ResultadoMovimiento(int idMovimiento, String mensaje, BigDecimal nuevoSaldo) {

    /** Legacy operations do not return the new balance. */
    public ResultadoMovimiento(int idMovimiento, String mensaje) {
        this(idMovimiento, mensaje, null);
    }
}
