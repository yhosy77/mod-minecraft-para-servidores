package ErrorCapa8.skillIssueAdmin;

public class SancionInvalidaException extends Exception {

    public SancionInvalidaException(String mensaje) {
        super(mensaje);
    }

    public SancionInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
