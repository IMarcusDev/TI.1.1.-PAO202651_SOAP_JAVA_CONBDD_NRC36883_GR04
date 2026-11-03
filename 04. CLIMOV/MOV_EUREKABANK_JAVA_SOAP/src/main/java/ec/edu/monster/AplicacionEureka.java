package ec.edu.monster;

import android.app.Application;
import android.content.Context;
import ec.edu.monster.config.SoapConfig;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.controlador.MovimientosControlador;
import ec.edu.monster.controlador.OperacionControlador;
import ec.edu.monster.modelos.interfaces.LoginRepositorio;
import ec.edu.monster.modelos.interfaces.MovimientoRepositorio;
import ec.edu.monster.modelos.interfaces.VerificadorRed;
import ec.edu.monster.modelos.repositorios.LoginSoapRepositorio;
import ec.edu.monster.modelos.repositorios.MovimientoSoapRepositorio;
import ec.edu.monster.modelos.soap.SoapTransporte;
import ec.edu.monster.servicios.EjecutorTareas;
import ec.edu.monster.servicios.RedAndroid;
import ec.edu.monster.servicios.SesionServicio;
import java.util.concurrent.Executors;

public class AplicacionEureka extends Application {

    private SesionServicio sesion;
    private LoginRepositorio loginRepositorio;
    private MovimientoRepositorio movimientoRepositorio;
    private EjecutorTareas tareas;

    public static AplicacionEureka de(Context contexto) {
        return (AplicacionEureka) contexto.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sesion = new SesionServicio();

        SoapTransporte transporte = new SoapTransporte();
        VerificadorRed red = new RedAndroid(this);
        loginRepositorio = new LoginSoapRepositorio(SoapConfig.URL_BASE, transporte, red);
        movimientoRepositorio = new MovimientoSoapRepositorio(SoapConfig.URL_BASE, transporte, red);

        tareas = new EjecutorTareas(Executors.newCachedThreadPool());
    }

    public EjecutorTareas getTareas() {
        return tareas;
    }

    public LoginControlador crearLoginControlador() {
        return new LoginControlador(loginRepositorio, sesion);
    }

    public MovimientosControlador crearMovimientosControlador() {
        return new MovimientosControlador(movimientoRepositorio, sesion);
    }

    public OperacionControlador crearOperacionControlador() {
        return new OperacionControlador(movimientoRepositorio, sesion);
    }
}
