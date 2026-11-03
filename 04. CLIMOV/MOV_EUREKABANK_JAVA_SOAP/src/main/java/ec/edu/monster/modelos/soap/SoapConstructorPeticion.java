package ec.edu.monster.modelos.soap;

public class SoapConstructorPeticion {
    private static final String SOAP_ENV = "http://schemas.xmlsoap.org/soap/envelope/";

    private final String namespace;
    private final String operacion;
    private final StringBuilder parametros = new StringBuilder();

    public SoapConstructorPeticion(String namespace, String operacion) {
        this.namespace = namespace;
        this.operacion = operacion;
    }

    public SoapConstructorPeticion parametro(String nombre, String valor) {
        if (valor != null) {
            parametros.append('<').append(nombre).append('>')
                      .append(escapar(valor))
                      .append("</").append(nombre).append('>');
        }
        return this;
    }

    public SoapConstructorPeticion parametro(String nombre, double valor) {
        return parametro(nombre, String.valueOf(valor));
    }

    public String construir() {
        return "<soap:Envelope xmlns:soap=\"" + SOAP_ENV + "\" xmlns:ws=\"" + namespace + "\">"
             + "<soap:Body>"
             + "<ws:" + operacion + ">" + parametros + "</ws:" + operacion + ">"
             + "</soap:Body>"
             + "</soap:Envelope>";
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
