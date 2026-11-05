package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.Sesion;
import org.junit.jupiter.api.Test;

/**
 * Cliente role of the session: the account used to log in is the only one they can use.
 */
class SesionRolesTest {

    @Test
    void clienteKeepsTheAccountUsedToLogIn() {
        Sesion sesion = new Sesion();
        sesion.iniciar("00100001", new DatosSesion("Ana Torres", "Cliente", "t"));

        assertTrue(sesion.esCliente());
        assertFalse(sesion.esEmpleado());
        assertEquals("00100001", sesion.getCuenta());
    }

    @Test
    void employeeHasNoFixedAccount() {
        Sesion sesion = new Sesion();
        sesion.iniciar("cajero", new DatosSesion("Carla Andrade", "Empleado", "t"));

        assertTrue(sesion.esEmpleado());
        assertFalse(sesion.esCliente());
        assertNull(sesion.getCuenta());
    }

    @Test
    void closingTheSessionForgetsTheAccount() {
        Sesion sesion = new Sesion();
        sesion.iniciar("00100001", new DatosSesion("Ana Torres", "Cliente", "t"));

        sesion.cerrar();

        assertFalse(sesion.esCliente());
        assertNull(sesion.getCuenta());
    }

    @Test
    void roleMustMatchExactly() {
        Sesion sesion = new Sesion();
        sesion.iniciar("00100001", new DatosSesion("Ana Torres", "cliente", "t"));

        assertFalse(sesion.esCliente());
        assertNull(sesion.getCuenta());
    }
}
