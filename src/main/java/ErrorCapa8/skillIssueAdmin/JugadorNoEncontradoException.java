package ErrorCapa8.skillIssueAdmin;

public class JugadorNoEncontradoException extends Exception {

    public JugadorNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public JugadorNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
