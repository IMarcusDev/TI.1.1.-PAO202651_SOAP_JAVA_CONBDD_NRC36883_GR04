package ec.edu.monster.pruebas.repositorios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.enums.Perfil;
import ec.edu.monster.modelos.repositorios.LoginSoapRepositorio;
import ec.edu.monster.modelos.repositorios.SoapErrores;
import ec.edu.monster.pruebas.apoyo.TransporteFalso;
import java.net.SocketTimeoutException;
import org.junit.Test;

public class LoginSoapRepositorioTest {

    private static final String URL_BASE = "http://servidor:8080/WS_CON_UNI_JAVA_SOAP/";

    private static LoginSoapRepositorio repositorio(TransporteFalso transporte) {
        return new LoginSoapRepositorio(URL_BASE, transporte, () -> false);
    }

    @Test
    public void loginEmpleado_ok_devuelveTokenPerfilYNombre() {
        TransporteFalso transporte = TransporteFalso.responde(TransporteFalso.respuesta("loginEmpleado",
                "<estado>OK</estado><mensaje>Ingreso de sesión exitoso</mensaje><token>abc-123</token>"
                + "<perfil>EMPLEADO</perfil><nombre>Marcos Escobar</nombre>"));

        Resultado<Sesion> resultado = repositorio(transporte).loginEmpleado("mescobar", "secreta");

        assertTrue(resultado.isExito());
        assertEquals("abc-123", resultado.getDatos().getToken());
        assertEquals(Perfil.EMPLEADO, resultado.getDatos().getPerfil());
        assertEquals("Marcos Escobar", resultado.getDatos().getNombre());
        assertEquals(URL_BASE + "LoginWebService", transporte.ultimaUrl());
        assertTrue(transporte.ultimoSobre().contains(
                "<ws:loginEmpleado><usuario>mescobar</usuario><clave>secreta</clave></ws:loginEmpleado>"));
    }

    @Test
    public void loginCliente_guardaLaCuentaDeLaSesion() {
        TransporteFalso transporte = TransporteFalso.responde(TransporteFalso.respuesta("loginCliente",
                "<estado>OK</estado><mensaje>ok</mensaje><token>t</token><perfil>CLIENTE</perfil><nombre>Ana</nombre>"));

        Resultado<Sesion> resultado = repositorio(transporte).loginCliente("00100001", "123456");

        assertEquals(Perfil.CLIENTE, resultado.getDatos().getPerfil());
        assertEquals("00100001", resultado.getDatos().getCuenta());
        assertTrue(transporte.ultimoSobre().contains("<cuenta>00100001</cuenta><clave>123456</clave>"));
    }

    @Test
    public void login_credencialesIncorrectas_muestraElMensajeDelServidor() {
        TransporteFalso transporte = TransporteFalso.responde(TransporteFalso.respuesta("loginCliente",
                "<estado>ERROR</estado><mensaje>La cuenta no se encuentra activa</mensaje>"));

        Resultado<Sesion> resultado = repositorio(transporte).loginCliente("00100001", "123456");

        assertFalse(resultado.isExito());
        assertEquals("La cuenta no se encuentra activa", resultado.getMensaje());
    }

    @Test
    public void login_okSinToken_esRespuestaIncompleta() {
        TransporteFalso transporte = TransporteFalso.responde(TransporteFalso.respuesta("loginEmpleado",
                "<estado>OK</estado><mensaje>ok</mensaje><token></token>"));

        assertEquals(SoapErrores.RESPUESTA_INCOMPLETA, repositorio(transporte).loginEmpleado("u", "c").getMensaje());
    }

    @Test
    public void login_sinRespuestaEnQuinceSegundos_informaQueNoHayServidor() {
        TransporteFalso transporte = TransporteFalso.lanza(new SocketTimeoutException("timeout"));

        assertTrue(repositorio(transporte).loginEmpleado("u", "c").getMensaje().startsWith(SoapErrores.SIN_SERVIDOR));
    }

    @Test
    public void login_sinRed_noLlamaAlServidor() {
        TransporteFalso transporte = TransporteFalso.responde("");
        LoginSoapRepositorio repositorio = new LoginSoapRepositorio(URL_BASE, transporte, () -> true);

        assertEquals(SoapErrores.SIN_CONEXION, repositorio.loginEmpleado("u", "c").getMensaje());
        assertEquals(0, transporte.peticiones());
    }
}
