package org.appbricks.commons.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.appbricks.commons.exception.LockException;
import org.appbricks.commons.exception.LockTimedoutException;
import org.appbricks.commons.exception.LockUpdateException;
import org.appbricks.commons.exception.MaintenanceEnabledException;
import org.appbricks.commons.model.DistributedLock;

import java.net.InetAddress;

/**
 * Used to manage distributed locks to achieve mutual
 * exclusion across multiple services instances.
 */
public class DistributedLockInstance {

    private static final Log log = LogFactory.getLog(DistributedLockInstance.class);

    private String name;
    private DistributedLockService service;

    private long lockCheckInterval;
    private int lockRetries;

    public DistributedLockInstance(String name, DistributedLockService service, long lockCheckInterval, int lockRetries) {
        this.name = name;
        this.service = service;
        this.lockCheckInterval = lockCheckInterval;
        this.lockRetries = lockRetries;

        // Ensure lock is created in database
        service.getLock(this.name);
    }

    public DistributedLock getLock() {
        return this.service.getLock(this.name);
    }

    public void lock(int timeout, int expireAfter)
        throws LockException {

        long timeoutAt = System.currentTimeMillis() + (timeout * 1000);

        DistributedLock lock = new DistributedLock();
        lock.setName(name);

        try {
            InetAddress ip = InetAddress.getLocalHost();

            lock.setHostname(ip.getHostName());
            lock.setAddress(ip.getHostAddress());

            Thread thread = Thread.currentThread();
            lock.setThreadId(thread.getId());
            lock.setThreadName(thread.getName());

            ThreadGroup threadGroup = thread.getThreadGroup();
            lock.setThreadGroupName(threadGroup.getName());

        } catch (Exception e) {
            throw new LockException(e, "Error retrieving metadata for lock '%s': %s", name, e.getMessage());
        }

        this.acquireLockWithRetry(lock, timeoutAt, expireAfter, this.lockRetries);
    }

    private void acquireLockWithRetry(DistributedLock lock, long timeoutAt, int expireAfter, int retryCount)
        throws  LockException {

        String name = lock.getName();
        boolean success = false;

        try {
            while (!(success = service.acquireLock(lock, expireAfter)) && System.currentTimeMillis() < timeoutAt) {
                Thread.sleep(this.lockCheckInterval);
            }

        } catch (MaintenanceEnabledException e) {
            throw e;

        } catch (InterruptedException e) {
            throw new LockException(e, "Lock wait was interrupted.");

        } catch (Throwable t) {

            if (retryCount > 0) {
                log.info(String.format("Retrying due to failure to acquire lock: %s", lock));
                this.acquireLockWithRetry(lock, timeoutAt, expireAfter, retryCount - 1);
            } else {
                throw new LockUpdateException(t, "Failed to acquire lock: %s", lock);
            }
        }

        // Check if timeout has expired
        if (!success && System.currentTimeMillis() >= timeoutAt) {
            throw new LockTimedoutException("Timed out waiting for lock %s", name);
        }
    }

    public void validate()
        throws LockException {

        this.service.validate(this.name);
    }

    public void unlock()
        throws LockException {

        this.service.unlock(this.name);
    }

    public void enterMaintenanceWindow(int timeout, int hours, int minutes)
        throws LockException {

        this.lock(timeout, (hours * 3600) + (minutes * 60));
        this.service.enterMaintenanceMode(this.name);
    }


    public void exitMaintenanceWindow()
        throws LockException {

        this.service.exitMaintenanceMode(this.name);
    }
}
