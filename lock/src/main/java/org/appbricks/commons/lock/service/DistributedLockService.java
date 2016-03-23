package org.appbricks.commons.lock.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.appbricks.commons.lock.data.DistributedLockRepository;
import org.appbricks.commons.lock.exception.InvalidLockException;
import org.appbricks.commons.lock.exception.LockException;
import org.appbricks.commons.lock.exception.MaintenanceEnabledException;
import org.appbricks.commons.lock.model.DistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.net.InetAddress;

/**
 * Service that uses a database resource
 * to create a mutually exclusive lock
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class DistributedLockService {

    private static final Log log = LogFactory.getLog(DistributedLockService.class);

    private DistributedLockRepository distributedLockRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    public DistributedLockService(DistributedLockRepository distributedLockRepository) {
        this.distributedLockRepository = distributedLockRepository;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public DistributedLock getLock(String name) {

        DistributedLock lock = distributedLockRepository.findByName(name);
        if (lock == null) {
            lock = new DistributedLock();
            lock.setName(name);
            lock.clear();
            this.distributedLockRepository.save(lock);
        }

        return lock;
    }

    public boolean acquireLock(DistributedLock lock, int expireAfter)
        throws LockException {

        String name = lock.getName();

        DistributedLock distributedLock = this.distributedLockRepository.findByName(name);
        if (distributedLock == null) {
            throw new LockException(String.format("Lock '%s' was not found.", name));
        }
        if (distributedLock.isLocked()) {

            if (distributedLock.isMaintenanceMode()) {
                throw new MaintenanceEnabledException("Lock '%s' is in maintenance mode so it cannot be locked.", name);
            }

            this.entityManager.refresh(distributedLock, LockModeType.PESSIMISTIC_WRITE);
            if (distributedLock.isLocked()) {
                log.debug(
                    String.format("Unable to acquire lock as it is locked by another process or thread: %s",
                    distributedLock.toString()));

                return false;
            }
        }

        distributedLock.lock(lock, expireAfter);
        this.distributedLockRepository.save(distributedLock);

        return true;
    }

    public DistributedLock validate(String name)
        throws LockException {

        DistributedLock distributedLock1 = new DistributedLock();

        try {
            InetAddress ip = InetAddress.getLocalHost();

            distributedLock1.setHostname(ip.getHostName());
            distributedLock1.setAddress(ip.getHostAddress());

            Thread thread = Thread.currentThread();
            distributedLock1.setThreadId(thread.getId());
            distributedLock1.setThreadName(thread.getName());

            ThreadGroup threadGroup = thread.getThreadGroup();
            distributedLock1.setThreadGroupName(threadGroup.getName());

        } catch (Exception e) {
            throw new LockException(e, "Error retrieving metadata for lock '%s': %s", name, e.getMessage());
        }

        DistributedLock distributedLock2 = distributedLockRepository.findByName(name);
        if (distributedLock2 == null) {
            throw new LockException("Lock '%s' does not exist.", name);
        }

        if (!distributedLock2.equals(distributedLock1) && distributedLock2.isLocked()) {
            throw new InvalidLockException("Expected locked state: %s, Actual state: %s", distributedLock1, distributedLock2);
        }

        return distributedLock2;
    }

    public void unlock(String name)
        throws LockException {

        DistributedLock distributedLock = this.validate(name);
        distributedLock.clear();
        this.distributedLockRepository.save(distributedLock);
    }

    public void enterMaintenanceMode(String name)
        throws LockException {

        DistributedLock distributedLock = this.validate(name);
        distributedLock.enableMaintenance();
        this.distributedLockRepository.save(distributedLock);
    }

    public void exitMaintenanceMode(String name)
        throws LockException {

        DistributedLock distributedLock = distributedLockRepository.findByName(name);
        if (distributedLock == null) {
            throw new LockException("Lock '%s' does not exist.", name);
        }
        if (!distributedLock.isLocked() && !distributedLock.isMaintenanceMode()) {
            throw new LockException("Lock '%s' is not in maintenance mode", name);
        }
        distributedLock.clear();
        this.distributedLockRepository.save(distributedLock);
    }
}
