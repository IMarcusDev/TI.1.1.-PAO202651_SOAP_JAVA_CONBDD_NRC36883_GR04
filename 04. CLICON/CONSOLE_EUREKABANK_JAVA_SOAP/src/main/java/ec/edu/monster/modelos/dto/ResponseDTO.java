package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//revisado y aprovado por Marcos Escobar
public class ResponseDTO {
  private EstadosEnum estado;
  private String mensaje;

  public ResponseDTO(EstadosEnum estado, String mensaje) {
    this.estado = estado;
    this.mensaje = mensaje;
  }

  public EstadosEnum getEstado() {
    return estado;
  }

  public void setEstado(EstadosEnum estado) {
    this.estado = estado;
  }

  public String getMensaje() {
    return mensaje;
  }

  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }

  /** ECUD4: el servidor responde «Token inválido o sesión expirada»; el cliente vuelve al inicio de sesión. */
  public boolean sesionExpirada() {
    return estado == EstadosEnum.ERROR && mensaje != null && mensaje.contains("Token");
  }

  public static ResponseDTO fromXML(String xml) {
    Element cuerpo = cuerpo(xml);
    return new ResponseDTO(leerEstado(cuerpo), leerTexto(cuerpo, "mensaje"));
  }

  protected static EstadosEnum leerEstado(Element cuerpo) {
    String texto = leerTexto(cuerpo, "estado");
    if (texto.isEmpty())
      throw new IllegalStateException("Respuesta sin estado");
    return EstadosEnum.valueOf(texto.toUpperCase());
  }

  /** El contenido del soap:Body; un soap:Fault se convierte en SoapFallaException. */
  public static Element cuerpo(String xml) {
    Element raiz;
    try {
      DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
      fabrica.setNamespaceAware(true);
      fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      raiz = fabrica.newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement();
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new IllegalStateException("Respuesta que no es XML", e);
    }

    Element body = hijo(raiz, "Body");
    if (body == null)
      throw new IllegalStateException("Respuesta sin soap:Body");

    Element falla = hijo(body, "Fault");
    if (falla != null)
      throw new SoapFallaException(leerTexto(falla, "faultstring"));

    Element respuesta = primerHijo(body);
    if (respuesta == null)
      throw new IllegalStateException("soap:Body vacío");
    return respuesta;
  }

  /** Primer elemento con ese nombre local, en cualquier nivel y sin distinguir mayúsculas. */
  public static String leerTexto(Element padre, String nombre) {
    for (Element e : descendientes(padre)) {
      if (nombre.equalsIgnoreCase(e.getLocalName()))
        return e.getTextContent().trim();
    }
    return "";
  }

  public static BigDecimal leerDecimal(Element padre, String nombre) {
    String texto = leerTexto(padre, nombre);
    try {
      return texto.isEmpty() ? null : new BigDecimal(texto);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static int leerEntero(Element padre, String nombre) {
    try {
      return new BigDecimal(leerTexto(padre, nombre)).intValue();
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  public static List<Element> descendientes(Element padre) {
    List<Element> lista = new ArrayList<>();
    NodeList nodos = padre.getElementsByTagName("*");
    for (int i = 0; i < nodos.getLength(); i++)
      lista.add((Element) nodos.item(i));
    return lista;
  }

  public static boolean tieneHijo(Element padre, String nombre) {
    for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element e && nombre.equalsIgnoreCase(e.getLocalName()))
        return true;
    }
    return false;
  }

  private static Element hijo(Element padre, String nombre) {
    for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element e && nombre.equals(e.getLocalName()))
        return e;
    }
    return null;
  }

  private static Element primerHijo(Element padre) {
    for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element e)
        return e;
    }
    return null;
  }
}
