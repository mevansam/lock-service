package org.appbricks.commons.sharedlock.exception;

/**
 * Exception thrown if sharedlock is not valid (i.e. owned by current process and thread)
 */
public class InvalidLockException
    extends LockException {

    public InvalidLockException(String format, Object... args) {
        super(String.format(format, args));
    }
}

