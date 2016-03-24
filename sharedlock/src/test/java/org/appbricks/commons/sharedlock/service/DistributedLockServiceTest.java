package org.appbricks.commons.sharedlock.service;

import org.appbricks.commons.sharedlock.LockTestContext;
import org.appbricks.commons.sharedlock.data.DistributedLockRepository;
import org.appbricks.commons.sharedlock.exception.InvalidLockException;
import org.appbricks.commons.sharedlock.exception.LockTimedoutException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { LockTestContext.class })
public class DistributedLockServiceTest {

    @Autowired
    DistributedLockRepository distributedLockRepository;

    @Autowired
    DistributedLockService distributedLockService;

    @Test
    public void testConcurrentLocking()
        throws Throwable {

        DistributedLockInstance testLock = new DistributedLockInstance(
            "testLock1", this.distributedLockService, 500, 3);
        ReentrantLock exclusivityCheck = new ReentrantLock();

        ThreadPoolExecutor threadPool =
            new ThreadPoolExecutor(10, 10, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

        List<LockTester> lockTesters = Arrays.asList(
            new LockTester("process-A1", testLock, 60, 60, 0, 5, exclusivityCheck),
            new LockTester("process-A2", testLock, 60, 60, 0, 5, exclusivityCheck),
            new LockTester("process-A3", testLock, 60, 60, 5, 5, exclusivityCheck),
            new LockTester("process-A4", testLock, 60, 60, 5, 5, exclusivityCheck)
        );
        lockTesters.forEach(lt -> threadPool.execute(lt));

        threadPool.shutdown();
        threadPool.awaitTermination(5, TimeUnit.MINUTES);

        for (LockTester lt : lockTesters) {
            lt.validate();
        }
    }

    @Test
    public void testLockTimeout()
        throws Throwable {

        DistributedLockInstance testLock = new DistributedLockInstance("testLock2", this.distributedLockService, 500, 3);
        ReentrantLock exclusivityCheck = new ReentrantLock();

        ThreadPoolExecutor threadPool =
                new ThreadPoolExecutor(10, 10, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

        LockTester b1 = new LockTester("process-B1", testLock, 60, 60, 0, 15, exclusivityCheck);
        LockTester b2 = new LockTester("process-B2", testLock, 10, 60, 2, 5, exclusivityCheck);
        threadPool.execute(b1);
        threadPool.execute(b2);

        threadPool.shutdown();
        threadPool.awaitTermination(5, TimeUnit.MINUTES);

        b1.validate();

        try {
            b2.validate();
            fail("Lock 'process-B2' did not timeout as expected");
        } catch (LockTimedoutException e) {
        }
    }

    @Test
    public void testLockExpiration()
        throws Throwable {

        DistributedLockInstance testLock = new DistributedLockInstance("testLock3", this.distributedLockService, 500, 3);
        ReentrantLock exclusivityCheck = new ReentrantLock();

        ThreadPoolExecutor threadPool =
                new ThreadPoolExecutor(10, 10, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

        LockTester c1 = new LockTester("process-C1", testLock, 60, 10, 0, 15, null);
        LockTester c2 = new LockTester("process-C2", testLock, 10, 60, 2, 5, null);
        threadPool.execute(c1);
        threadPool.execute(c2);

        threadPool.shutdown();
        threadPool.awaitTermination(5, TimeUnit.MINUTES);

        try {
            c1.validate();
            fail("Lock 'process-C1' did not expire as expected");
        } catch (InvalidLockException e) {
        }

        c2.validate();
    }

    private class LockTester
        implements Runnable {

        private Throwable executionException = null;

        private String name;
        private DistributedLockInstance lock;

        private int timeout;
        private int expireAfter;

        private int startDelay;
        private int execDelay;

        private ReentrantLock exclusivityCheck;

        LockTester(String name, DistributedLockInstance lock,
            int timeout, int expireAfter,
            int startDelay, int execDelay,
            ReentrantLock exclusivityCheck ) {

            this.name = name;
            this.lock = lock;
            this.timeout = timeout;
            this.expireAfter = expireAfter;
            this.startDelay = startDelay;
            this.execDelay = execDelay;
            this.exclusivityCheck = exclusivityCheck;
        }

        public void validate()
            throws Throwable {

            if (this.executionException != null) {
                throw this.executionException;
            }
        }

        @Override
        public void run() {

            try {
                Thread.sleep(this.startDelay * 1000);

                System.out.println(String.format(
                    "==> '%s' acquiring sharedlock.", this.name));

                this.lock.lock(this.timeout, this.expireAfter);

                if (this.exclusivityCheck != null) {
                    if (this.exclusivityCheck.isLocked()) {
                        throw new Exception("Distributed sharedlock is already held by another process/thread.");
                    }
                    this.exclusivityCheck.lock();
                }

                System.out.println(String.format(
                    "==> '%s' acquired sharedlock, processing for %d seconds.", this.name, this.execDelay));

                this.lock.validate();
                Thread.sleep(this.execDelay * 1000);
                this.lock.validate();

                this.lock.unlock();

                if (this.exclusivityCheck != null) {
                    this.exclusivityCheck.unlock();
                }

            } catch (Throwable t) {

                System.out.println(String.format(
                    "==> '%s failed with exception: %s", this.name, t.getMessage()));
                t.printStackTrace();

                this.executionException = t;
            }
        }
    }
}
