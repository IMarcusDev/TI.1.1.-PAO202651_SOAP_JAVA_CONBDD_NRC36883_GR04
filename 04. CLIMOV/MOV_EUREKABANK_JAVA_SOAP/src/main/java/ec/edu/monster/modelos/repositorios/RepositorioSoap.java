package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.SoapConfig;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.excepciones.RespuestaInvalidaException;
import ec.edu.monster.modelos.excepciones.SoapFallaException;
import ec.edu.monster.modelos.interfaces.VerificadorRed;
import ec.edu.monster.modelos.soap.SoapConstructorPeticion;
import ec.edu.monster.modelos.soap.SoapLectorRespuesta;
import ec.edu.monster.modelos.soap.SoapTransporte;
import java.io.IOException;
import java.math.BigDecimal;

/** Base de los repositorios SOAP: envía la petición y traduce la respuesta estructurada del servidor. */
abstract class RepositorioSoap {

    private final String url;
    private final SoapTransporte transporte;
    private final VerificadorRed red;

    protected RepositorioSoap(String urlBase, String servicio, SoapTransporte transporte, VerificadorRed red) {
        this.url = urlBase + servicio;
        this.transporte = transporte;
        this.red = red;
    }

    protected SoapConstructorPeticion peticion(String operacion) {
        return new SoapConstructorPeticion(SoapConfig.NAMESPACE, operacion);
    }

    protected boolean sinRed() {
        return red.sinRed();
    }

    protected SoapLectorRespuesta enviar(SoapConstructorPeticion peticion)
            throws IOException, RespuestaInvalidaException, SoapFallaException {
        return new SoapLectorRespuesta(transporte.enviar(url, peticion.construir()));
    }

    /** Convierte una respuesta con estado ERROR en un resultado, distinguiendo el token inválido. */
    protected static <T> Resultado<T> fallo(SoapLectorRespuesta respuesta, String porDefecto) {
        String mensaje = SoapErrores.mensajeServidor(respuesta.texto("mensaje"), porDefecto);
        return SoapErrores.esTokenInvalido(mensaje)
                ? Resultado.sesionExpirada(SoapErrores.SESION_EXPIRADA)
                : Resultado.error(mensaje);
    }

    protected static BigDecimal decimal(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected static String textoOVacio(String texto) {
        return texto != null ? texto : "";
    }
}
