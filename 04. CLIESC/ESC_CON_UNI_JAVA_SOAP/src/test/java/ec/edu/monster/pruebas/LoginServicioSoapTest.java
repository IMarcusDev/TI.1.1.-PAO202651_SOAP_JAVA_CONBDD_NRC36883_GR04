package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ec.edu.monster.dobles.ConfiguracionDePrueba;
import ec.edu.monster.dobles.FakeLoginWebService;
import ec.edu.monster.dobles.FakeSoapServer;
import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.LoginServicioSoap;
import ec.edu.monster.modelo.ServicioException;
import jakarta.xml.ws.Endpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Exercises the real SOAP round trip (Java -> XML -> HTTP -> XML -> Java) against
 * a fake service published on a free local port.
 */
class LoginServicioSoapTest {

    @Nested
    class WithServer {

        private Endpoint servidor;
        private LoginServicioSoap servicio;

        @BeforeEach
        void iniciar() {
            Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
            servidor = FakeSoapServer.publicar(configuracion, "LoginWebService", new FakeLoginWebService());
            servicio = new LoginServicioSoap(configuracion);
        }

        @AfterEach
        void detener() {
            if (servidor != null) {
                servidor.stop();
            }
        }

        @Test
        void validCredentialsReturnTheSessionData() throws ServicioException {
            DatosSesion datos = servicio.iniciarSesion("admin", "1234");

            assertEquals("Administrador", datos.nombre());
            assertEquals("Empleado", datos.tipoUsuario());
            assertEquals(FakeLoginWebService.TOKEN, datos.token());
        }

        @Test
        void wrongCredentialsThrowTheServerMessage() {
            ServicioException error = assertThrows(ServicioException.class,
                    () -> servicio.iniciarSesion("admin", "mala"));

            assertEquals("Contraseña incorrecta", error.getMessage());
        }
    }

    /** Nothing is listening at the configured address. */
    @Test
    void serverDownThrowsTheConnectionMessage() {
        Configuracion configuracion = ConfiguracionDePrueba.paraPuerto(ConfiguracionDePrueba.puertoLibre());
        LoginServicioSoap servicio = new LoginServicioSoap(configuracion);

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.iniciarSesion("admin", "1234"));

        assertEquals("No se pudo conectar con el servidor: " + configuracion.getEndpoint("LoginWebService"),
                error.getMessage());
    }
}
