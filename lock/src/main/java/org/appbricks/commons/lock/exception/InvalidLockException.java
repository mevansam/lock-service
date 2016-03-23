package org.appbricks.commons.exception;

import org.appbricks.commons.model.DistributedLock;

/**
 * Exception thrown if lock is not valid (i.e. owned by current process and thread)
 */
public class InvalidLockException
    extends LockException {

    public InvalidLockException(String format, Object... args) {
        super(String.format(format, args));
    }
}

