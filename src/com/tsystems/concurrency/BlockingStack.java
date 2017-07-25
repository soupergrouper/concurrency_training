package com.tsystems.concurrency;

public interface BlockingStack<V> {

  /**
   * Put value to the stack if it is not full. Otherwise - wait for room.
   */
  void push(V val) throws InterruptedException;

  /**
   * Get value from the stack if it is not empty. Otherwise - wait for one.
   */
  V pop() throws InterruptedException;

}
