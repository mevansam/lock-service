package org.appbricks.commons.lock.data;

import org.appbricks.commons.lock.model.DistributedLock;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.LockModeType;

public interface DistributedLockRepository
    extends CrudRepository<DistributedLock, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    DistributedLock findByName(String name);
}
