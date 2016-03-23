package org.appbricks.commons.exception;

/**
 * Lock thrown if lock acquisition times out.
 */
public class LockException
    extends Throwable {

    public LockException(String format, Object... args) {
        super(String.format(format, args));
    }

    public LockException(Throwable cause, String format, Object... args) {
        super(String.format(format, args), cause);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }
}
