package PersistenceFramework;

public class PersistenceException extends RuntimeException {
    protected PersistenceException(String msg) {
        super(msg);
    }

    protected PersistenceException(Throwable t) {
        super(t);
    }
}
