package com.tsystems.concurrency;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by sgorev on 16.05.2017.
 */
public class LoadableCacheImpl<V> implements LoadableCache {

    Loader loader;
    Long timeout;
    Map<String, CacheEntry> cache = new ConcurrentHashMap<String, CacheEntry>();
    ScheduledThreadPoolExecutor resetExecutor = new ScheduledThreadPoolExecutor(1);

    LoadableCacheImpl (Long timeout, Loader loader) {
        this.timeout = timeout;
        this.loader = loader;
        init();
    }

    LoadableCacheImpl (Long timeout) {
        this.timeout = timeout;
        this.loader = new DefaultLoader();
        init();
    }

    @Override
    public Object get(String key) throws InterruptedException {
        System.out.println("Thread " + Thread.currentThread().getName() + " started");
        if (cache.get(key) == null)
                cache.put(key, new CacheEntry());
        CacheEntry entry = cache.get(key);
        entry.getLock().lock();
            try {
                if (!entry.getValid().get() && !entry.getLoading().get()) {
                    entry.getLoading().set(true);
                    entry.setValue((V) loader.load(key));
                    entry.getValid().set(true);
                    entry.setTimestamp(new Date());
                    System.out.println(entry.getTimestamp());
                    entry.getLoading().set(false);
                    entry.getWaitingLoading().signalAll();
                }
                if (entry.getLoading().get()) {
                    entry.getWaitingLoading().await();
                }
                return entry.getValue();
            }
            catch(NoSuchElementException e) {
                if (cache.get(key) != null) {
                    entry.getLock().unlock();
                    cache.remove(key);
                }
                return null;
            }
            finally {
                if (cache.get(key) != null)
                    entry.getLock().unlock();
                System.out.println("Thread " + Thread.currentThread().getName() + " terminated");
            }

    }

    private void init() {
        Runnable resetTask = () -> {
            Calendar expiryDate =  Calendar.getInstance();
            expiryDate.add(Calendar.SECOND, new Long(timeout / -1000L).intValue());
            cache.values().stream().filter(entry -> entry.getTimestamp() != null && entry.getTimestamp().before(expiryDate.getTime())
                    && entry.getValid().get())
                    .forEach(entry -> {
                        entry.getLock().lock();
                        try {
                            System.out.println("Invalidating value " + entry.getValue());
                            entry.setValid(new AtomicBoolean(false));
                        } finally {
                            entry.getLock().unlock();
                        }
                    });
        };
        resetExecutor.scheduleAtFixedRate(resetTask, 0, timeout / 5, TimeUnit.MILLISECONDS);
    }

    @Override
    public void reset(String key)  {
        cache.get(key).setValid(new AtomicBoolean(false));
    }

    public class DefaultLoader implements Loader<Integer> {
        Map<String, Integer> values = new HashMap<>();

        DefaultLoader() {
            values.put("first", 1);
            values.put("second", 2);
            values.put("third", 3);
        }

        @Override
        public Integer load(String key) throws InterruptedException, NoSuchElementException {
            System.out.println("Loading value " + key + "...");
            //Time-consuming loading emulation
            Thread.sleep(1000);
            if (values.get(key) == null)
                throw new NoSuchElementException();
            return values.get(key);
        }
    }

    private class CacheEntry {
        AtomicBoolean valid = new AtomicBoolean();
        AtomicBoolean loading = new AtomicBoolean();
        V value;
        Lock lock;
        Condition waitingLoading;
        Date timestamp;

        CacheEntry() {
            valid.set(false);
            loading.set(false);
            lock = new ReentrantLock();
            waitingLoading = lock.newCondition();
        }

        public AtomicBoolean getValid() {
            return valid;
        }

        public void setValid(AtomicBoolean valid) {
            this.valid = valid;
        }

        public AtomicBoolean getLoading() {
            return loading;
        }

        public void setLoading(AtomicBoolean loading) {
            this.loading = loading;
        }


        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public Lock getLock() {
            return lock;
        }

        public void setLock(Lock lock) {
            this.lock = lock;
        }

        public Condition getWaitingLoading() {
            return waitingLoading;
        }

        public void setWaitingLoading(Condition waitingLoading) {
            this.waitingLoading = waitingLoading;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

    }
}
