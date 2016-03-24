package org.appbricks.commons.sharedlock.exception;

/**
 * Exception thrown if sharedlock is in maintenance mode.
 */
public class MaintenanceEnabledException
    extends LockException {

    public MaintenanceEnabledException(String format, Object... args) {
        super(String.format(format, args));
    }

}
