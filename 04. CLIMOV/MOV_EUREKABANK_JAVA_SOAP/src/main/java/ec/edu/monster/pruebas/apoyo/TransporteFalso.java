package ec.edu.monster.pruebas.apoyo;

import ec.edu.monster.modelos.soap.SoapTransporte;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class TransporteFalso extends SoapTransporte {

    public interface Respuesta {
        String para(String sobre) throws IOException;
    }

    private final Respuesta respuesta;
    private final List<String> urls = Collections.synchronizedList(new ArrayList<>());
    private final List<String> sobres = Collections.synchronizedList(new ArrayList<>());

    public TransporteFalso(Respuesta respuesta) {
        this.respuesta = respuesta;
    }

    public static TransporteFalso responde(String xml) {
        return new TransporteFalso(sobre -> xml);
    }

    public static TransporteFalso segun(Function<String, String> elegir) {
        return new TransporteFalso(elegir::apply);
    }

    public static TransporteFalso lanza(IOException error) {
        return new TransporteFalso(sobre -> {
            throw error;
        });
    }

    @Override
    public String enviar(String url, String sobre) throws IOException {
        urls.add(url);
        sobres.add(sobre);
        return respuesta.para(sobre);
    }

    public int peticiones() {
        return sobres.size();
    }

    public String ultimaUrl() {
        return urls.get(urls.size() - 1);
    }

    public String ultimoSobre() {
        return sobres.get(sobres.size() - 1);
    }

    public static String respuesta(String operacion, String contenido) {
        return "<?xml version='1.0' encoding='UTF-8'?>"
             + "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
             + "<ns2:" + operacion + "Response xmlns:ns2=\"http://ws.monster.edu.ec/\">"
             + "<return>" + contenido + "</return>"
             + "</ns2:" + operacion + "Response></S:Body></S:Envelope>";
    }

    public static String falla(String faultstring) {
        return "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><S:Fault>"
             + "<faultcode>S:Server</faultcode><faultstring>" + faultstring + "</faultstring>"
             + "</S:Fault></S:Body></S:Envelope>";
    }
}
