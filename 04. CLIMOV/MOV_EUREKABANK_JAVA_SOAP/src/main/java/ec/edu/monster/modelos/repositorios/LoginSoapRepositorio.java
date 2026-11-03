package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.config.SoapConfig;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.data.Sesion;
import ec.edu.monster.modelos.enums.Perfil;
import ec.edu.monster.modelos.interfaces.LoginRepositorio;
import ec.edu.monster.modelos.interfaces.VerificadorRed;
import ec.edu.monster.modelos.soap.SoapConstructorPeticion;
import ec.edu.monster.modelos.soap.SoapLectorRespuesta;
import ec.edu.monster.modelos.soap.SoapTransporte;

/** LoginWebService: loginEmpleado, loginCliente y logout (ECUD1). */
public class LoginSoapRepositorio extends RepositorioSoap implements LoginRepositorio {

    static final String CREDENCIALES_INCORRECTAS = "Credenciales incorrectas";

    public LoginSoapRepositorio(String urlBase, SoapTransporte transporte, VerificadorRed red) {
        super(urlBase, SoapConfig.SERVICIO_LOGIN, transporte, red);
    }

    @Override
    public Resultado<Sesion> loginEmpleado(String usuario, String clave) {
        return login(peticion("loginEmpleado")
                .parametro("usuario", usuario)
                .parametro("clave", clave), Perfil.EMPLEADO, null);
    }

    @Override
    public Resultado<Sesion> loginCliente(String cuenta, String clave) {
        return login(peticion("loginCliente")
                .parametro("cuenta", cuenta)
                .parametro("clave", clave), Perfil.CLIENTE, cuenta);
    }

    private Resultado<Sesion> login(SoapConstructorPeticion peticion, Perfil perfilPedido, String cuenta) {
        if (sinRed()) {
            return Resultado.error(SoapErrores.SIN_CONEXION);
        }

        try {
            SoapLectorRespuesta respuesta = enviar(peticion);
            if (respuesta.estado() == null) {
                return Resultado.error(SoapErrores.RESPUESTA_INCOMPLETA);
            }
            if (!respuesta.estadoOk()) {
                return Resultado.error(SoapErrores.mensajeServidor(respuesta.texto("mensaje"), CREDENCIALES_INCORRECTAS));
            }

            String token = respuesta.texto("token");
            if (token == null || token.isEmpty()) {
                return Resultado.error(SoapErrores.RESPUESTA_INCOMPLETA);
            }

            Perfil perfil = Perfil.desde(respuesta.texto("perfil"), perfilPedido);
            Sesion sesion = new Sesion(token, perfil, textoOVacio(respuesta.texto("nombre")), cuenta);
            return Resultado.ok(sesion, textoOVacio(respuesta.texto("mensaje")));
        } catch (Exception e) {
            return Resultado.error(SoapErrores.mensaje(e));
        }
    }

    @Override
    public Resultado<Void> logout(String token) {
        if (sinRed()) {
            return Resultado.error(SoapErrores.SIN_CONEXION);
        }

        try {
            SoapLectorRespuesta respuesta = enviar(peticion("logout").parametro("token", token));
            return respuesta.estadoOk()
                    ? Resultado.ok(null, textoOVacio(respuesta.texto("mensaje")))
                    : Resultado.error(SoapErrores.mensajeServidor(respuesta.texto("mensaje"), SoapErrores.RESPUESTA_INCOMPLETA));
        } catch (Exception e) {
            return Resultado.error(SoapErrores.mensaje(e));
        }
    }
}
