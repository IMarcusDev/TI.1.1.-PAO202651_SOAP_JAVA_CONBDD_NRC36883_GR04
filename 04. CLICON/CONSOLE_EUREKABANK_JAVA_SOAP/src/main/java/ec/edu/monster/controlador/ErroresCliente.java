package ec.edu.monster.controlador;

import ec.edu.monster.modelos.dto.SoapFallaException;

/** Mensajes del cliente cuando no hay respuesta válida del servidor (ECUD1-ECUD4), iguales a los del cliente móvil. */
//revisado y aprovado por Marcos Escobar
public final class ErroresCliente {
  public static final String SIN_SERVIDOR = "No se pudo conectar con el servidor. Verifique la red e intente nuevamente.";
  public static final String RESPUESTA_INCOMPLETA = "El servidor envió una respuesta vacía o incompleta.";
  public static final String SESION_EXPIRADA = "Su sesión expiró o no es válida. Inicie sesión nuevamente.";
  public static final String FALLA_SERVIDOR = "El servidor no pudo procesar la solicitud. Intente más tarde.";

  private ErroresCliente() {
  }

  /** Sin conexión o sin respuesta en 15 segundos, un SOAP Fault o una respuesta que no se puede leer. */
  public static String mensaje(Exception e) {
    if (e instanceof InterruptedException)
      Thread.currentThread().interrupt();

    if (e instanceof SoapFallaException)
      return FALLA_SERVIDOR;

    if (e instanceof IllegalStateException || e instanceof IllegalArgumentException)
      return RESPUESTA_INCOMPLETA;

    return SIN_SERVIDOR;
  }
}
