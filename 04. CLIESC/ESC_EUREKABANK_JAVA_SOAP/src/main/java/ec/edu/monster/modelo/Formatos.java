package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Display formats of "Consultar movimientos", the same ones the web client uses:
 * movement number with 6 digits, date dd/MM/yyyy HH:mm, money "$ 1,250.00" and
 * "-" for an empty reference account.
 */
public final class Formatos {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formatos() {
        // Utility class: only static methods, no instances
    }

    /** "2026-10-20T10:00:00" -> "20/10/2026 10:00"; an unknown format is shown as received. */
    public static String fecha(String iso) {
        if (iso == null || iso.isBlank()) {
            return "";
        }
        try {
            return LocalDateTime.parse(iso).format(FECHA);
        } catch (DateTimeParseException e) {
            return iso;
        }
    }

    /** 1250 -> "$ 1,250.00"; null -> "". */
    public static String dinero(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        // DecimalFormat is not thread-safe: a new instance per call
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "$ " + formato.format(valor);
    }

    /** Empty reference account -> "-". */
    public static String referencia(String cuenta) {
        return cuenta == null || cuenta.isBlank() ? "-" : cuenta;
    }

    /** 6 -> "000006". */
    public static String numeroMovimiento(int numero) {
        return String.format("%06d", numero);
    }
}
