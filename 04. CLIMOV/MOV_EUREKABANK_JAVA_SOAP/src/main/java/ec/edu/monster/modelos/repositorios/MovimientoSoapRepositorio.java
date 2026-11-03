package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.SoapConfig;
import ec.edu.monster.modelos.data.Comprobante;
import ec.edu.monster.modelos.data.ConsultaMovimientos;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Movimiento;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.SolicitudOperacion;
import ec.edu.monster.modelos.enums.TipoOperacion;
import ec.edu.monster.modelos.interfaces.MovimientoRepositorio;
import ec.edu.monster.modelos.interfaces.VerificadorRed;
import ec.edu.monster.modelos.soap.SoapConstructorPeticion;
import ec.edu.monster.modelos.soap.SoapLectorRespuesta;
import ec.edu.monster.modelos.soap.SoapTransporte;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** MovimientoWebService: consultarMovimientos (ECUD2) y registrarDeposito/Retiro/Transferencia (ECUD3). */
public class MovimientoSoapRepositorio extends RepositorioSoap implements MovimientoRepositorio {

    static final String NO_SE_PUDO_REGISTRAR = "No se pudo registrar la operación, intente nuevamente";

    public MovimientoSoapRepositorio(String urlBase, SoapTransporte transporte, VerificadorRed red) {
        super(urlBase, SoapConfig.SERVICIO_MOVIMIENTO, transporte, red);
    }

    @Override
    public Resultado<ConsultaMovimientos> consultarMovimientos(String token, String cuenta) {
        if (sinRed()) {
            return Resultado.error(SoapErrores.SIN_CONEXION);
        }

        try {
            SoapLectorRespuesta respuesta = enviar(peticion("consultarMovimientos")
                    .parametro("token", token)
                    .parametro("cuenta", cuenta));
            if (respuesta.estado() == null) {
                return Resultado.error(SoapErrores.RESPUESTA_INCOMPLETA);
            }
            if (!respuesta.estadoOk()) {
                return fallo(respuesta, "La cuenta no existe");
            }

            String numero = respuesta.texto("numeroCuenta");
            Cuenta datosCuenta = new Cuenta(
                    numero != null && !numero.isEmpty() ? numero : cuenta,
                    textoOVacio(respuesta.texto("titular")),
                    textoOVacio(respuesta.texto("moneda")),
                    decimal(respuesta.texto("saldo")),
                    textoOVacio(respuesta.texto("estadoCuenta")));

            List<Movimiento> movimientos = new ArrayList<>();
            for (Map<String, String> registro : respuesta.registros("numeroMovimiento")) {
                movimientos.add(new Movimiento(
                        textoOVacio(registro.get("numeroMovimiento")),
                        textoOVacio(registro.get("fecha")),
                        textoOVacio(registro.get("tipoMovimiento")),
                        textoOVacio(registro.get("accion")),
                        decimal(registro.get("importe")),
                        textoOVacio(registro.get("cuentaReferencia"))));
            }

            return Resultado.ok(new ConsultaMovimientos(datosCuenta, movimientos),
                    textoOVacio(respuesta.texto("mensaje")));
        } catch (Exception e) {
            return Resultado.error(SoapErrores.mensaje(e));
        }
    }

    @Override
    public Resultado<Comprobante> registrar(String token, SolicitudOperacion solicitud) {
        if (sinRed()) {
            return Resultado.error(SoapErrores.SIN_CONEXION);
        }

        try {
            SoapLectorRespuesta respuesta = enviar(peticionDe(token, solicitud));
            if (respuesta.estado() == null) {
                return Resultado.error(SoapErrores.RESPUESTA_INCOMPLETA);
            }
            if (!respuesta.estadoOk()) {
                return fallo(respuesta, NO_SE_PUDO_REGISTRAR);
            }

            Comprobante comprobante = new Comprobante(solicitud,
                    textoOVacio(respuesta.texto("numeroMovimiento")),
                    decimal(respuesta.texto("nuevoSaldo")),
                    new Date());
            return Resultado.ok(comprobante, textoOVacio(respuesta.texto("mensaje")));
        } catch (Exception e) {
            // ECUD3 FA10: no se reintenta automáticamente para no duplicar movimientos.
            return Resultado.error(SoapErrores.mensaje(e));
        }
    }

    private SoapConstructorPeticion peticionDe(String token, SolicitudOperacion solicitud) {
        TipoOperacion tipo = solicitud.getTipo();
        String importe = solicitud.getImporte().toPlainString();
        SoapConstructorPeticion peticion = peticion(tipo.getOperacionSoap()).parametro("token", token);

        switch (tipo) {
            case DEPOSITO:
                return peticion.parametro("cuenta", solicitud.getCuenta())
                               .parametro("importe", importe);
            case RETIRO:
                return peticion.parametro("cuenta", solicitud.getCuenta())
                               .parametro("importe", importe)
                               .parametro("clave", solicitud.getClave());
            case TRANSFERENCIA:
            default:
                return peticion.parametro("cuentaOrigen", solicitud.getCuenta())
                               .parametro("cuentaDestino", solicitud.getCuentaDestino())
                               .parametro("importe", importe)
                               .parametro("clave", solicitud.getClave());
        }
    }
}
