package ec.edu.monster.modelos.http;

import ec.edu.monster.modelos.data.Endpoints;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Envía sobres SOAP 1.1 document/literal wrapped como los publica Metro: la operación va en el namespace
 * del servicio y los parámetros sin namespace.
 */
//revisado y aprovado por Marcos Escobar
public class SOAPClient {
  private static final String SOAP_ENV = "http://schemas.xmlsoap.org/soap/envelope/";

  /** @param parametros pares nombre, valor; un valor null no se envía. */
  public String post(String servicio, String operacion, String... parametros) throws IOException, InterruptedException {
    RespuestaHttp respuesta = ConfigHTTP.getTransporte().enviar(
      ConfigHTTP.getBaseUrl() + servicio,
      sobre(operacion, parametros),
      "text/xml; charset=utf-8",
      Map.of("SOAPAction", "\"\"")
    );

    return respuesta.cuerpo();
  }

  public static String sobre(String operacion, String... parametros) {
    StringBuilder cuerpo = new StringBuilder();

    for (int i = 0; i + 1 < parametros.length; i += 2) {
      if (parametros[i + 1] != null)
        cuerpo.append('<').append(parametros[i]).append('>')
          .append(escapar(parametros[i + 1]))
          .append("</").append(parametros[i]).append('>');
    }

    return "<soap:Envelope xmlns:soap=\"" + SOAP_ENV + "\"><soap:Body>"
      + "<ns:" + operacion + " xmlns:ns=\"" + Endpoints.NAMESPACE + "\">" + cuerpo + "</ns:" + operacion + ">"
      + "</soap:Body></soap:Envelope>";
  }

  /** El importe viaja como texto decimal con punto y dos decimales (120.50). */
  public static String importe(BigDecimal valor) {
    return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private static String escapar(String texto) {
    return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
      .replace("\"", "&quot;").replace("'", "&apos;");
  }
}
