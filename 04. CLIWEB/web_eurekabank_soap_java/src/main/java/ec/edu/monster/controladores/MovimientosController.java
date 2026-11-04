package ec.edu.monster.controladores;

import ec.edu.monster.modelos.dto.HistorialCuenta;
import ec.edu.monster.modelos.servicios.MovimientoService;
import ec.edu.monster.ws.movimiento.EstadosEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Controller of the "Consultar movimientos" screen. Read-only, so it only answers GET.
 * Empleado: GET /movimientos shows the search form; GET /movimientos?cuenta=X queries X.
 * Cliente: always queries the account stored in session at login; the parameter is ignored.
 * Server errors are shown as the server sends them.
 */
@WebServlet(name = "MovimientosController", urlPatterns = {"/movimientos"})
public class MovimientosController extends HttpServlet {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/movimientos.jsp";
  private static final String RUTA_LOGIN = "/login";
  private static final String CUENTA_VACIA = "Ingrese el número de cuenta";

  private final transient MovimientoService movimientoService;

  public MovimientosController() {
    this(new MovimientoService());
  }

  // Used by the tests to inject a mock model
  MovimientosController(MovimientoService movimientoService) {
    this.movimientoService = movimientoService;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    String token = token(session);
    if (token == null) {
      response.sendRedirect(request.getContextPath() + RUTA_LOGIN);
      return;
    }
    response.setHeader("Cache-Control", "no-store");

    // A Cliente can only see their own account: the parameter is ignored
    String cuentaCliente = cuentaCliente(session);
    String cuenta = cuentaCliente != null ? cuentaCliente : request.getParameter("cuenta");

    // First visit of an employee: only the search form
    if (cuenta == null) {
      render(request, response);
      return;
    }

    cuenta = cuenta.strip();
    request.setAttribute("cuenta", cuenta);
    if (cuenta.isEmpty()) {
      request.setAttribute("errores", Map.of("cuenta", CUENTA_VACIA));
      render(request, response);
      return;
    }

    HistorialCuenta historial = movimientoService.consultarMovimientos(token, cuenta);
    if (historial == null || historial.estado() != EstadosEnum.OK) {
      request.setAttribute("error", historial == null
          ? "No se pudieron consultar los movimientos" : historial.mensaje());
    } else {
      request.setAttribute("historial", historial);
    }
    render(request, response);
  }

  private static void render(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.getRequestDispatcher(VISTA).forward(request, response);
  }

  // Session token, or null when there is no logged-in user
  private static String token(HttpSession session) {
    if (session == null) {
      return null;
    }
    Object token = session.getAttribute("token");
    return token instanceof String value && !value.isBlank() ? value : null;
  }

  // Account stored at login for a Cliente, or null for an employee
  private static String cuentaCliente(HttpSession session) {
    Object cuenta = session.getAttribute("cuenta");
    return cuenta instanceof String value && !value.isBlank() ? value : null;
  }
}
