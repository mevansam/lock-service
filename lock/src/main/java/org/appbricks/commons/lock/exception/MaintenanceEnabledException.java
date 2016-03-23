package org.appbricks.commons.exception;

/**
 * Exception thrown if lock is in maintenance mode.
 */
public class MaintenanceEnabledException
    extends LockException {

    public MaintenanceEnabledException(String format, Object... args) {
        super(String.format(format, args));
    }

}
