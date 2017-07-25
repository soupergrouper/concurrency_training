package com.tsystems.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by sgorev on 15.05.2017.
 */
public class BlockingStackImpl<V> implements BlockingStack<V> {

    final int max;
    List<V> stack;

    Lock lock = new ReentrantLock();
    Condition notFull = lock.newCondition();
    Condition notEmpty = lock.newCondition();


    BlockingStackImpl(int size) {
        stack = new ArrayList<V>();
        max = size;
    }

    @Override
    public void push(V val) throws InterruptedException {
        lock.lock();
        try {
            if (stack.size() == max) {
                notFull.await();
            }
                stack.add(val);
                notEmpty.signal();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public V pop() throws InterruptedException {
        V retVal = null;
        lock.lock();
        try {
            if (stack.isEmpty()) {
                notEmpty.await();
            }
                retVal = stack.get(0);
                stack.remove(0);
                notFull.signal();
        }
        finally {
            lock.unlock();
        }
        return retVal;
    }

    public void print() {
        stack.forEach(x -> System.out.println(x + "  "));
    }
}
