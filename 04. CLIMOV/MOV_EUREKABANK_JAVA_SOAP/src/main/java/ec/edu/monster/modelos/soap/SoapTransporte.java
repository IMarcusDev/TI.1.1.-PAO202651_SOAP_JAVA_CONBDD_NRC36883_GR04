package ec.edu.monster.modelos.soap;

import ec.edu.monster.config.SoapConfig;
import ec.edu.monster.modelos.excepciones.HttpEstadoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SoapTransporte {

    private final int tiempoEsperaMs;

    public SoapTransporte() {
        this(SoapConfig.TIEMPO_ESPERA_MS);
    }

    public SoapTransporte(int tiempoEsperaMs) {
        this.tiempoEsperaMs = tiempoEsperaMs;
    }

    public String enviar(String url, String sobre) throws IOException {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) new URL(url).openConnection();
            conexion.setRequestMethod("POST");
            conexion.setConnectTimeout(tiempoEsperaMs);
            conexion.setReadTimeout(tiempoEsperaMs);
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conexion.setRequestProperty("SOAPAction", "\"\"");

            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(sobre.getBytes(StandardCharsets.UTF_8));
            }

            int codigo = conexion.getResponseCode();
            if (codigo == HttpURLConnection.HTTP_OK) {
                return leer(conexion.getInputStream());
            }
            if (codigo == HttpURLConnection.HTTP_INTERNAL_ERROR && conexion.getErrorStream() != null) {
                return leer(conexion.getErrorStream());
            }
            throw new HttpEstadoException(codigo, url);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private static String leer(InputStream flujo) throws IOException {
        try (InputStream entrada = flujo; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] bloque = new byte[4096];
            int leidos;
            while ((leidos = entrada.read(bloque)) != -1) {
                buffer.write(bloque, 0, leidos);
            }
            return buffer.toString(StandardCharsets.UTF_8.name());
        }
    }
}
