package ec.edu.monster.modelos.soap;

import ec.edu.monster.modelos.excepciones.RespuestaInvalidaException;
import ec.edu.monster.modelos.excepciones.SoapFallaException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SoapLectorRespuesta {

    public static final String ESTADO_OK = "OK";

    private final Document documento;

    public SoapLectorRespuesta(String xml) throws RespuestaInvalidaException, SoapFallaException {
        this.documento = parsear(xml);

        String falla = texto("faultstring");
        if (falla != null) {
            throw new SoapFallaException(falla);
        }
    }

    public String estado() {
        return texto("estado");
    }

    public boolean estadoOk() {
        return ESTADO_OK.equals(estado());
    }

    public String texto(String nombre) {
        NodeList nodos = documento.getElementsByTagNameNS("*", nombre);
        return nodos.getLength() > 0 ? nodos.item(0).getTextContent().trim() : null;
    }

    /**
     * Devuelve, como mapas nombre-valor, los elementos que contienen el campo indicado.
     * Así se leen las listas (por ejemplo, los movimientos) sin depender de si el servidor
     * las envuelve en un elemento contenedor o no.
     */
    public List<Map<String, String>> registros(String campoClave) {
        NodeList claves = documento.getElementsByTagNameNS("*", campoClave);
        List<Map<String, String>> registros = new ArrayList<>();
        for (int i = 0; i < claves.getLength(); i++) {
            Node padre = claves.item(i).getParentNode();
            Map<String, String> registro = new HashMap<>();
            NodeList hijos = padre.getChildNodes();
            for (int j = 0; j < hijos.getLength(); j++) {
                Node hijo = hijos.item(j);
                if (hijo.getNodeType() == Node.ELEMENT_NODE) {
                    registro.put(nombreLocal(hijo), hijo.getTextContent().trim());
                }
            }
            registros.add(registro);
        }
        return registros;
    }

    private static String nombreLocal(Node nodo) {
        return nodo.getLocalName() != null ? nodo.getLocalName() : nodo.getNodeName();
    }

    private static Document parsear(String xml) throws RespuestaInvalidaException {
        if (xml == null || xml.trim().isEmpty()) {
            throw new RespuestaInvalidaException("Respuesta vacía", null);
        }
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setNamespaceAware(true);
            return fabrica.newDocumentBuilder()
                          .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RespuestaInvalidaException("La respuesta no es un XML válido", e);
        }
    }
}
