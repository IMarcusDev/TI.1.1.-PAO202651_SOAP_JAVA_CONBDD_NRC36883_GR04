package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.modelo.Formatos;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FormatosTest {

    @Test
    void movementNumberHasSixDigits() {
        assertEquals("000006", Formatos.numeroMovimiento(6));
    }

    @Test
    void isoDateIsShownAsDayMonthYearHourMinute() {
        assertEquals("20/10/2026 10:00", Formatos.fecha("2026-10-20T10:00:00"));
    }

    @Test
    void unknownDateFormatIsShownAsReceived() {
        assertEquals("ayer", Formatos.fecha("ayer"));
        assertEquals("", Formatos.fecha(null));
    }

    @Test
    void moneyHasDollarSignThousandsAndTwoDecimals() {
        assertEquals("$ 1,250.00", Formatos.dinero(new BigDecimal("1250")));
        assertEquals("", Formatos.dinero(null));
    }

    @Test
    void emptyReferenceIsADash() {
        assertEquals("-", Formatos.referencia(null));
        assertEquals("-", Formatos.referencia(" "));
        assertEquals("00100002", Formatos.referencia("00100002"));
    }
}
