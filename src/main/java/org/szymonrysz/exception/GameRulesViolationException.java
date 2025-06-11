package org.szymonrysz.exception;

public class GameRulesViolationException extends RuntimeException {

    public GameRulesViolationException(String message) {
        super(message);
    }
}
