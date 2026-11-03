package ec.edu.monster.controladores;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.edu.monster.fakes.FakeLoginWebService;
import ec.edu.monster.fakes.FakeSoapServer;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.ws.Endpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the controller through its public doGet/doPost, with a fake browser
 * (Mockito mocks) and the fake SOAP server.
 */
class LoginControllerTest {
  private static final String VISTA = "/WEB-INF/ec/edu/monster/vista/login.jsp";

  private final LoginController controller = new LoginController();
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final RequestDispatcher dispatcher = mock(RequestDispatcher.class);
  private final HttpSession oldSession = mock(HttpSession.class);
  private final HttpSession newSession = mock(HttpSession.class);
  private Endpoint server;

  @BeforeEach
  void setUp() {
    server = FakeSoapServer.publish("LoginWebService", new FakeLoginWebService());
    when(request.getRequestDispatcher(VISTA)).thenReturn(dispatcher);
    when(request.getContextPath()).thenReturn("/web_eurekabank_soap_java");
    when(request.getSession()).thenReturn(oldSession);
    when(request.getSession(true)).thenReturn(newSession);
  }

  @AfterEach
  void tearDown() {
    server.stop();
  }

  @Test
  void getShowsLoginView() throws Exception {
    controller.doGet(request, response);

    verify(dispatcher).forward(request, response);
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void blankFieldsShowErrorWithoutCreatingSession() throws Exception {
    when(request.getParameter("username")).thenReturn("admin");
    when(request.getParameter("password")).thenReturn("  ");

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Ingrese usuario y contraseña");
    verify(request).setAttribute("username", "admin");
    verify(dispatcher).forward(request, response);
    verify(request, never()).getSession(anyBoolean());
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void wrongCredentialsShowServerMessageWithoutCreatingSession() throws Exception {
    when(request.getParameter("username")).thenReturn("admin");
    when(request.getParameter("password")).thenReturn("mala");

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Contraseña incorrecta");
    verify(request).setAttribute("username", "admin");
    verify(dispatcher).forward(request, response);
    verify(request, never()).getSession(anyBoolean());
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void validCredentialsStoreUserInNewSessionAndRedirectToMenu() throws Exception {
    when(request.getParameter("username")).thenReturn(" admin ");
    when(request.getParameter("password")).thenReturn("1234");

    controller.doPost(request, response);

    // Old session discarded, data saved in the new one
    verify(oldSession).invalidate();
    verify(newSession).setAttribute("token", FakeLoginWebService.TOKEN);
    verify(newSession).setAttribute("username", "admin");
    verify(newSession).setAttribute("nombre", "Administrador");
    verify(newSession).setAttribute("tipoUsuario", "ADMIN");
    // Post/Redirect/Get: redirect, never forward to the login view
    verify(response).sendRedirect("/web_eurekabank_soap_java/menu");
    verify(dispatcher, never()).forward(request, response);
  }
}
