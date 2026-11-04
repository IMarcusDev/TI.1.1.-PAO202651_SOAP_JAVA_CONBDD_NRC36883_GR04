package ec.edu.monster;

import ec.edu.monster.controlador.EstadoControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.controlador.MovimientoControlador;
import ec.edu.monster.modelo.Comprobantes;
import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.EstadoServidor;
import ec.edu.monster.modelo.LoginServicio;
import ec.edu.monster.modelo.LoginServicioSoap;
import ec.edu.monster.modelo.MovimientoServicio;
import ec.edu.monster.modelo.MovimientoServicioSoap;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.LoginFrame;
import ec.edu.monster.vista.PrincipalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Composition root: builds the model once and wires each window with its
 * controllers, handling navigation between login and the main window.
 */
public final class App {

    private static final String TITULO = "EurekaBank";
    private static final String SIN_CONEXION = "No se pudo conectar con el servidor";

    private final Configuracion configuracion;
    private final Sesion sesion = new Sesion();
    private final EstadoServidor estado;

    private App(Configuracion configuracion) {
        this.configuracion = configuracion;
        this.estado = new EstadoServidor(configuracion);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::iniciar);
    }

    private static void iniciar() {
        Configuracion configuracion;
        try {
            configuracion = Configuracion.cargar();
        } catch (RuntimeException e) {
            error(e.getMessage());
            return;
        }
        new App(configuracion).mostrarLogin();
    }

    private void mostrarLogin() {
        LoginServicio servicio;
        try {
            // Building the SOAP stubs resolves the WSDL; fail gracefully if it cannot be loaded.
            servicio = new LoginServicioSoap(configuracion);
        } catch (RuntimeException | LinkageError e) {
            error(SIN_CONEXION);
            return;
        }
        LoginFrame login = new LoginFrame(this::mostrarPrincipal);
        new LoginControlador(login, servicio, sesion);
        new EstadoControlador(login.getIndicadorEstado(), estado);
        login.setVisible(true);
    }

    private void mostrarPrincipal() {
        MovimientoServicio servicio;
        PrincipalFrame principal;
        try {
            servicio = new MovimientoServicioSoap(configuracion);
            principal = new PrincipalFrame(sesion.getNombre(), sesion.getTipoUsuario(), sesion.getCuenta(),
                    this::cerrarSesion);
        } catch (RuntimeException | LinkageError e) {
            error(SIN_CONEXION);
            return;
        }
        conectar(principal, servicio);
        principal.setVisible(true);
    }

    private void conectar(PrincipalFrame principal, MovimientoServicio servicio) {
        new MovimientoControlador(principal.getDepositoPanel(), sesion,
                form -> "¿Depositar " + form.getImporteFormateado() + " en la cuenta " + form.getCuenta() + "?",
                (token, form) -> Comprobantes.deposito(form,
                        servicio.registrarDeposito(token, form.getCuenta(), form.getImporte())));

        // A Cliente sends no key (own account, tied to the token); an Empleado sends the typed one
        new MovimientoControlador(principal.getRetiroPanel(), sesion,
                form -> "¿Retirar " + form.getImporteFormateado() + " de la cuenta " + form.getCuenta() + "?",
                (token, form) -> Comprobantes.retiro(form,
                        servicio.registrarRetiro(token, form.getCuenta(), form.getImporte(), form.getClave())));

        new MovimientoControlador(principal.getTransferenciaPanel(), sesion,
                form -> "¿Transferir " + form.getImporteFormateado() + " de la cuenta " + form.getOrigen()
                        + " a la cuenta " + form.getDestino() + "?",
                (token, form) -> Comprobantes.transferencia(form,
                        servicio.registrarTransferencia(token, form.getOrigen(), form.getDestino(),
                                form.getImporte(), form.getClave())));
    }

    private void cerrarSesion() {
        sesion.cerrar();
        mostrarLogin();
    }

    private static void error(String mensaje) {
        JOptionPane.showOptionDialog(null, mensaje, TITULO, JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE, null, new Object[] {"Aceptar"}, "Aceptar");
    }
}
