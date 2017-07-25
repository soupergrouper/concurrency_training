package com.tsystems.concurrency;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FutureValueImpl<V> implements FutureValue<V> {

    private AtomicBoolean set = new AtomicBoolean(false);
    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    private V value;

    public boolean trySet(V v) {
        if (!set.get()) {
            lock.lock();
            try {
                value = v;
                set.set(true);
                condition.signalAll();
            } finally {
                lock.unlock();
            }
            return true;
        }
     else {
            return false;
        }
    }

    public V get() throws InterruptedException {
        if (!set.get()) {
            lock.lock();
            try {
                while (!set.get()) {
                    condition.await();
                }
            }
            finally {
                lock.unlock();
            }
        }
        return value;
    }

}
