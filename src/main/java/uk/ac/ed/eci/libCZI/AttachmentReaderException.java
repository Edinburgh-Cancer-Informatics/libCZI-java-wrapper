package uk.ac.ed.eci.libCZI;

public class AttachmentReaderException extends RuntimeException {
    public AttachmentReaderException(String message) {
        super(message);
    }

    public AttachmentReaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
