package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controlador.ErroresCliente;
import ec.edu.monster.controlador.LoginController;
import ec.edu.monster.controlador.MovimientoController;
import ec.edu.monster.modelos.dto.ConsultaResponseDTO;
import ec.edu.monster.modelos.dto.LoginResponseDTO;
import ec.edu.monster.modelos.dto.MovimientoResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.modelos.http.ConfigHTTP;
import java.io.StringReader;
import java.math.BigDecimal;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

//revisado y aprovado por Marcos Escobar
class ControladoresTest {
  private static final String BASE = "http://servidor:8080/WS_EUREKABANK_JAVA_SOAP";

  private static String sobre(String respuesta) {
    return "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>" + respuesta + "</S:Body></S:Envelope>";
  }

  private static String ok(String contenido) {
    return sobre("<ns2:x xmlns:ns2=\"http://ws.monster.edu.ec/\"><return><estado>OK</estado><mensaje>ok</mensaje>"
      + contenido + "</return></ns2:x>");
  }

  private static ServidorFalso preparar(String respuesta) {
    ServidorFalso falso = new ServidorFalso(respuesta);
    ConfigHTTP.setTransporte(falso);
    ConfigHTTP.setBaseUrl(BASE);
    ConfigHTTP.limpiar();
    return falso;
  }

  private static void sesionEmpleado() {
    ConfigHTTP.setToken("tok-e");
    ConfigHTTP.setPerfil(ConfigHTTP.EMPLEADO);
  }

  /** El elemento de la operación dentro del soap:Body. */
  private static Element operacion(ServidorFalso s) throws Exception {
    DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
    fabrica.setNamespaceAware(true);
    Element raiz = fabrica.newDocumentBuilder().parse(new InputSource(new StringReader(s.cuerpo))).getDocumentElement();
    Element body = (Element) raiz.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Body").item(0);
    for (Node n = body.getFirstChild(); n != null; n = n.getNextSibling())
      if (n instanceof Element e)
        return e;
    throw new AssertionError("sin operación");
  }

  /** Los parámetros van sin namespace, como los espera Metro. */
  private static String parametro(Element operacion, String nombre) {
    NodeList hijos = operacion.getElementsByTagNameNS("", nombre);
    return hijos.getLength() == 0 ? null : hijos.item(0).getTextContent();
  }

  @Test
  void loginEmpleadoUrlSobreYSesion() throws Exception {
    ServidorFalso s = preparar(ok("<nombre>Carla Andrade</nombre><perfil>EMPLEADO</perfil><token>tok-e</token>"));
    LoginResponseDTO r = new LoginController().loginEmpleado("cajero", "cajero123");
    assertEquals(EstadosEnum.OK, r.getEstado());
    assertEquals(BASE + "/LoginWebService", s.url);
    assertEquals("text/xml; charset=utf-8", s.tipoContenido);
    Element op = operacion(s);
    assertEquals("loginEmpleado", op.getLocalName());
    assertEquals("http://ws.monster.edu.ec/", op.getNamespaceURI());
    assertEquals("cajero", parametro(op, "usuario"));
    assertEquals("cajero123", parametro(op, "clave"));
    assertEquals("tok-e", ConfigHTTP.getToken());
    assertFalse(ConfigHTTP.esCliente());
  }

  @Test
  void loginClienteGuardaLaCuenta() throws Exception {
    ServidorFalso s = preparar(ok("<nombre>Ana Torres</nombre><perfil>CLIENTE</perfil><token>tok-c</token>"));
    new LoginController().loginCliente("00100001", "123456");
    assertEquals("loginCliente", operacion(s).getLocalName());
    assertEquals("00100001", parametro(operacion(s), "cuenta"));
    assertTrue(ConfigHTTP.esCliente());
    assertEquals("00100001", ConfigHTTP.getCuenta());
  }

  @Test
  void loginRechazadoNoAbreSesion() {
    preparar(sobre("<ns2:r xmlns:ns2=\"http://ws.monster.edu.ec/\"><return><estado>ERROR</estado><mensaje>Credenciales incorrectas</mensaje></return></ns2:r>"));
    LoginResponseDTO r = new LoginController().loginCliente("00100001", "999999");
    assertEquals("Credenciales incorrectas", r.getMensaje());
    assertFalse(ConfigHTTP.haySesion());
  }

  @Test
  void sinServidorSoapFaultORespuestaQueNoEsXml() {
    ServidorFalso s = preparar("");
    s.sinRed = true;
    assertEquals(ErroresCliente.SIN_SERVIDOR, new LoginController().loginEmpleado("cajero", "x").getMensaje());
    s.sinRed = false;
    s.codigo = 500;
    s.respuesta = sobre("<S:Fault><faultcode>S:Client</faultcode><faultstring>x</faultstring></S:Fault>");
    assertEquals(ErroresCliente.FALLA_SERVIDOR, new LoginController().loginEmpleado("cajero", "x").getMensaje());
    s.codigo = 404;
    s.respuesta = "<html>404";
    assertEquals(ErroresCliente.RESPUESTA_INCOMPLETA, new LoginController().loginEmpleado("cajero", "x").getMensaje());
  }

  @Test
  void logoutEnviaTokenYBorraLaSesionAunqueFalleLaRed() throws Exception {
    ServidorFalso s = preparar("");
    sesionEmpleado();
    s.sinRed = true;
    new LoginController().logout();
    assertEquals("logout", operacion(s).getLocalName());
    assertEquals("tok-e", parametro(operacion(s), "token"));
    assertFalse(ConfigHTTP.haySesion());
  }

  @Test
  void consultarEnviaTokenYCuenta() throws Exception {
    ServidorFalso s = preparar(ok("<cuenta><numeroCuenta>00100001</numeroCuenta><saldo>500.00</saldo></cuenta>"));
    sesionEmpleado();
    ConsultaResponseDTO r = new MovimientoController().consultar("00100001");
    assertEquals(BASE + "/MovimientoWebService", s.url);
    assertEquals("consultarMovimientos", operacion(s).getLocalName());
    assertEquals("00100001", parametro(operacion(s), "cuenta"));
    assertEquals(new BigDecimal("500.00"), r.getSaldo());
  }

  @Test
  void depositoConImporteDeDosDecimalesYSinClave() throws Exception {
    ServidorFalso s = preparar(ok("<numeroMovimiento>5</numeroMovimiento><nuevoSaldo>620.50</nuevoSaldo>"));
    sesionEmpleado();
    MovimientoResponseDTO r = new MovimientoController().deposito("00100001", new BigDecimal("120.5"));
    assertEquals("registrarDeposito", operacion(s).getLocalName());
    assertEquals("120.50", parametro(operacion(s), "importe"));
    assertNull(parametro(operacion(s), "clave"));
    assertEquals(5, r.getNumeroMovimiento());
    assertEquals(new BigDecimal("620.50"), r.getNuevoSaldo());
  }

  @Test
  void retiroDelClienteConClaveVacia() throws Exception {
    ServidorFalso s = preparar(ok(""));
    ConfigHTTP.setToken("tok-c");
    new MovimientoController().retiro("00100001", new BigDecimal("40"), "");
    assertEquals("registrarRetiro", operacion(s).getLocalName());
    assertEquals("", parametro(operacion(s), "clave"));
    assertEquals("40.00", parametro(operacion(s), "importe"));
  }

  @Test
  void transferenciaConLasDosCuentasYLaClave() throws Exception {
    ServidorFalso s = preparar(ok(""));
    sesionEmpleado();
    new MovimientoController().transferencia("00100002", "00100001", new BigDecimal("200"), "654321");
    Element op = operacion(s);
    assertEquals("registrarTransferencia", op.getLocalName());
    assertEquals("00100002", parametro(op, "cuentaOrigen"));
    assertEquals("00100001", parametro(op, "cuentaDestino"));
    assertEquals("654321", parametro(op, "clave"));
  }

  @Test
  void tokenVencidoEsSesionExpirada() {
    preparar(sobre("<ns2:r xmlns:ns2=\"http://ws.monster.edu.ec/\"><return><estado>ERROR</estado><mensaje>Token inválido o sesión expirada</mensaje></return></ns2:r>"));
    assertTrue(new MovimientoController().retiro("00100001", BigDecimal.ONE, "").sesionExpirada());
  }
}
