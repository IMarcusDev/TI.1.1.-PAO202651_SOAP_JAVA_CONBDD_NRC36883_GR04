package ec.edu.monster.pruebas.repositorios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.modelos.data.Comprobante;
import ec.edu.monster.modelos.data.ConsultaMovimientos;
import ec.edu.monster.modelos.data.Movimiento;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.SolicitudOperacion;
import ec.edu.monster.modelos.enums.TipoOperacion;
import ec.edu.monster.modelos.repositorios.MovimientoSoapRepositorio;
import ec.edu.monster.pruebas.apoyo.TransporteFalso;
import java.math.BigDecimal;
import org.junit.Test;

public class MovimientoSoapRepositorioTest {

    private static final String URL_BASE = "http://servidor:8080/WS_CON_UNI_JAVA_SOAP/";

    private static MovimientoSoapRepositorio repositorio(TransporteFalso transporte) {
        return new MovimientoSoapRepositorio(URL_BASE, transporte, () -> false);
    }

    private static final String MOVIMIENTO_1 = "<numeroMovimiento>1</numeroMovimiento><fecha>2026-10-30T09:15:00</fecha>"
            + "<tipoMovimiento>Depósito</tipoMovimiento><accion>INGRESO</accion><importe>500.00</importe>"
            + "<cuentaReferencia></cuentaReferencia>";
    private static final String MOVIMIENTO_2 = "<numeroMovimiento>2</numeroMovimiento><fecha>2026-10-31T16:40:00</fecha>"
            + "<tipoMovimiento>Transferencia</tipoMovimiento><accion>SALIDA</accion><importe>120.50</importe>"
            + "<cuentaReferencia>00100002</cuentaReferencia>";

    @Test
    public void consultar_leeLaCuentaYSusMovimientos() {
        TransporteFalso transporte = TransporteFalso.responde(TransporteFalso.respuesta("consultarMovimientos",
                "<estado>OK</estado><mensaje>Consulta de movimientos exitosa</mensaje>"
                + "<cuenta><numeroCuenta>00100001</numeroCuenta><titular>Ana Pérez</titular><moneda>USD</moneda>"
                + "<saldo>379.50</saldo><estadoCuenta>ACTIVO</estadoCuenta></cuenta>"
                + "<movimientos>" + MOVIMIENTO_1 + "</movimientos>"
                + "<movimientos>" + MOVIMIENTO_2 + "</movimientos>"));

        Resultado<ConsultaMovimientos> resultado = repositorio(transporte).consultarMovimientos("tok", "00100001");

        assertTrue(resultado.isExito());
        assertEquals("Ana Pérez", resultado.getDatos().getCuenta().getTitular());
        assertEquals(new BigDecimal("379.50"), resultado.getDatos().getCuenta().getSaldo());
        assertEquals(2, resultado.getDatos().getMovimientos().size());
        Movimiento transferencia = resultado.getDatos().getMovimientos().get(1);
        assertFalse(transferencia.esIngreso());
        assertEquals("00100002", transferencia.getCuentaReferencia());
        assertEquals(URL_BASE + "MovimientoWebService", transporte.ultimaUrl());
        assertTrue(transporte.ultimoSobre().contains(
                "<ws:consultarMovimientos><token>tok</token><cuenta>00100001</cuenta></ws:consultarMovimientos>"));
    }

    @Test
    public void consultar_aceptaMovimientosEnvueltosEnUnContenedor() {
        TransporteFalso transporte = TransporteFalso.responde(TransporteFalso.respuesta("consultarMovimientos",
                "<estado>OK</estado><mensaje>ok</mensaje><numeroCuenta>00100001</numeroCuenta>"
                + "<movimientos><movimiento>" + MOVIMIENTO_1 + "</movimiento>"
                + "<movimiento>" + MOVIMIENTO_2 + "</movimiento></movimientos>"));

        assertEquals(2, repositorio(transporte).consultarMovimientos("tok", "00100001").getDatos().getMovimientos().size());
    }

    @Test
    public void consultar_cuentaSinMovimientos_devuelveListaVacia() {
        TransporteFalso transporte = TransporteFalso.responde(TransporteFalso.respuesta("consultarMovimientos",
                "<estado>OK</estado><mensaje>ok</mensaje><cuenta><numeroCuenta>00100001</numeroCuenta></cuenta>"));

        assertTrue(repositorio(transporte).consultarMovimientos("tok", "00100001").getDatos().getMovimientos().isEmpty());
    }

    @Test
    public void tokenInvalido_marcaLaSesionComoExpirada() {
        TransporteFalso transporte = TransporteFalso.responde(TransporteFalso.respuesta("consultarMovimientos",
                "<estado>ERROR</estado><mensaje>Token inválido o sesión expirada</mensaje>"));

        assertTrue(repositorio(transporte).consultarMovimientos("tok", "00100001").isSesionExpirada());
    }

    @Test
    public void registrarTransferencia_enviaLasDosCuentasLaClaveYElImporte() {
        TransporteFalso transporte = TransporteFalso.responde(TransporteFalso.respuesta("registrarTransferencia",
                "<estado>OK</estado><mensaje>Transferencia registrada correctamente</mensaje>"
                + "<numeroMovimiento>15</numeroMovimiento><nuevoSaldo>879.50</nuevoSaldo>"));
        SolicitudOperacion solicitud = new SolicitudOperacion(TipoOperacion.TRANSFERENCIA,
                "00100001", "00100002", new BigDecimal("120.50"), "123456");

        Resultado<Comprobante> resultado = repositorio(transporte).registrar("tok", solicitud);

        assertTrue(resultado.isExito());
        assertEquals("15", resultado.getDatos().getNumeroMovimiento());
        assertEquals(new BigDecimal("879.50"), resultado.getDatos().getNuevoSaldo());
        assertTrue(transporte.ultimoSobre().contains("<ws:registrarTransferencia><token>tok</token>"
                + "<cuentaOrigen>00100001</cuentaOrigen><cuentaDestino>00100002</cuentaDestino>"
                + "<importe>120.50</importe><clave>123456</clave></ws:registrarTransferencia>"));
    }

    @Test
    public void registrarDeposito_noEnviaClave() {
        TransporteFalso transporte = TransporteFalso.responde(TransporteFalso.respuesta("registrarDeposito",
                "<estado>OK</estado><mensaje>ok</mensaje><numeroMovimiento>3</numeroMovimiento><nuevoSaldo>10</nuevoSaldo>"));
        SolicitudOperacion solicitud = new SolicitudOperacion(TipoOperacion.DEPOSITO,
                "00100001", null, new BigDecimal("10.00"), "");

        repositorio(transporte).registrar("tok", solicitud);

        assertTrue(transporte.ultimoSobre().contains("<ws:registrarDeposito><token>tok</token>"
                + "<cuenta>00100001</cuenta><importe>10.00</importe></ws:registrarDeposito>"));
    }

    @Test
    public void registrar_errorDeNegocio_muestraElMensajeDelServidor() {
        TransporteFalso transporte = TransporteFalso.responde(TransporteFalso.respuesta("registrarRetiro",
                "<estado>ERROR</estado><mensaje>Saldo insuficiente</mensaje>"));
        SolicitudOperacion solicitud = new SolicitudOperacion(TipoOperacion.RETIRO,
                "00100001", null, new BigDecimal("9999.00"), "123456");

        Resultado<Comprobante> resultado = repositorio(transporte).registrar("tok", solicitud);

        assertFalse(resultado.isExito());
        assertFalse(resultado.isSesionExpirada());
        assertEquals("Saldo insuficiente", resultado.getMensaje());
    }
}
