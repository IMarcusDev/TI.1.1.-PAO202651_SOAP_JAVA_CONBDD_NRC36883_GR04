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
 * GET  /login -> shows the form
 * POST /login -> validates the credentials against the SOAP server
 */
@WebServlet(name = "LoginController", urlPatterns = {"/login"})
public class LoginController extends HttpServlet {
  // JSP inside WEB-INF: only reachable through this controller
  private static final String VISTA_LOGIN = "/WEB-INF/ec/edu/monster/vista/login.jsp";

  // Page shown after a successful login
  private static final String RUTA_MENU = "/menu";

  // Model: the only class that talks to the SOAP LoginWebService
  private final LoginService loginService = new LoginService();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.getRequestDispatcher(VISTA_LOGIN).forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Read the form fields (name="username" / name="password" in the JSP)
    String username = request.getParameter("username");
    String password = request.getParameter("password");

    // Avoid a SOAP call when the form is incomplete
    if (isBlank(username) || isBlank(password)) {
      showError(request, response, "Ingrese usuario y contraseña", username);
      return;
    }

    LoginResponseDTO resultado = loginService.login(username.trim(), password);

    if (resultado.getEstado() != EstadosEnum.OK) {
      // Wrong credentials or connection problem: message comes from the model
      showError(request, response, resultado.getMensaje(), username);
      return;
    }

    // New session id after login, so an old (possibly stolen) id is not reused
    request.getSession().invalidate();
    HttpSession session = request.getSession(true);

    // Only plain Strings in the session; the generated DTO is not Serializable
    session.setAttribute("token", resultado.getToken());
    session.setAttribute("username", username.trim());
    session.setAttribute("nombre", resultado.getNombre());
    session.setAttribute("tipoUsuario", resultado.getTipoUsuario());

    // Redirect (not forward): a browser refresh will not resend the password
    response.sendRedirect(request.getContextPath() + RUTA_MENU);
  }

  // Back to the form with an error message; the username is kept, the password is not
  private void showError(HttpServletRequest request, HttpServletResponse response,
      String mensaje, String username) throws ServletException, IOException {
    request.setAttribute("error", mensaje);
    request.setAttribute("username", username);
    request.getRequestDispatcher(VISTA_LOGIN).forward(request, response);
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
