package ec.edu.monster.fakes;

import ec.edu.monster.config.WsConfig;
import jakarta.xml.ws.Endpoint;

/**
 * Publishes a fake SOAP service at the address WsConfig points to,
 * so tests exercise the real Java -> XML -> HTTP -> XML -> Java round trip
 * without Payara, the real server or the database.
 */
public final class FakeSoapServer {
  private FakeSoapServer() {
  }

  /** Starts the fake; call stop() on the returned Endpoint after the test. */
  public static Endpoint publish(String serviceName, Object implementation) {
    return Endpoint.publish(WsConfig.getEndpoint(serviceName), implementation);
  }
}
