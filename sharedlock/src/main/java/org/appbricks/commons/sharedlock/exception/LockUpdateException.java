package org.appbricks.commons.sharedlock.exception;

/**
 * Exception thrown if sharedlock update failed.
 */
public class LockUpdateException
    extends LockException {

    public LockUpdateException(Throwable cause, String format, Object...args) {
        super(String.format(format, args), cause);
    }
}
