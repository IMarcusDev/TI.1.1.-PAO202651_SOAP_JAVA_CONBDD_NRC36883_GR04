package ec.edu.monster.controladores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
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
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

/**
 * Tests the transfer controller through doGet/doPost with Mockito mocks;
 * the SOAP model (MovimientoService) is injected as a mock.
 */
class TransferenciaControllerTest {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/transferencia.jsp";
  private static final String CTX = "/web_eurekabank_soap_java";

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
  }

  private void params(String paso, String origen, String destino, String importe) {
    when(request.getParameter("paso")).thenReturn(paso);
    when(request.getParameter("origen")).thenReturn(origen);
    when(request.getParameter("destino")).thenReturn(destino);
    when(request.getParameter("importe")).thenReturn(importe);
  }

  private static MovimientoResponseDTO respuesta(EstadosEnum estado, String mensaje, int id) {
    MovimientoResponseDTO dto = new MovimientoResponseDTO();
    dto.setEstado(estado);
    dto.setMensaje(mensaje);
    dto.setIdMovimiento(id);
    return dto;
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
    verify(request).setAttribute("importeFormateado", "$ 150.00");
    verify(dispatcher).forward(request, response);
    verifyNoInteractions(service);
  }

  @Test
  void confirmarOkCallsServiceOnceStoresReceiptAndRedirects() throws Exception {
    params("confirmar", "00100001", "00200002", "1250");
    when(service.transferencia("abc123", "00100001", "00200002", 1250.0))
        .thenReturn(respuesta(EstadosEnum.OK, "Transferencia registrada", 7));

    controller.doPost(request, response);

    verify(service, times(1)).transferencia("abc123", "00100001", "00200002", 1250.0);
    verify(service, never()).retiro(anyString(), anyString(), any(), anyDouble());
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> comprobante = ArgumentCaptor.forClass(Map.class);
    verify(session).setAttribute(eq("comprobanteTransferencia"), comprobante.capture());
    Map<String, String> c = comprobante.getValue();
    assertEquals("Transferencia", c.get("operacion"));
    assertEquals("00100001", c.get("origen"));
    assertEquals("00200002", c.get("destino"));
    assertEquals("$ 1,250.00", c.get("importe"));
    assertEquals("7", c.get("idMovimiento"));
    verify(response).sendRedirect(CTX + "/transferencia");
  }

  @Test
  void confirmarErrorShowsServerMessage() throws Exception {
    params("confirmar", "00100001", "00200002", "150");
    when(service.transferencia("abc123", "00100001", "00200002", 150.0))
        .thenReturn(respuesta(EstadosEnum.ERROR, "Cuenta destino no existe", 0));

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Cuenta destino no existe");
    verify(request).setAttribute("vista", "formulario");
    verify(request).setAttribute("origen", "00100001");
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
}
