package ru.game.aurora.modding;

/**
 * Thrown by mods
 */
public class ModException extends Exception {

    public ModException() {
    }

    public ModException(String message) {
        super(message);
    }

    public ModException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModException(Throwable cause) {
        super(cause);
    }
}
