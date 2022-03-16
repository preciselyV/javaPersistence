package PersistenceFramework;

/**
 * Unchecked framework exception
 */

public class PersistenceException extends RuntimeException {
    protected PersistenceException(String msg) {
        super(msg);
    }

    protected PersistenceException(Throwable t) {
        super(t);
    }

    protected PersistenceException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }
}
