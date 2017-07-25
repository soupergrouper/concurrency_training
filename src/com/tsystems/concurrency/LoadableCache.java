package com.tsystems.concurrency;

/**
 * This interface is intended to represent lazy-cache which
 * is able to load its value on demand using Loader<V>.
 *
 * If multiple threads requests a value only single load shoud
 * be triggered and the other getters should wait till it is done.
 *
 * There should be periodical task, which checks the cache and
 * resets/invalidates the values which are too old (parametrised
 * via constructor or whatever).
 */
public interface LoadableCache<V> {

  /**
   * Gets a value if present, otherwise - loads it or waits till it is loaded.
   */
  V get(String key) throws InterruptedException;

  /**
   * Invalidates the key mapped value so that next get will retrigger load.
   */
  void reset(String key);

  /**
   * This is the interface for the loader per se.
   */
  interface Loader<V> {

    V load(String key) throws InterruptedException;

  }

}
