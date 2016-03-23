package org.appbricks.commons.lock.exception;

/**
 * Exception thrown when waiting for a lock times out.
 */
public class LockTimedoutException
    extends LockException {

    public LockTimedoutException(String format, Object... args) {
        super(String.format(format, args));
    }
}
