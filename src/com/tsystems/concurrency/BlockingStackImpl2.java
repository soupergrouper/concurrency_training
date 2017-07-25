package com.tsystems.concurrency;


public class BlockingStackImpl2<V> implements BlockingStack<V> {

  private int cur;
  private Object[] values;

  private final Object notFull = new Object();
  private final Object notEmpty = new Object();

  public BlockingStackImpl2(int size) {
    this.values = new Object[size];
  }

  public void push(V v) throws InterruptedException {
    synchronized(notFull) {
      synchronized(notEmpty) {
        while (cur == values.length) {
          notFull.wait();
        }
        values[cur++] = v;
        notEmpty.notify();
      }
    }
  }

  @SuppressWarnings("unckecked")
  public V pop() throws InterruptedException {
    synchronized(notFull) {
      synchronized(notEmpty) {
        while (cur == 0) {
          notEmpty.wait();
        }
        V v = (V) values[cur--];
        notFull.notify();
        return v;
      }
    }
  }

}
