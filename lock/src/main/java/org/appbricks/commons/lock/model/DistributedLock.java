package org.appbricks.commons.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A distributed Lock shared across multiple processes.
 */
@Entity
public class DistributedLock {

    @Id
    @Column(length=50)
    private String name;

    @Column(length=50)
    private String hostname;
    @Column(length=15)
    private String address;

    @Column(length=10)
    private long threadId;
    @Column(length=50)
    private String threadName;
    @Column(length=50)
    private String threadGroupName;

    private char maintenance = 'N';
    private char locked;
    private long updatedTime;
    private long expiresAt;

    private static final long TIME_ORIGIN = (new Date(0L)).getTime();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss z");

    static {
        DistributedLock.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public DistributedLock() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadGroupName() {
        return threadGroupName;
    }

    public void setThreadGroupName(String threadGroupName) {
        this.threadGroupName = threadGroupName;
    }

    public void enableMaintenance() {
        this.maintenance = 'Y';
    }

    public void disableMaintenance() {
        this.maintenance = 'N';
    }

    public boolean isMaintenanceMode() {
        return this.maintenance=='Y';
    }

    public boolean isLocked() {
        return locked=='Y' && System.currentTimeMillis() < this.expiresAt;
    }

    public Date getUpdateTime() {
        return new Date(this.updatedTime);
    }

    public Date getExpirationTime() {
        return new Date(this.expiresAt);
    }

    public void lock(DistributedLock distributedLock, int expireAfter) {
        this.hostname = distributedLock.hostname;
        this.address = distributedLock.address;
        this.threadName = distributedLock.threadName;
        this.threadGroupName = distributedLock.threadGroupName;
        this.threadId = distributedLock.threadId;
        this.updatedTime = System.currentTimeMillis();
        this.expiresAt = System.currentTimeMillis() + (expireAfter * 1000);
        this.maintenance = 'N';
        this.locked = 'Y';
    }

    public void clear() {
        this.hostname = StringUtils.EMPTY;
        this.address = StringUtils.EMPTY;
        this.threadName = StringUtils.EMPTY;
        this.threadGroupName = StringUtils.EMPTY;
        this.threadId = -1;
        this.updatedTime = System.currentTimeMillis();
        this.expiresAt = TIME_ORIGIN;
        this.maintenance = 'N';
        this.locked = 'F';
    }

    @Override
    public boolean equals(Object o) {

        if (o != null && o instanceof DistributedLock) {

            DistributedLock distributedLock = (DistributedLock) o;
            return this.hostname != null && this.hostname.equals(distributedLock.hostname) &&
                this.address != null && this.address.equals(distributedLock.address) &&
                this.threadName != null && this.threadName.equals(distributedLock.threadName) &&
                this.threadGroupName != null && this.threadGroupName.equals(distributedLock.threadGroupName) &&
                this.threadId == distributedLock.threadId;
        }
        return false;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("DistributedLock(");
        sb.append("name=").append(this.name).append(", ");;
        sb.append("hostname=").append(this.hostname).append(", ");
        sb.append("address=").append(this.address).append(", ");
        sb.append("threadName=").append(this.threadName).append(", ");
        sb.append("threadGroupName=").append(this.threadGroupName).append(", ");
        sb.append("threadId=").append(this.threadId).append(", ");
        sb.append("expiresAt=").append(DistributedLock.dateFormat.format(new Date(this.expiresAt))).append(", ");
        sb.append("locked=").append(this.locked).append(")");
        return sb.toString();
    }
}
