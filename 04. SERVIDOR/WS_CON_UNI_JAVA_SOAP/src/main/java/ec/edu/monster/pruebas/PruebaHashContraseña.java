/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.pruebas;

import ec.edu.monster.servicios.HashService;

/**
 *
 * @author Mateo Sosa
 */
public class PruebaHashContraseña {
  public static void main(String args[]) {
    System.out.println("msosa - " + HashService.hashPassword("msosa"));
    System.out.println("empl1234 - " + HashService.hashPassword("empl1234"));
  }
}
