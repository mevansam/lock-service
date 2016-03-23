package org.appbricks.commons.lock.exception;

/**
 * Exception thrown if lock update failed.
 */
public class LockUpdateException
    extends LockException {

    public LockUpdateException(Throwable cause, String format, Object...args) {
        super(String.format(format, args), cause);
    }
}
