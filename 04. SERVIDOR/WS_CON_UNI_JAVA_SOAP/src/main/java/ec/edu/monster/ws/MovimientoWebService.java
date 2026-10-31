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
}
