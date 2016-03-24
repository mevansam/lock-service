package org.appbricks.commons.sharedlock.exception;

/**
 * Exception thrown when waiting for a sharedlock times out.
 */
public class LockTimedoutException
    extends LockException {

    public LockTimedoutException(String format, Object... args) {
        super(String.format(format, args));
    }
}
