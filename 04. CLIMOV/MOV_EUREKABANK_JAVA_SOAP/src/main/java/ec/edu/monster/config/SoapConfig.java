package ec.edu.monster.config;

import ec.edu.monster.BuildConfig;

public final class SoapConfig {

    /** Se define en gradle.properties (propiedad "servidor"). */
    public static final String URL_BASE = BuildConfig.URL_SERVIDOR;

    public static final String NAMESPACE = "http://ws.monster.edu.ec/";
    public static final String SERVICIO_LOGIN = "LoginWebService";
    public static final String SERVICIO_MOVIMIENTO = "MovimientoWebService";

    /** RNF-007: el cliente deja de esperar a los 15 segundos. */
    public static final int TIEMPO_ESPERA_SEGUNDOS = 15;
    public static final int TIEMPO_ESPERA_MS = TIEMPO_ESPERA_SEGUNDOS * 1000;

    private SoapConfig() { }
}
