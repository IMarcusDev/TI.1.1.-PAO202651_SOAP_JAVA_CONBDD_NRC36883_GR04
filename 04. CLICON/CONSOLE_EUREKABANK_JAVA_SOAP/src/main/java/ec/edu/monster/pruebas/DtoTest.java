package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.dto.ConsultaResponseDTO;
import ec.edu.monster.modelos.dto.MovimientoDTO;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.dto.MovimientoResponseDTO;
import ec.edu.monster.modelos.dto.ResponseDTO;
import ec.edu.monster.modelos.dto.SoapFallaException;
import ec.edu.monster.modelos.enums.EstadosEnum;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

//revisado y aprovado por Marcos Escobar
class DtoTest {
  private static String sobre(String respuesta) {
    return "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>" + respuesta + "</S:Body></S:Envelope>";
  }

  private static String respuesta(String operacion, String contenido) {
    return sobre("<ns2:" + operacion + "Response xmlns:ns2=\"http://ws.monster.edu.ec/\"><return>" + contenido
      + "</return></ns2:" + operacion + "Response>");
  }

  @Test
  void estadoYMensajeEnMinusculasComoJaxb() {
    ResponseDTO r = ResponseDTO.fromXML(respuesta("logout", "<estado>OK</estado><mensaje>Sesión cerrada</mensaje>"));
    assertEquals(EstadosEnum.OK, r.getEstado());
    assertEquals("Sesión cerrada", r.getMensaje());
  }

  @Test
  void tokenInvalidoEsSesionExpirada() {
    assertTrue(ResponseDTO.fromXML(respuesta("x", "<estado>ERROR</estado><mensaje>Token inválido o sesión expirada</mensaje>")).sesionExpirada());
  }

  @Test
  void soapFaultLanzaSoapFallaException() {
    assertThrows(SoapFallaException.class, () -> ResponseDTO.fromXML(
      sobre("<S:Fault><faultcode>S:Client</faultcode><faultstring>No existe</faultstring></S:Fault>")));
  }

  @Test
  void loginTraeTokenPerfilYNombre() {
    LoginResponseDTO dto = LoginResponseDTO.fromXML(respuesta("loginEmpleado",
      "<estado>OK</estado><estadoUsuario>ACTIVO</estadoUsuario><mensaje>Ingreso de sesión exitoso</mensaje><nombre>Carla Andrade</nombre>"
      + "<perfil>EMPLEADO</perfil><tipoUsuario>Empleado</tipoUsuario><token>t-1</token><username>Carla Andrade</username>"));
    assertEquals(EstadosEnum.OK, dto.getEstado());
    assertEquals("t-1", dto.getToken());
    assertEquals("EMPLEADO", dto.getPerfil());
    assertEquals("Carla Andrade", dto.getNombre());
  }

  @Test
  void operacionTraeNumeroYNuevoSaldo() {
    MovimientoResponseDTO dto = MovimientoResponseDTO.fromXML(respuesta("registrarRetiro",
      "<estado>OK</estado><mensaje>ok</mensaje><idMovimiento>23</idMovimiento><numeroMovimiento>23</numeroMovimiento><nuevoSaldo>475.00</nuevoSaldo>"));
    assertEquals(23, dto.getNumeroMovimiento());
    assertEquals(new BigDecimal("475.00"), dto.getNuevoSaldo());
  }

  @Test
  void consultaConMovimientosRepetidosComoJaxb() {
    ConsultaResponseDTO dto = ConsultaResponseDTO.fromXML(respuesta("consultarMovimientos",
      "<estado>OK</estado><mensaje>Consulta de movimientos exitosa</mensaje>"
      + "<cuenta><estadoCuenta>ACTIVO</estadoCuenta><moneda>USD</moneda><numeroCuenta>00100001</numeroCuenta><saldo>575.00</saldo><titular>Ana Torres</titular></cuenta>"
      + "<movimientos><accion>SALIDA</accion><cuentaReferencia>00100002</cuentaReferencia><fecha>2026-11-03T16:07:05</fecha><importe>25.00</importe><numeroMovimiento>23</numeroMovimiento><tipoMovimiento>Transferencia</tipoMovimiento></movimientos>"
      + "<movimientos><accion>INGRESO</accion><fecha>2026-11-03T16:09:00</fecha><importe>100.00</importe><numeroMovimiento>24</numeroMovimiento><tipoMovimiento>Depósito</tipoMovimiento></movimientos>"));
    assertEquals("00100001", dto.getNumeroCuenta());
    assertEquals("Ana Torres", dto.getTitular());
    assertEquals(new BigDecimal("575.00"), dto.getSaldo());
    assertEquals("ACTIVO", dto.getEstadoCuenta());
    assertEquals(2, dto.getMovimientos().size());
    assertEquals("00100002", dto.getMovimientos().get(0).getCuentaReferencia());
    assertEquals("", dto.getMovimientos().get(1).getCuentaReferencia());
    assertFalse(dto.getMovimientos().get(0).esIngreso());
    assertTrue(dto.getMovimientos().get(1).esIngreso());
  }

  @Test
  void consultaSinMovimientosYErrorSinDatos() {
    assertEquals(0, ConsultaResponseDTO.fromXML(respuesta("consultarMovimientos",
      "<estado>OK</estado><mensaje>ok</mensaje><cuenta><numeroCuenta>00100002</numeroCuenta></cuenta>")).getMovimientos().size());
    ConsultaResponseDTO error = ConsultaResponseDTO.fromXML(respuesta("consultarMovimientos",
      "<estado>ERROR</estado><mensaje>La cuenta no existe</mensaje>"));
    assertEquals("La cuenta no existe", error.getMensaje());
  }

  //revisado y aprovado por Marcos Escobar
  @Test
  void consultaMuestraPrimeroElUltimoMovimiento() {
    ConsultaResponseDTO dto = new ConsultaResponseDTO(EstadosEnum.OK, "ok");
    for (int n : new int[] { 3, 6, 10, 9 }) {
      dto.getMovimientos().add(new MovimientoDTO(n, "", "", "", BigDecimal.ONE, ""));
    }
    StringBuilder recientes = new StringBuilder();
    for (MovimientoDTO m : dto.getMovimientosRecientes()) {
      recientes.append(recientes.length() == 0 ? "" : ",").append(m.getNumeroMovimiento());
    }
    assertEquals("10,9,6,3", recientes.toString());
    assertEquals(3, dto.getMovimientos().get(0).getNumeroMovimiento());
  }
}
