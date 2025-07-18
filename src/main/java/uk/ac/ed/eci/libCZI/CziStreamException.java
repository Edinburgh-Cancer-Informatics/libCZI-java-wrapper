package uk.ac.ed.eci.libCZI;

public class CziStreamException extends RuntimeException {
    public CziStreamException(String message) {
        super(message);
    }

    public CziStreamException(String message, Throwable cause) {
        super(message, cause);
    }
}