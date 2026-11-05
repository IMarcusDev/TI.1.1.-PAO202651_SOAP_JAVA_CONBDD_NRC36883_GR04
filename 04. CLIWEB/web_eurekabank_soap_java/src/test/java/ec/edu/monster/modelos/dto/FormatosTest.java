package ec.edu.monster.modelos.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Display formats of the movement history (UI template). */
class FormatosTest {

  @ParameterizedTest
  @CsvSource({
      "2026-11-03T09:15:00, 03/11/2026 09:15",
      "2026-01-05T23:59:59, 05/01/2026 23:59",
      "no es fecha, no es fecha"
  })
  void serverDatesAreShownAsDayMonthYearHourMinute(String iso, String esperado) {
    assertEquals(esperado, Formatos.fecha(iso));
  }

  @Test
  void missingDateIsEmpty() {
    assertEquals("", Formatos.fecha(null));
    assertEquals("", Formatos.fecha(" "));
  }

  @Test
  void amountsHaveDollarSignThousandsSeparatorAndTwoDecimals() {
    assertEquals("$ 1,250.00", Formatos.dinero(new BigDecimal("1250")));
    assertEquals("$ 0.50", Formatos.dinero(new BigDecimal("0.5")));
    assertEquals("", Formatos.dinero(null));
  }

  @Test
  void movementNumberHasSixDigits() {
    assertEquals("000006", Formatos.numeroMovimiento(6));
    assertEquals("123456", Formatos.numeroMovimiento(123456));
  }

  @Test
  void emptyReferenceIsShownAsDash() {
    assertEquals("-", Formatos.referencia(null));
    assertEquals("-", Formatos.referencia(""));
    assertEquals("00100002", Formatos.referencia("00100002"));
  }
}
