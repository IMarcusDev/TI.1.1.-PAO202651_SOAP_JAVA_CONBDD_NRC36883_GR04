package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.Test;

/**
 * Role logins (loginEmpleado / loginCliente) of LoginServicioSoap against a fake SOAP server.
 */
class LoginRolesSoapTest {

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
    void employeeLoginReturnsTheEmployeeSession() throws ServicioException {
        DatosSesion datos = servicio.iniciarSesionEmpleado("cajero", "cajero123");

        assertEquals("Carla Andrade", datos.nombre());
        assertEquals("Empleado", datos.tipoUsuario());
        assertEquals(FakeLoginWebService.TOKEN, datos.token());
    }

    @Test
    void clienteLoginReturnsTheClienteSession() throws ServicioException {
        DatosSesion datos = servicio.iniciarSesionCliente("00100001", "123456");

        assertEquals("Ana Torres", datos.nombre());
        assertEquals("Cliente", datos.tipoUsuario());
        assertEquals(FakeLoginWebService.TOKEN_CLIENTE, datos.token());
    }

    @Test
    void wrongEmployeeCredentialsThrowTheServerMessage() {
        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.iniciarSesionEmpleado("cajero", "mala"));

        assertEquals("Credenciales incorrectas", error.getMessage());
    }

    @Test
    void inactiveAccountThrowsTheServerMessage() {
        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.iniciarSesionCliente("00100003", "123456"));

        assertEquals("La cuenta no se encuentra activa", error.getMessage());
    }

    @Test
    void serverDownThrowsTheConnectionMessage() {
        servidor.stop();
        servidor = null;

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicio.iniciarSesionCliente("00100001", "123456"));

        assertTrue(error.getMessage().startsWith("No se pudo conectar con el servidor: "));
    }
}
