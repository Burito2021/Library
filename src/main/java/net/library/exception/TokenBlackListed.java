package net.library.exception;

public class TokenBlackListed extends RuntimeException {
    public TokenBlackListed(String message) {
        super(message);
    }
}
