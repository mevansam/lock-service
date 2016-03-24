package org.appbricks.commons.sharedlock.data;

import org.appbricks.commons.sharedlock.model.DistributedLock;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.LockModeType;

public interface DistributedLockRepository
    extends CrudRepository<DistributedLock, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    DistributedLock findByName(String name);
}
