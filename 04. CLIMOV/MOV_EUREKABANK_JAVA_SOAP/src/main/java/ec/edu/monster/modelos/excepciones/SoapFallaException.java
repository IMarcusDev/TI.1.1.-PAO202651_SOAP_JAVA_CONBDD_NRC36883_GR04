package ec.edu.monster.modelos.excepciones;

public class SoapFallaException extends Exception {
    public SoapFallaException(String faultstring) {
        super(faultstring);
    }
}
