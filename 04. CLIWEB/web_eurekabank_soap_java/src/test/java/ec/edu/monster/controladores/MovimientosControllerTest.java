package ec.edu.monster.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.edu.monster.modelos.dto.HistorialCuenta;
import ec.edu.monster.modelos.servicios.MovimientoService;
import ec.edu.monster.ws.movimiento.EstadosEnum;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the "Consultar movimientos" controller through doGet with Mockito mocks;
 * the SOAP model (MovimientoService) is injected as a mock.
 * Empleado: types any account. Cliente: only the account stored in session.
 */
class MovimientosControllerTest {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/movimientos.jsp";
  private static final String CTX = "/web_eurekabank_soap_java";

  private final MovimientoService service = mock(MovimientoService.class);
  private final MovimientosController controller = new MovimientosController(service);
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

  private static HistorialCuenta historial() {
    return new HistorialCuenta(EstadosEnum.OK, "Consulta exitosa",
        new HistorialCuenta.CuentaConsultada(
            "00100001", "Juan Pérez", "Dólares", new BigDecimal("1250.00"), "ACTIVO"),
        List.of());
  }

  private void assertNoQuery() {
    verify(service, never()).consultarMovimientos(anyString(), anyString());
    verify(request, never()).setAttribute(eq("historial"), any());
  }

  @Test
  void withoutSessionGoesToLogin() throws Exception {
    when(request.getSession(false)).thenReturn(null);

    controller.doGet(request, response);

    verify(response).sendRedirect(CTX + "/login");
    verify(dispatcher, never()).forward(request, response);
    assertNoQuery();
  }

  @Test
  void blankTokenGoesToLogin() throws Exception {
    when(session.getAttribute("token")).thenReturn(" ");

    controller.doGet(request, response);

    verify(response).sendRedirect(CTX + "/login");
    verify(dispatcher, never()).forward(request, response);
    assertNoQuery();
  }

  @Test
  void empleadoFirstVisitShowsOnlyTheForm() throws Exception {
    controller.doGet(request, response);

    verify(response).setHeader("Cache-Control", "no-store");
    verify(dispatcher).forward(request, response);
    verify(request, never()).setAttribute(eq("cuenta"), any());
    verify(request, never()).setAttribute(eq("errores"), any());
    verify(request, never()).setAttribute(eq("error"), any());
    assertNoQuery();
  }

  @Test
  void empleadoBlankAccountIsRejectedWithoutCallingTheServer() throws Exception {
    when(request.getParameter("cuenta")).thenReturn("   ");

    controller.doGet(request, response);

    verify(request).setAttribute("cuenta", "");
    verify(request).setAttribute("errores", Map.of("cuenta", "Ingrese el número de cuenta"));
    verify(request, never()).setAttribute(eq("error"), any());
    verify(dispatcher).forward(request, response);
    assertNoQuery();
  }

  @Test
  void empleadoQueriesTheTypedAccount() throws Exception {
    HistorialCuenta historial = historial();
    when(request.getParameter("cuenta")).thenReturn(" 00100001 ");
    when(service.consultarMovimientos("abc123", "00100001")).thenReturn(historial);

    controller.doGet(request, response);

    verify(service).consultarMovimientos("abc123", "00100001");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("historial", historial);
    verify(request, never()).setAttribute(eq("error"), any());
    verify(response).setHeader("Cache-Control", "no-store");
    verify(dispatcher).forward(request, response);
  }

  @Test
  void serverErrorIsShownAsIs() throws Exception {
    when(request.getParameter("cuenta")).thenReturn("99999999");
    when(service.consultarMovimientos("abc123", "99999999"))
        .thenReturn(HistorialCuenta.error(EstadosEnum.ERROR, "La cuenta no existe"));

    controller.doGet(request, response);

    verify(request).setAttribute("cuenta", "99999999");
    verify(request).setAttribute("error", "La cuenta no existe");
    verify(request, never()).setAttribute(eq("historial"), any());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void nullAnswerShowsAGenericError() throws Exception {
    when(request.getParameter("cuenta")).thenReturn("00100001");
    when(service.consultarMovimientos("abc123", "00100001")).thenReturn(null);

    controller.doGet(request, response);

    verify(request).setAttribute("error", "No se pudieron consultar los movimientos");
    verify(request, never()).setAttribute(eq("historial"), any());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void clienteAlwaysQueriesTheOwnAccount() throws Exception {
    givenCliente();
    HistorialCuenta historial = historial();
    when(request.getParameter("cuenta")).thenReturn("00200002");
    when(service.consultarMovimientos("abc123", "00100001")).thenReturn(historial);

    controller.doGet(request, response);

    verify(service).consultarMovimientos("abc123", "00100001");
    verify(service, never()).consultarMovimientos("abc123", "00200002");
    verify(request).setAttribute("cuenta", "00100001");
    verify(request).setAttribute("historial", historial);
    verify(dispatcher).forward(request, response);
  }

  @Test
  void clienteFirstVisitQueriesWithoutAParameter() throws Exception {
    givenCliente();
    HistorialCuenta historial = historial();
    when(service.consultarMovimientos("abc123", "00100001")).thenReturn(historial);

    controller.doGet(request, response);

    verify(request).setAttribute("historial", historial);
    verify(dispatcher).forward(request, response);
  }
}
