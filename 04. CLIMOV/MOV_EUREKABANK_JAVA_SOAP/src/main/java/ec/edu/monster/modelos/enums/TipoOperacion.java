package ec.edu.monster.modelos.enums;

public enum TipoOperacion {
    DEPOSITO("registrarDeposito"),
    RETIRO("registrarRetiro"),
    TRANSFERENCIA("registrarTransferencia");

    private final String operacionSoap;

    TipoOperacion(String operacionSoap) {
        this.operacionSoap = operacionSoap;
    }

    public String getOperacionSoap() {
        return operacionSoap;
    }
}
