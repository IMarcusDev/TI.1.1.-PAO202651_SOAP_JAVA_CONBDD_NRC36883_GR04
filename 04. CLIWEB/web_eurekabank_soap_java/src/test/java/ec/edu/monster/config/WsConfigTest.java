package ec.edu.monster.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** WsConfig must build the URLs from config.properties (test copy). */
class WsConfigTest {

  @Test
  void buildsBaseUrlFromHostPortAndContext() {
    assertEquals("http://localhost:18089/eurekabank-test", WsConfig.getBaseUrl());
  }

  @Test
  void appendsServiceNameToBaseUrl() {
    assertEquals("http://localhost:18089/eurekabank-test/LoginWebService",
        WsConfig.getEndpoint("LoginWebService"));
  }
}
