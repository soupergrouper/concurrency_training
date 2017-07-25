package com.tsystems.concurrency;

/**
 * Created by sgorev on 28.03.2017.
 */
public class FutureValueImpl2<V> implements FutureValue<V> {
    private volatile boolean set;

    private V value;

    public boolean trySet(V v) {
        if (set) {
            return false;
        } else {
            synchronized(this) {
                if (set) {
                    return false;
                } else {
                    value = v;
                    set = true;
                    notifyAll();
                    return true;
                }
            }
        }
    }

    public V get() throws InterruptedException {
        if (!set) {
            synchronized(this) {
                while (!set) {
                    wait();
                }
            }
        }
        return value;
    }
}
