package ec.edu.monster.modelos.data;

/** Servicios SOAP del servidor Java (JAX-WS/Metro) y operaciones del ERS (RNF-001). */
//revisado y aprovado por Marcos Escobar
public final class Endpoints {
  /** Namespace que genera JAX-WS para el paquete ec.edu.monster.ws. */
  public static final String NAMESPACE = "http://ws.monster.edu.ec/";

  public static final String LOGIN = "/LoginWebService";
  public static final String MOVIMIENTO = "/MovimientoWebService";

  public static final String LOGIN_EMPLEADO = "loginEmpleado";
  public static final String LOGIN_CLIENTE = "loginCliente";
  public static final String LOGOUT = "logout";
  public static final String CONSULTAR = "consultarMovimientos";
  public static final String DEPOSITO = "registrarDeposito";
  public static final String RETIRO = "registrarRetiro";
  public static final String TRANSFERENCIA = "registrarTransferencia";

  private Endpoints() {
  }
}
