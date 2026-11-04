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
 * Tests the transfer controller through doGet/doPost with Mockito mocks.
 * Empleado: any source account, its key is asked on confirmation.
 * Cliente: the source is always the account stored in session, no key (sent as "").
 */
class TransferenciaControllerTest {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/transferencia.jsp";
  private static final String CTX = "/web_eurekabank_soap_java";
  private static final String CLAVE_VACIA = "Ingrese la clave de la cuenta";

  private final MovimientoService service = mock(MovimientoService.class);
  private final TransferenciaController controller = new TransferenciaController(service);
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

  private void params(String paso, String origen, String destino, String importe) {
    when(request.getParameter("paso")).thenReturn(paso);
    when(request.getParameter("origen")).thenReturn(origen);
    when(request.getParameter("destino")).thenReturn(destino);
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

  private void assertKeyNotKept() {
    verify(request, never()).setAttribute(eq("clave"), any());
    verify(session, never()).setAttribute(eq("clave"), any());
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> capturedReceipt() {
    ArgumentCaptor<Map<String, String>> comprobante = ArgumentCaptor.forClass(Map.class);
    verify(session).setAttribute(eq("comprobanteTransferencia"), comprobante.capture());
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
  void getShowsEmptyFormWithoutCache() throws Exception {
    controller.doGet(request, response);

    verify(response).setHeader("Cache-Control", "no-store");
    verify(request).setAttribute("vista", "formulario");
    verify(request, never()).setAttribute(eq("origen"), any());
    verify(dispatcher).forward(request, response);
  }

  @ParameterizedTest
  @ValueSource(strings = {"NaN", "abc", "0", "1.234", "-5"})
  void revisarWithInvalidAmountShowsErrors(String importe) throws Exception {
    params("revisar", "00100001", "00200002", importe);

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> errores = ArgumentCaptor.forClass(Map.class);
    verify(request).setAttribute(eq("errores"), errores.capture());
    assertEquals("El importe debe ser mayor que cero y tener como máximo dos decimales",
        errores.getValue().get("importe"));
    verify(request).setAttribute("origen", "00100001");
    verify(request).setAttribute("destino", "00200002");
    verify(request).setAttribute("importe", importe);
    verifyNoInteractions(service);
  }

  @Test
  void sameAccountIsRejectedWithoutCallingService() throws Exception {
    params("confirmar", "00100001", " 00100001", "10");
    when(request.getParameter("clave")).thenReturn("123456");

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> errores = ArgumentCaptor.forClass(Map.class);
    verify(request).setAttribute(eq("errores"), errores.capture());
    assertEquals("La cuenta de origen y la de destino deben ser diferentes",
        errores.getValue().get("destino"));
    verifyNoInteractions(service);
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void revisarValidShowsConfirmationWithoutCallingService() throws Exception {
    params("revisar", "00100001", "00200002", "150");

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "confirmacion");
    verify(request).setAttribute("origen", "00100001");
    verify(request).setAttribute("destino", "00200002");
    verify(request).setAttribute("importe", "150");
    verify(request).setAttribute("importeFormateado", "$ 150.00");
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  void employeeConfirmWithoutKeyShowsConfirmationAgainWithoutCallingService(String clave)
      throws Exception {
    params("confirmar", "00100001", "00200002", "150");
    when(request.getParameter("clave")).thenReturn(clave);

    controller.doPost(request, response);

    verify(request).setAttribute("errores", Map.of("clave", CLAVE_VACIA));
    verify(request).setAttribute("vista", "confirmacion");
    verify(request).setAttribute("origen", "00100001");
    verify(request).setAttribute("destino", "00200002");
    verify(request).setAttribute("importe", "150");
    verify(request).setAttribute("importeFormateado", "$ 150.00");
    verify(dispatcher).forward(request, response);
    assertKeyNotKept();
    verifyNoInteractions(service);
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void employeeConfirmSendsTheKeyStoresReceiptAndRedirects() throws Exception {
    params("confirmar", "00100001", "00200002", "1250");
    when(request.getParameter("clave")).thenReturn("123456");
    when(service.registrarTransferencia("abc123", "00100001", "00200002", new BigDecimal("1250"), "123456"))
        .thenReturn(respuesta(EstadosEnum.OK, "Transferencia registrada", 7, "250.00"));

    controller.doPost(request, response);

    verify(service, times(1))
        .registrarTransferencia("abc123", "00100001", "00200002", new BigDecimal("1250"), "123456");
    Map<String, String> c = capturedReceipt();
    assertEquals("Transferencia", c.get("operacion"));
    assertEquals("00100001", c.get("origen"));
    assertEquals("00200002", c.get("destino"));
    assertEquals("$ 1,250.00", c.get("importe"));
    assertEquals("7", c.get("idMovimiento"));
    assertEquals("$ 250.00", c.get("nuevoSaldo"));
    assertEquals("Transferencia registrada", c.get("mensaje"));
    assertNotNull(c.get("fecha"));
    assertFalse(c.containsValue("123456"));
    assertKeyNotKept();
    verify(response).sendRedirect(CTX + "/transferencia");
    verify(dispatcher, never()).forward(any(ServletRequest.class), any(ServletResponse.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"La cuenta destino no existe", "Clave de cuenta incorrecta"})
  void confirmarErrorShowsServerMessage(String mensaje) throws Exception {
    params("confirmar", "00100001", "00200002", "150");
    when(request.getParameter("clave")).thenReturn("123456");
    when(service.registrarTransferencia("abc123", "00100001", "00200002", new BigDecimal("150"), "123456"))
        .thenReturn(respuesta(EstadosEnum.ERROR, mensaje, 0, null));

    controller.doPost(request, response);

    verify(request).setAttribute("error", mensaje);
    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("origen", "00100001");
    verify(request).setAttribute("destino", "00200002");
    verify(request).setAttribute("importe", "150");
    verify(session, never()).setAttribute(eq("comprobanteTransferencia"), any());
    assertKeyNotKept();
    verify(response, never()).sendRedirect(anyString());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void getAfterSuccessShowsReceiptAndClearsFlash() throws Exception {
    Map<String, String> comprobante = Map.of("operacion", "Transferencia");
    when(session.getAttribute("comprobanteTransferencia")).thenReturn(comprobante);

    controller.doGet(request, response);

    verify(request).setAttribute("comprobante", comprobante);
    verify(request).setAttribute("vista", "comprobante");
    verify(session).removeAttribute("comprobanteTransferencia");
    verify(dispatcher).forward(request, response);
  }

  @Test
  void clienteGetShowsFormWithOwnAccountAsSource() throws Exception {
    givenCliente();

    controller.doGet(request, response);

    verify(request).setAttribute("origen", "00100001");
    verify(request).setAttribute("vista", "formulario");
    verify(dispatcher).forward(request, response);
  }

  // A tampered "origen" parameter is ignored: the source always comes from the session
  @Test
  void clienteRevisarUsesOwnAccountAsSourceIgnoringTheForm() throws Exception {
    givenCliente();
    params("revisar", "00100002", "00200002", "50");

    controller.doPost(request, response);

    verify(request).setAttribute("vista", "confirmacion");
    verify(request).setAttribute("origen", "00100001");
    verify(request, never()).setAttribute("origen", "00100002");
    verifyNoInteractions(service);
  }

  @Test
  void clienteConfirmUsesOwnAccountAndAnEmptyKey() throws Exception {
    givenCliente();
    params("confirmar", "00100002", "00200002", "50");
    when(request.getParameter("clave")).thenReturn("999999");
    when(service.registrarTransferencia("abc123", "00100001", "00200002", new BigDecimal("50"), ""))
        .thenReturn(respuesta(EstadosEnum.OK, "Transferencia registrada", 8, "450.00"));

    controller.doPost(request, response);

    verify(service, times(1))
        .registrarTransferencia("abc123", "00100001", "00200002", new BigDecimal("50"), "");
    Map<String, String> c = capturedReceipt();
    assertEquals("00100001", c.get("origen"));
    assertEquals("00200002", c.get("destino"));
    assertKeyNotKept();
    verify(response).sendRedirect(CTX + "/transferencia");
  }

  @Test
  void clienteCannotTransferToOwnAccount() throws Exception {
    givenCliente();
    params("revisar", null, "00100001", "50");

    controller.doPost(request, response);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> errores = ArgumentCaptor.forClass(Map.class);
    verify(request).setAttribute(eq("errores"), errores.capture());
    assertEquals(Map.of("destino", "La cuenta de origen y la de destino deben ser diferentes"),
        errores.getValue());
    verify(request).setAttribute("origen", "00100001");
    verifyNoInteractions(service);
  }
}
