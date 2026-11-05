package ec.edu.monster.modelo;

/**
 * Client-side access to the SOAP LoginWebService. Controllers depend on this
 * interface, never on the generated stubs, so they can be tested with a double.
 */
public interface LoginServicio {

    /**
     * Employee (bank teller) login with username and key.
     *
     * @throws ServicioException when the server rejects the credentials or cannot be reached
     */
    DatosSesion iniciarSesionEmpleado(String usuario, String clave) throws ServicioException;

    /**
     * Cliente login with the account number and its key; the token is tied to that account.
     *
     * @throws ServicioException when the server rejects the credentials or cannot be reached
     */
    DatosSesion iniciarSesionCliente(String cuenta, String clave) throws ServicioException;
}
