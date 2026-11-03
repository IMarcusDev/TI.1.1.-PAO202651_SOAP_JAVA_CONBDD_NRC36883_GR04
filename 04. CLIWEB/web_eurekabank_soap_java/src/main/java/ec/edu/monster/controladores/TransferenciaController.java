package ec.edu.monster.controladores;

import ec.edu.monster.modelos.servicios.MovimientoService;
import ec.edu.monster.ws.movimiento.EstadosEnum;
import ec.edu.monster.ws.movimiento.MovimientoResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller of the transfer screen.
 * GET  /transferencia                 -> empty form, or the receipt after a successful transfer
 * POST /transferencia paso=revisar    -> validates and shows the confirmation
 * POST /transferencia paso=editar     -> back to the form keeping the values
 * POST /transferencia paso=confirmar  -> re-validates, calls the SOAP service and redirects (PRG)
 */
@WebServlet(name = "TransferenciaController", urlPatterns = {"/transferencia"})
public class TransferenciaController extends HttpServlet {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/transferencia.jsp";
  private static final String RUTA = "/transferencia";
  private static final String RUTA_LOGIN = "/login";
  // Session flash key: the receipt survives exactly one redirect
  private static final String FLASH = "comprobanteTransferencia";
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final transient MovimientoService movimientoService;

  public TransferenciaController() {
    this(new MovimientoService());
  }

  // Used by the tests to inject a mock model
  TransferenciaController(MovimientoService movimientoService) {
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

    Object comprobante = session.getAttribute(FLASH);
    if (comprobante != null) {
      session.removeAttribute(FLASH);
      request.setAttribute("comprobante", comprobante);
      render(request, response, "comprobante");
      return;
    }
    render(request, response, "formulario");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    String token = token(session);
    if (token == null) {
      response.sendRedirect(request.getContextPath() + RUTA_LOGIN);
      return;
    }
    response.setHeader("Cache-Control", "no-store");

    String paso = request.getParameter("paso");
    String origen = request.getParameter("origen");
    String destino = request.getParameter("destino");
    String importe = request.getParameter("importe");

    if ("editar".equals(paso)) {
      showForm(request, response, origen, destino, importe);
      return;
    }

    // Always validated, also on confirm: hidden fields can be tampered with
    MovimientoForm form = MovimientoForm.transferencia(origen, destino, importe);
    if (!form.isValido()) {
      request.setAttribute("errores", form.getErrores());
      showForm(request, response, origen, destino, importe);
      return;
    }

    if (!"confirmar".equals(paso)) {
      request.setAttribute("origen", form.getOrigen());
      request.setAttribute("destino", form.getDestino());
      request.setAttribute("importe", form.getImporte().toPlainString());
      request.setAttribute("importeFormateado", form.getImporteFormateado());
      render(request, response, "confirmacion");
      return;
    }

    MovimientoResponseDTO resultado = movimientoService.transferencia(
        token, form.getOrigen(), form.getDestino(), form.getImporte().doubleValue());

    if (resultado == null || resultado.getEstado() != EstadosEnum.OK) {
      request.setAttribute("error", resultado == null
          ? "No se pudo registrar la transferencia" : resultado.getMensaje());
      showForm(request, response, origen, destino, importe);
      return;
    }

    // Only Strings in the session (the generated DTO is not Serializable)
    Map<String, String> comprobante = new LinkedHashMap<>();
    comprobante.put("operacion", "Transferencia");
    comprobante.put("origen", form.getOrigen());
    comprobante.put("destino", form.getDestino());
    comprobante.put("importe", form.getImporteFormateado());
    comprobante.put("idMovimiento", String.valueOf(resultado.getIdMovimiento()));
    comprobante.put("fecha", LocalDateTime.now().format(FECHA));
    comprobante.put("mensaje", resultado.getMensaje());
    session.setAttribute(FLASH, comprobante);

    // Post/Redirect/Get: a browser refresh never repeats the transfer
    response.sendRedirect(request.getContextPath() + RUTA);
  }

  // Form state keeping what the user typed
  private void showForm(HttpServletRequest request, HttpServletResponse response,
      String origen, String destino, String importe) throws ServletException, IOException {
    request.setAttribute("origen", origen);
    request.setAttribute("destino", destino);
    request.setAttribute("importe", importe);
    render(request, response, "formulario");
  }

  private void render(HttpServletRequest request, HttpServletResponse response, String vista)
      throws ServletException, IOException {
    request.setAttribute("vista", vista);
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
}
