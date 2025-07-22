package uk.ac.ed.eci.libCZI;

public class CziBitmapException extends RuntimeException  {
    public CziBitmapException(String message) {
        super(message);
    }

    public CziBitmapException(String message, Throwable cause) {
        super(message, cause);
    }
}