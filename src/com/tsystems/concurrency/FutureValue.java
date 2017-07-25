package com.tsystems.concurrency;

public interface FutureValue<V> {

    /**
     * Sets future value once. If value already set - return false.
     */
    boolean trySet(V value);

    /**
     * Return value if set, or wait till set.
     */
    V get() throws InterruptedException;

}
