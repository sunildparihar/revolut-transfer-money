package com.revolut.core.fundstransfer.locks;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectsLockManager {

    private static final ConcurrentHashMap<Object, Lock> keys2LockMap = new ConcurrentHashMap<>();
    private static final ObjectsLockManager instance = new ObjectsLockManager();

    private ObjectsLockManager() {
    }

    public static ObjectsLockManager getInstance() {
        return instance;
    }

    public Lock getLock(Object key) {
        Lock lock = keys2LockMap.get(key);
        if (Objects.isNull(lock)) {
            lock = new ReentrantLock();
            keys2LockMap.putIfAbsent(key, lock);
        }

        return lock;
    }

    public void lockKey(Object key) {
        Lock lock = getLock(key);
        lock.lock();
    }

    public void lockKeyWithTimeout(Object key, long timeoutInMillis) throws Exception {
        if (timeoutInMillis <=0) {
            throw new Exception("timeout value should be greater than zero");
        }
        Lock lock = getLock(key);
        lock.tryLock(timeoutInMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * This method locks multiple keys without as an atomic operation i.e. either all keys are locked or no key is locked.
     * This method waits until all are locked
     * @param keys
     */
    public void lockMultipleAtomically(Object... keys) {
        Deque<Lock> lockedStack = new ArrayDeque<>();
        boolean isAllLocked;
        do {
            isAllLocked = true;
            try {
                for (Object key : keys) {
                    Lock lock = getLock(key);
                    boolean isLocked = lock.tryLock();
                    if (isLocked) {
                        lockedStack.push(lock);
                    } else {
                        isAllLocked = false;
                        break;
                    }
                }
            } finally {
                if (!isAllLocked) {
                    //unable to lock all the keys, hence release acquired locks immediately to avoid any deadlocks or to re-enter already acquired locks
                    releaseAcquiredLocks(lockedStack);
                }
            }
        } while (!isAllLocked);
    }

    /**
     * This method locks multiple keys as an atomic operation i.e. either all keys are locked or no key is locked
     * This method waits until the timeout has elapsed otherwise exception is thrown.
     * @param keys
     */
    public void lockMultipleAtomicallyWithTimeout(long timeoutInMillis, Object... keys) throws Exception {
        if (timeoutInMillis <=0) {
            throw new Exception("timeout value should be greater than zero");
        }
        Deque<Lock> lockedStack = new ArrayDeque<>();
        boolean isAllLocked = false;
        long startTime = System.currentTimeMillis();
        long timeoutResidual = timeoutInMillis;
        try {
            for (Object key : keys) {
                Lock lock = getLock(key);
                boolean isLocked = lock.tryLock(timeoutResidual, TimeUnit.MILLISECONDS);
                timeoutResidual = timeoutResidual - (System.currentTimeMillis() - startTime);
                if (isLocked) {
                    lockedStack.push(lock);
                } else {
                    throw new Exception("Timeout Occurred");
                }
            }
            isAllLocked = true;
        } finally {
            if (!isAllLocked) {
                //unable to lock all the keys, hence release acquired locks
                releaseAcquiredLocks(lockedStack);
            }
        }
    }

    public void unlockKey(Object key) {
        ReentrantLock lock = (ReentrantLock) getLock(key);
        lock.unlock();
    }

    public void unlockMultiple(Object... keys) throws Exception{
        boolean wasAllLocked = true;
        for (Object key : keys) {
            ReentrantLock lock = (ReentrantLock) getLock(key);
            if (!lock.isLocked()) {
                wasAllLocked = false;
            } else {
                lock.unlock();
            }
        }

        if (!wasAllLocked) {
            throw new Exception("One or more keys were found not locked");
        }

    }

    private void releaseAcquiredLocks(Deque<Lock> lockedStack) {
        Lock lock;
        while (!lockedStack.isEmpty() && (lock = lockedStack.pop()) != null) {
            lock.unlock();
        }
    }

}
