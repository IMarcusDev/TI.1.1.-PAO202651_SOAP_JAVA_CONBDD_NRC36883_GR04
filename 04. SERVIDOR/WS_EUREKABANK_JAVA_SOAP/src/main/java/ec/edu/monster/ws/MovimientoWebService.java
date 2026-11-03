/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.ws;

import ec.edu.monster.controladores.MovimientoController;
import ec.edu.monster.modelos.data.Movimiento;
import ec.edu.monster.modelos.dto.MovimientoResponseDTO;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.servicios.TokenService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
//revisado y aprovado por Marcos Escobar
import ec.edu.monster.controladores.LoginController;
import ec.edu.monster.modelos.data.Cuenta;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.data.Usuario;
import ec.edu.monster.modelos.dto.ConsultaMovimientosResponseDTO;
import ec.edu.monster.modelos.dto.CuentaDTO;
import ec.edu.monster.modelos.dto.MovimientoDTO;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 *
 * @author Mateo Sosa
 */
@WebService(serviceName = "MovimientoWebService")
public class MovimientoWebService {
  @WebMethod(operationName = "deposito")
  public MovimientoResponseDTO deposito(
    @WebParam(name = "token") String token,
    @WebParam(name = "cuenta_origen") String cuentaOrigen,
    @WebParam(name = "cuenta_destino") String cuentaDestino,
    @WebParam(name = "valor") double valor
  ) {
    if (!TokenService.validateToken(token))
      return new MovimientoResponseDTO(
        EstadosEnum.ERROR,
        "Token inválido",
        0
      );
    
    try {
      //revisado y aprovado por Marcos Escobar
      MovimientoController.autorizar(TokenService.getSesion(token), "DEPOSITO", cuentaOrigen);

      Movimiento movimiento = MovimientoController.registerDeposito(
        new Movimiento(
          "0",
          "DEPOSITO",
          cuentaOrigen,
          cuentaDestino,
          valor
        )
      );

      return new MovimientoResponseDTO(
        EstadosEnum.OK,
        "Depósito registrado exitosamente",
        Integer.parseInt(movimiento.getId())
      );
    } catch (Exception e) {
      return new MovimientoResponseDTO(
        EstadosEnum.ERROR,
        e.getLocalizedMessage(),
        0
      );
    }
  }

  @WebMethod(operationName = "retiro")
  public MovimientoResponseDTO retiro(
    @WebParam(name = "token") String token,
    @WebParam(name = "cuenta_origen") String cuentaOrigen,
    @WebParam(name = "cuenta_destino") String cuentaDestino,
    @WebParam(name = "valor") double valor
  ) {
    if (!TokenService.validateToken(token))
      return new MovimientoResponseDTO(
        EstadosEnum.ERROR,
        "Token inválido",
        0
      );
    
    try {
      //revisado y aprovado por Marcos Escobar
      MovimientoController.autorizar(TokenService.getSesion(token), "RETIRO", cuentaOrigen);

      Movimiento movimiento = MovimientoController.registerRetiro(
        new Movimiento(
          "0",
          "RETIRO",
          cuentaOrigen,
          cuentaDestino,
          valor
        )
      );

      return new MovimientoResponseDTO(
        EstadosEnum.OK,
        "Retiro registrado exitosamente",
        Integer.parseInt(movimiento.getId())
      );
    } catch (Exception e) {
      return new MovimientoResponseDTO(
        EstadosEnum.ERROR,
        e.getLocalizedMessage(),
        0
      );
    }
  }

  @WebMethod(operationName = "transferencia")
  public MovimientoResponseDTO transferencia(
    @WebParam(name = "token") String token,
    @WebParam(name = "cuenta_origen") String cuentaOrigen,
    @WebParam(name = "cuenta_destino") String cuentaDestino,
    @WebParam(name = "valor") double valor
  ) {
    if (!TokenService.validateToken(token))
      return new MovimientoResponseDTO(
        EstadosEnum.ERROR,
        "Token inválido",
        0
      );
    
    try {
      //revisado y aprovado por Marcos Escobar
      MovimientoController.autorizar(TokenService.getSesion(token), "TRANSFERENCIA", cuentaOrigen);

      Movimiento movimiento = MovimientoController.registerTransferencia(
        new Movimiento(
          "0",
          "TRANSFERENCIA",
          cuentaOrigen,
          cuentaDestino,
          valor
        )
      );

      return new MovimientoResponseDTO(
        EstadosEnum.OK,
        "Transferencia registrada exitosamente",
        Integer.parseInt(movimiento.getId())
      );
    } catch (Exception e) {
      return new MovimientoResponseDTO(
        EstadosEnum.ERROR,
        e.getLocalizedMessage(),
        0
      );
    }
  }

  //revisado y aprovado por Marcos Escobar
  @WebMethod(operationName = "consultarMovimientos")
  public ConsultaMovimientosResponseDTO consultarMovimientos(
    @WebParam(name = "token") String token,
    @WebParam(name = "cuenta") String cuenta
  ) {
    Sesion sesion = TokenService.getSesion(token);
    if (sesion == null)
      return new ConsultaMovimientosResponseDTO(EstadosEnum.ERROR, MovimientoController.TOKEN_INVALIDO);

    try {
      Cuenta datos = MovimientoController.getCuentaConsultable(sesion, cuenta);
      Usuario titular = LoginController.getUsuarioById(cuenta);

      ConsultaMovimientosResponseDTO respuesta = new ConsultaMovimientosResponseDTO(
        EstadosEnum.OK,
        "Consulta de movimientos exitosa"
      );
      respuesta.setCuenta(new CuentaDTO(datos.getId(), titular.getNombre(), datos.getSaldo(), datos.getEstado()));
      for (Movimiento m : MovimientoController.getMovimientosByUserId(cuenta))
        respuesta.getMovimientos().add(MovimientoDTO.desde(m, cuenta));

      return respuesta;
    } catch (SQLException e) {
      return new ConsultaMovimientosResponseDTO(EstadosEnum.ERROR, LoginController.SERVICIO_NO_DISPONIBLE);
    } catch (Exception e) {
      return new ConsultaMovimientosResponseDTO(EstadosEnum.ERROR, e.getLocalizedMessage());
    }
  }

  //revisado y aprovado por Marcos Escobar
  @WebMethod(operationName = "registrarDeposito")
  public MovimientoResponseDTO registrarDeposito(
    @WebParam(name = "token") String token,
    @WebParam(name = "cuenta") String cuenta,
    @WebParam(name = "importe") BigDecimal importe
  ) {
    return registrar(token, "DEPOSITO", cuenta, null, importe, null, "Depósito registrado correctamente");
  }

  //revisado y aprovado por Marcos Escobar
  @WebMethod(operationName = "registrarRetiro")
  public MovimientoResponseDTO registrarRetiro(
    @WebParam(name = "token") String token,
    @WebParam(name = "cuenta") String cuenta,
    @WebParam(name = "importe") BigDecimal importe,
    @WebParam(name = "clave") String clave
  ) {
    return registrar(token, "RETIRO", cuenta, null, importe, clave, "Retiro registrado correctamente");
  }

  //revisado y aprovado por Marcos Escobar
  @WebMethod(operationName = "registrarTransferencia")
  public MovimientoResponseDTO registrarTransferencia(
    @WebParam(name = "token") String token,
    @WebParam(name = "cuentaOrigen") String cuentaOrigen,
    @WebParam(name = "cuentaDestino") String cuentaDestino,
    @WebParam(name = "importe") BigDecimal importe,
    @WebParam(name = "clave") String clave
  ) {
    return registrar(
      token, "TRANSFERENCIA", cuentaOrigen, cuentaDestino, importe, clave,
      "Transferencia registrada correctamente"
    );
  }

  //revisado y aprovado por Marcos Escobar
  private static MovimientoResponseDTO registrar(
    String token,
    String tipo,
    String cuentaOrigen,
    String cuentaDestino,
    BigDecimal importe,
    String clave,
    String mensajeOk
  ) {
    try {
      Movimiento movimiento = MovimientoController.registrar(
        TokenService.getSesion(token), tipo, cuentaOrigen, cuentaDestino, importe, clave
      );
      int numero = Integer.parseInt(movimiento.getId());

      MovimientoResponseDTO respuesta = new MovimientoResponseDTO(EstadosEnum.OK, mensajeOk, numero);
      respuesta.setNumeroMovimiento(numero);
      respuesta.setNuevoSaldo(MovimientoDTO.monto(movimiento.getNuevoSaldo()));
      return respuesta;
    } catch (Exception e) {
      return new MovimientoResponseDTO(EstadosEnum.ERROR, e.getLocalizedMessage(), 0);
    }
  }
}
