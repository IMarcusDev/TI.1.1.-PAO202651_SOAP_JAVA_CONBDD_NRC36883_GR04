package ec.edu.monster.controladores;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the logout controller through its public doPost, with a fake browser
 * (Mockito mocks).
 */
class LogoutControllerTest {
  private final LogoutController controller = new LogoutController();
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final HttpSession session = mock(HttpSession.class);

  @BeforeEach
  void setUp() {
    when(request.getContextPath()).thenReturn("/web_eurekabank_soap_java");
  }

  @Test
  void withSessionInvalidatesItAndRedirectsToLogin() throws Exception {
    when(request.getSession(false)).thenReturn(session);

    controller.doPost(request, response);

    verify(session).invalidate();
    verify(response).sendRedirect("/web_eurekabank_soap_java/login");
  }

  @Test
  void withoutSessionRedirectsWithoutCreatingOne() throws Exception {
    when(request.getSession(false)).thenReturn(null);

    controller.doPost(request, response);

    verify(request, never()).getSession();
    verify(request, never()).getSession(true);
    verify(response).sendRedirect("/web_eurekabank_soap_java/login");
  }
}
