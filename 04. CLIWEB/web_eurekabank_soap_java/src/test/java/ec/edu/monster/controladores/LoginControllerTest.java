package ec.edu.monster.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests the controller through its public doGet/doPost, with a fake browser
 * (Mockito mocks) and the fake SOAP server.
 * The user picks the role: "empleado" -> loginEmpleado, "cliente" -> loginCliente.
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

  private void givenForm(String tipo, String username, String password) {
    when(request.getParameter("tipo")).thenReturn(tipo);
    when(request.getParameter("username")).thenReturn(username);
    when(request.getParameter("password")).thenReturn(password);
  }

  // Back on the login view: no session created, no redirect
  private void assertLoginViewWithoutSession() throws Exception {
    verify(dispatcher).forward(request, response);
    verify(request, never()).getSession(anyBoolean());
    verify(oldSession, never()).invalidate();
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void getShowsLoginViewWithEmpleadoSelected() throws Exception {
    controller.doGet(request, response);

    verify(request).setAttribute("tipo", "empleado");
    verify(dispatcher).forward(request, response);
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void blankEmployeeFieldsShowErrorWithoutCreatingSession() throws Exception {
    givenForm("empleado", "cajero", "  ");

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Ingrese usuario y contraseña");
    verify(request).setAttribute("username", "cajero");
    verify(request).setAttribute("tipo", "empleado");
    assertLoginViewWithoutSession();
  }

  @Test
  void blankClientFieldsShowErrorWithoutCreatingSession() throws Exception {
    givenForm("cliente", " ", "123456");

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Ingrese número de cuenta y clave");
    verify(request).setAttribute("username", " ");
    verify(request).setAttribute("tipo", "cliente");
    assertLoginViewWithoutSession();
  }

  @Test
  void wrongEmployeeCredentialsShowServerMessageWithoutCreatingSession() throws Exception {
    givenForm("empleado", "cajero", "mala");

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Credenciales incorrectas");
    verify(request).setAttribute("username", "cajero");
    verify(request).setAttribute("tipo", "empleado");
    assertLoginViewWithoutSession();
  }

  @Test
  void wrongClientCredentialsShowServerMessageWithoutCreatingSession() throws Exception {
    givenForm("cliente", "00100001", "000000");

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Credenciales incorrectas");
    verify(request).setAttribute("username", "00100001");
    verify(request).setAttribute("tipo", "cliente");
    assertLoginViewWithoutSession();
  }

  @Test
  void inactiveClientAccountShowsServerMessage() throws Exception {
    givenForm("cliente", "00100003", "111111");

    controller.doPost(request, response);

    verify(request).setAttribute("error", "La cuenta no se encuentra activa");
    verify(request).setAttribute("tipo", "cliente");
    assertLoginViewWithoutSession();
  }

  // The client account is only accepted by loginCliente, never by loginEmpleado
  @Test
  void clientCredentialsWithEmpleadoSelectedAreRejected() throws Exception {
    givenForm("empleado", "00100001", "123456");

    controller.doPost(request, response);

    verify(request).setAttribute("error", "Credenciales incorrectas");
    verify(request).setAttribute("tipo", "empleado");
    assertLoginViewWithoutSession();
  }

  @Test
  void validEmployeeStoresUserInNewSessionWithoutAccount() throws Exception {
    givenForm("empleado", " cajero ", "cajero123");

    controller.doPost(request, response);

    // Old session discarded, data saved in the new one
    verify(oldSession).invalidate();
    verify(newSession).setAttribute("token", FakeLoginWebService.TOKEN);
    verify(newSession).setAttribute("username", "cajero");
    verify(newSession).setAttribute("nombre", "Carla Andrade");
    verify(newSession).setAttribute("tipoUsuario", "Empleado");
    verify(newSession, never()).setAttribute(eq("cuenta"), any());
    // Post/Redirect/Get: redirect, never forward to the login view
    verify(response).sendRedirect("/web_eurekabank_soap_java/menu");
    verify(dispatcher, never()).forward(request, response);
  }

  @Test
  void validClientStoresUserAndOwnAccountInNewSession() throws Exception {
    givenForm("cliente", " 00100001 ", "123456");

    controller.doPost(request, response);

    verify(oldSession).invalidate();
    verify(newSession).setAttribute("token", FakeLoginWebService.TOKEN_CLIENTE);
    verify(newSession).setAttribute("username", "00100001");
    verify(newSession).setAttribute("nombre", "Ana Torres");
    verify(newSession).setAttribute("tipoUsuario", "Cliente");
    // The server ties the token to the account but does not return it: keep the typed one
    verify(newSession).setAttribute("cuenta", "00100001");
    verify(response).sendRedirect("/web_eurekabank_soap_java/menu");
    verify(dispatcher, never()).forward(request, response);
  }

  @Test
  void roleIsCaseInsensitive() throws Exception {
    givenForm("CLIENTE", "00100001", "123456");

    controller.doPost(request, response);

    verify(newSession).setAttribute("tipoUsuario", "Cliente");
    verify(newSession).setAttribute("cuenta", "00100001");
    verify(response).sendRedirect("/web_eurekabank_soap_java/menu");
  }

  // Missing or unknown role: the form default (Empleado) is used
  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "admin"})
  void missingOrUnknownRoleLogsInAsEmployee(String tipo) throws Exception {
    givenForm(tipo, "cajero", "cajero123");

    controller.doPost(request, response);

    verify(newSession).setAttribute("tipoUsuario", "Empleado");
    verify(newSession, never()).setAttribute(eq("cuenta"), any());
    verify(response).sendRedirect("/web_eurekabank_soap_java/menu");
  }
}
