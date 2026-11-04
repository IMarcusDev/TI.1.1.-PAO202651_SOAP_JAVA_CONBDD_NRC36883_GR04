package ec.edu.monster.controladores;

import ec.edu.monster.modelos.servicios.LoginService;
import ec.edu.monster.ws.login.EstadosEnum;
import ec.edu.monster.ws.login.LoginResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller of the login screen.
 * GET  /login -> shows the form (Empleado selected)
 * POST /login -> validates the credentials against the SOAP server
 * The user picks the role: tipo=empleado -> loginEmpleado(usuario, clave),
 * tipo=cliente -> loginCliente(cuenta, clave). Anything else counts as empleado.
 */
@WebServlet(name = "LoginController", urlPatterns = {"/login"})
public class LoginController extends HttpServlet {
  // JSP inside WEB-INF: only reachable through this controller
  private static final String VISTA_LOGIN = "/WEB-INF/ec/edu/monster/vista/login.jsp";

  // Page shown after a successful login
  private static final String RUTA_MENU = "/menu";

  // Values of the "tipo" radio buttons in the JSP
  private static final String TIPO_EMPLEADO = "empleado";
  private static final String TIPO_CLIENTE = "cliente";

  // Model: the only class that talks to the SOAP LoginWebService
  private final LoginService loginService = new LoginService();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setAttribute("tipo", TIPO_EMPLEADO);
    request.getRequestDispatcher(VISTA_LOGIN).forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    boolean esCliente = TIPO_CLIENTE.equalsIgnoreCase(request.getParameter("tipo"));
    String tipo = esCliente ? TIPO_CLIENTE : TIPO_EMPLEADO;

    // Read the form fields (name="username" / name="password" in the JSP);
    // for a Cliente, username holds the account number
    String username = request.getParameter("username");
    String password = request.getParameter("password");

    // Avoid a SOAP call when the form is incomplete
    if (isBlank(username) || isBlank(password)) {
      showError(request, response, esCliente
          ? "Ingrese número de cuenta y clave" : "Ingrese usuario y contraseña", username, tipo);
      return;
    }

    String identificador = username.trim();
    LoginResponseDTO resultado = esCliente
        ? loginService.loginCliente(identificador, password)
        : loginService.loginEmpleado(identificador, password);

    if (resultado.getEstado() != EstadosEnum.OK) {
      // Wrong credentials, inactive account or connection problem: message comes from the model
      showError(request, response, resultado.getMensaje(), username, tipo);
      return;
    }

    // New session id after login, so an old (possibly stolen) id is not reused
    request.getSession().invalidate();
    HttpSession session = request.getSession(true);

    // Only plain Strings in the session; the generated DTO is not Serializable
    session.setAttribute("token", resultado.getToken());
    session.setAttribute("username", identificador);
    session.setAttribute("nombre", resultado.getNombre());
    session.setAttribute("tipoUsuario", resultado.getTipoUsuario());
    if (esCliente) {
      // The server ties the token to this account but does not return it:
      // operations of a Cliente always use this value, never one from the form
      session.setAttribute("cuenta", identificador);
    }

    // Redirect (not forward): a browser refresh will not resend the password
    response.sendRedirect(request.getContextPath() + RUTA_MENU);
  }

  // Back to the form with an error message; the username and role are kept, the password is not
  private void showError(HttpServletRequest request, HttpServletResponse response,
      String mensaje, String username, String tipo) throws ServletException, IOException {
    request.setAttribute("error", mensaje);
    request.setAttribute("username", username);
    request.setAttribute("tipo", tipo);
    request.getRequestDispatcher(VISTA_LOGIN).forward(request, response);
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
