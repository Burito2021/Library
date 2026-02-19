package net.library.exception;

public class InvalidFingerprint extends RuntimeException {
    public InvalidFingerprint(String message) {
        super(message);
    }
}
