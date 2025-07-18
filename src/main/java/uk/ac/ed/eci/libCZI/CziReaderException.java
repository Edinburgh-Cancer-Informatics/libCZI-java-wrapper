package uk.ac.ed.eci.libCZI;

public class CziReaderException extends RuntimeException {
    public CziReaderException(String message) {
        super(message);
    }

    public CziReaderException(String message, Throwable cause) {
        super(message, cause);
    }
}