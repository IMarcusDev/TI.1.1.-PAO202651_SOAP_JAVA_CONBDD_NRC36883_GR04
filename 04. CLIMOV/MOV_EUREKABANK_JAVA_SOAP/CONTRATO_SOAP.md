# Contrato SOAP que consume el cliente móvil

El cliente móvil implementa las operaciones del ERS (RNF-001) y del ECUD. Este documento fija los nombres de los parámetros y de los campos de respuesta que lee el cliente, para que el servidor los publique igual.

- Namespace: `http://ws.monster.edu.ec/` (el que genera JAX-WS para el paquete `ec.edu.monster.ws`).
- URL base: `http://<IP>:8080/WS_EUREKABANK_JAVA_SOAP/`, que se configura en `gradle.properties` (`servidor=...`).
- Estilo: document/literal wrapped, como lo publica Metro por defecto. Los parámetros van sin namespace.
- Todas las respuestas llevan `estado` (`OK` o `ERROR`) y `mensaje`. El cliente lee los campos por su nombre local, sin importar en qué orden o nivel de anidamiento lleguen.

## LoginWebService

| Operación | Parámetros (`@WebParam`) | Campos de respuesta si `estado = OK` |
|---|---|---|
| `loginEmpleado` | `usuario`, `clave` | `token`, `perfil` (`EMPLEADO`), `nombre` |
| `loginCliente` | `cuenta`, `clave` | `token`, `perfil` (`CLIENTE`), `nombre` (titular) |
| `logout` | `token` | — |

## MovimientoWebService

| Operación | Parámetros (`@WebParam`) | Campos de respuesta si `estado = OK` |
|---|---|---|
| `consultarMovimientos` | `token`, `cuenta` | Cuenta: `numeroCuenta`, `titular`, `moneda`, `saldo`, `estadoCuenta`. Movimientos: una lista de elementos con `numeroMovimiento`, `fecha`, `tipoMovimiento`, `accion` (`INGRESO`/`SALIDA`), `importe`, `cuentaReferencia` |
| `registrarDeposito` | `token`, `cuenta`, `importe` | `numeroMovimiento`, `nuevoSaldo` |
| `registrarRetiro` | `token`, `cuenta`, `importe`, `clave` | `numeroMovimiento`, `nuevoSaldo` |
| `registrarTransferencia` | `token`, `cuentaOrigen`, `cuentaDestino`, `importe`, `clave` | `numeroMovimiento`, `nuevoSaldo` (de la cuenta de origen) |

Notas para el servidor:

- `importe` llega como texto decimal con punto y dos decimales (`120.50`). Conviene recibirlo como `BigDecimal`.
- Cuando opera el cliente, `cuenta`/`cuentaOrigen` es la cuenta de su sesión y `clave` llega vacía (ECUD3 paso 6).
- El estado de la cuenta se llama `estadoCuenta`, no `estado`, para no confundirlo con el estado de la respuesta.
- `fecha` puede enviarse como `xs:dateTime` (`2026-10-31T16:40:00`) o ya formateada (`31/10/2026 16:40`); el cliente acepta los dos formatos.
- La lista de movimientos puede ir como elementos `<movimientos>` repetidos (lo que genera JAXB para un `List<MovimientoDTO> movimientos`) o envuelta en un contenedor.
- Si el token no es válido, el `mensaje` debe contener la palabra "Token" (ECUD4: "Token inválido o sesión expirada"). Así el cliente lo reconoce, borra la sesión y vuelve al inicio de sesión.
- Para un error de negocio, el cliente muestra el `mensaje` tal como llega ("Saldo insuficiente", "Clave de cuenta incorrecta", etc.). Si el mensaje contiene "clave", el cliente borra el campo de clave.

## Ejemplo de respuesta de `consultarMovimientos`

```xml
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
  <S:Body>
    <ns2:consultarMovimientosResponse xmlns:ns2="http://ws.monster.edu.ec/">
      <return>
        <estado>OK</estado>
        <mensaje>Consulta de movimientos exitosa</mensaje>
        <cuenta>
          <numeroCuenta>00100001</numeroCuenta>
          <titular>Ana Pérez</titular>
          <moneda>USD</moneda>
          <saldo>379.50</saldo>
          <estadoCuenta>ACTIVO</estadoCuenta>
        </cuenta>
        <movimientos>
          <numeroMovimiento>1</numeroMovimiento>
          <fecha>2026-10-30T09:15:00</fecha>
          <tipoMovimiento>Depósito</tipoMovimiento>
          <accion>INGRESO</accion>
          <importe>500.00</importe>
          <cuentaReferencia></cuentaReferencia>
        </movimientos>
      </return>
    </ns2:consultarMovimientosResponse>
  </S:Body>
</S:Envelope>
```

Las pruebas de `src/main/java/ec/edu/monster/pruebas/repositorios` usan estas mismas respuestas y comprueban los sobres SOAP que envía el cliente.
