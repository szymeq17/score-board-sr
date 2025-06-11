package org.szymonrysz.exception;

import java.util.UUID;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(UUID id) {
        super(String.format("Game with id=%s not found!", id));
    }
}
