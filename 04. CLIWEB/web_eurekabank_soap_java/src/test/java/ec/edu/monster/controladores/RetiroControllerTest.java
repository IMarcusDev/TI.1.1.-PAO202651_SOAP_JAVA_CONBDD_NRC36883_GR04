package ec.edu.monster.controladores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ec.edu.monster.modelos.servicios.MovimientoService;
import ec.edu.monster.ws.movimiento.EstadosEnum;
import ec.edu.monster.ws.movimiento.MovimientoResponseDTO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

/**
 * Tests the withdrawal controller through doGet/doPost with Mockito mocks;
 * the SOAP model (MovimientoService) is injected as a mock.
 * Empleado: any account, the account key is asked on confirmation.
 * Cliente: only the account stored in session, no key (sent as "").
 */
class RetiroControllerTest {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/retiro.jsp";
  private static final String CTX = "/web_eurekabank_soap_java";
  private static final String CLAVE_VACIA = "Ingrese la clave de la cuenta";

  private final MovimientoService service = mock(MovimientoService.class);
  private final RetiroController controller = new RetiroController(service);
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final RequestDispatcher dispatcher = mock(RequestDispatcher.class);
  private final HttpSession session = mock(HttpSession.class);

  @BeforeEach
  void setUp() {
    when(request.getRequestDispatcher(VISTA)).thenReturn(dispatcher);
    when(request.getContextPath()).thenReturn(CTX);
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("token")).thenReturn("abc123");
    when(session.getAttribute("tipoUsuario")).thenReturn("Empleado");
  }

  private void givenCliente() {
    when(session.getAttribute("tipoUsuario")).thenReturn("Cliente");
    when(session.getAttribute("cuenta")).thenReturn("00100001");
  }

  private void params(String paso, String cuenta, String importe) {
    when(request.getParameter("paso")).thenReturn(paso);
    when(request.getParameter("cuenta")).thenReturn(cuenta);
    when(request.getParameter("importe")).thenReturn(importe);
  }

  private static MovimientoResponseDTO respuesta(EstadosEnum estado, String mensaje, int numero,
      String nuevoSaldo) {
    MovimientoResponseDTO dto = new MovimientoResponseDTO();
    dto.setEstado(estado);
    dto.setMensaje(mensaje);
    dto.setNumeroMovimiento(numero);
    dto.setNuevoSaldo(nuevoSaldo == null ? null : new BigDecimal(nuevoSaldo));
    return dto;
  }

  // The key is never put back in the request or the session
  private void assertKeyNotKept() {
    verify(request, never()).setAttribute(eq("clave"), any());
    verify(session, never()).setAttribute(eq("clave"), any());
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> capturedReceipt() {
    ArgumentCaptor<Map<String, String>> comprobante = ArgumentCaptor.forClass(Map.class);
    verify(session).setAttribute(eq("comprobanteRetiro"), comprobante.capture());
    return comprobante.getValue();
  }

  @Test
  void withoutSessionRedirectsToLogin() throws Exception {
    when(request.getSession(false)).thenReturn(null);

    controller.doGet(request, response);
    controller.doPost(request, response);

    verify(response, times(2)).sendRedirect(CTX + "/login");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
    verifyNoInteractions(service);
  }

  @Test
  void sessionWithBlankTokenRedirectsToLogin() throws Exception {
    when(session.getAttribute("token")).thenReturn(" ");

    controller.doPost(request, response);

    verify(response).sendRedirect(CTX + "/login");
    verifyNoInteractions(service);
  }

  @Test
  void getShowsEmptyFormWithoutCache() throws Exception {
    controller.doGet(request, response);

    verify(response).setHeader("Cache-Control", "no-store");
    verify(request).setAttribute("vista", "formulario");
    verify(request, never()).setAttribute(eq("cuenta"), any());
    verify(dispatcher).forward(request, response);
  }

  @ParameterizedTest
  @ValueSource(strings = {"NaN", "abc", "0", "1.234", "-5"})
  void revisarWithInvalidAmountShowsErrorsAndKeepsValues(String importe) throws Exception {
    params("revisar", "00100001", importe);

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> errores = ArgumentCaptor.forClass(Map.class);
    verify(request).setAttribute(eq("errores"), errores.capture());
    assertEquals("El importe debe ser mayor que cero y tener como máximo dos decimales",
        errores.getValue().get("importe"));
    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("importe", importe);
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void revisarValidShowsConfirmationWithoutCallingService() throws Exception {
    params("revisar", " 00100001 ", "150");

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "confirmacion");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("importe", "150");
    verify(request).setAttribute("importeFormateado", "$ 150.00");
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void editarReturnsToFormKeepingValues() throws Exception {
    params("editar", "00100001", "150");
    when(request.getParameter("clave")).thenReturn("123456");

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("importe", "150");
    assertKeyNotKept();
    verifyNoInteractions(service);
  }

  @Test
  void confirmarRevalidatesHiddenFields() throws Exception {
    params("confirmar", "00100001", "-5");
    when(request.getParameter("clave")).thenReturn("123456");

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "formulario");
    verifyNoInteractions(service);
    verify(response, never()).sendRedirect(anyString());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  void employeeConfirmWithoutKeyShowsConfirmationAgainWithoutCallingService(String clave)
      throws Exception {
    params("confirmar", "00100001", "150");
    when(request.getParameter("clave")).thenReturn(clave);

    controller.doPost(request, response);

    verify(request).setAttribute("errores", Map.of("clave", CLAVE_VACIA));
    verify(request).setAttribute("vista", "confirmacion");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("importe", "150");
    verify(request).setAttribute("importeFormateado", "$ 150.00");
    verify(dispatcher).forward(request, response);
    assertKeyNotKept();
    verifyNoInteractions(service);
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void employeeConfirmSendsTheKeyAsTypedStoresReceiptAndRedirects() throws Exception {
    params("confirmar", "00100001", "150,5");
    when(request.getParameter("clave")).thenReturn(" 123456");
    when(service.registrarRetiro("abc123", "00100001", new BigDecimal("150.5"), " 123456"))
        .thenReturn(respuesta(EstadosEnum.OK, "Retiro registrado", 42, "349.50"));

    controller.doPost(request, response);

    verify(service, times(1)).registrarRetiro("abc123", "00100001", new BigDecimal("150.5"), " 123456");
    Map<String, String> c = capturedReceipt();
    assertEquals("Retiro", c.get("operacion"));
    assertEquals("00100001", c.get("cuenta"));
    assertEquals("$ 150.50", c.get("importe"));
    assertEquals("42", c.get("idMovimiento"));
    assertEquals("$ 349.50", c.get("nuevoSaldo"));
    assertEquals("Retiro registrado", c.get("mensaje"));
    assertNotNull(c.get("fecha"));
    assertFalse(c.containsValue(" 123456"));
    assertKeyNotKept();
    verify(response).sendRedirect(CTX + "/retiro");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
  }

  @Test
  void receiptWithoutNewBalanceOmitsIt() throws Exception {
    params("confirmar", "00100001", "150");
    when(request.getParameter("clave")).thenReturn("123456");
    when(service.registrarRetiro("abc123", "00100001", new BigDecimal("150"), "123456"))
        .thenReturn(respuesta(EstadosEnum.OK, "Retiro registrado", 42, null));

    controller.doPost(request, response);

    assertFalse(capturedReceipt().containsKey("nuevoSaldo"));
    verify(response).sendRedirect(CTX + "/retiro");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Clave de cuenta incorrecta", "Saldo insuficiente",
      "Token inválido o sesión expirada"})
  void confirmarErrorShowsServerMessageAndKeepsValues(String mensaje) throws Exception {
    params("confirmar", "00100001", "150");
    when(request.getParameter("clave")).thenReturn("123456");
    when(service.registrarRetiro("abc123", "00100001", new BigDecimal("150"), "123456"))
        .thenReturn(respuesta(EstadosEnum.ERROR, mensaje, 0, null));

    controller.doPost(request, response);

    verify(request).setAttribute("error", mensaje);
    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("importe", "150");
    verify(session, never()).setAttribute(eq("comprobanteRetiro"), any());
    assertKeyNotKept();
    verify(response, never()).sendRedirect(anyString());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void getAfterSuccessShowsReceiptAndClearsFlash() throws Exception {
    Map<String, String> comprobante = Map.of("operacion", "Retiro");
    when(session.getAttribute("comprobanteRetiro")).thenReturn(comprobante);

    controller.doGet(request, response);

    verify(request).setAttribute("comprobante", comprobante);
    verify(request).setAttribute("vista", "comprobante");
    verify(session).removeAttribute("comprobanteRetiro");
    verify(dispatcher).forward(request, response);
  }

  @Test
  void clienteGetShowsFormWithOwnAccount() throws Exception {
    givenCliente();

    controller.doGet(request, response);

    verify(response).setHeader("Cache-Control", "no-store");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("vista", "formulario");
    verify(dispatcher).forward(request, response);
  }

  // A tampered "cuenta" parameter is ignored: the account always comes from the session
  @Test
  void clienteRevisarUsesOwnAccountIgnoringTheForm() throws Exception {
    givenCliente();
    params("revisar", "00100002", "50");

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "confirmacion");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request, never()).setAttribute("cuenta", "00100002");
    verifyNoInteractions(service);
  }

  @Test
  void clienteConfirmUsesOwnAccountAndAnEmptyKey() throws Exception {
    givenCliente();
    params("confirmar", "00100002", "50");
    when(request.getParameter("clave")).thenReturn("999999");
    when(service.registrarRetiro("abc123", "00100001", new BigDecimal("50"), ""))
        .thenReturn(respuesta(EstadosEnum.OK, "Retiro registrado", 43, "450.00"));

    controller.doPost(request, response);

    verify(service, times(1)).registrarRetiro("abc123", "00100001", new BigDecimal("50"), "");
    Map<String, String> c = capturedReceipt();
    assertEquals("00100001", c.get("cuenta"));
    assertEquals("$ 450.00", c.get("nuevoSaldo"));
    assertKeyNotKept();
    verify(response).sendRedirect(CTX + "/retiro");
  }
}
