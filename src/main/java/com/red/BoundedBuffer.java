package com.red;

public class BoundedBuffer<T> {
    private final Object[] buffer;
    private int putpos, takepos, count;

    public BoundedBuffer(int bound) {
        buffer = new Object[bound];
    } /* * This method blocks until buffer becomes empty */

    public synchronized void put(T object) {
        try {
            while (isFull()) {
                wait();
            }
        } catch (InterruptedException e) { e.printStackTrace(); }
        doPut(object); notifyAll();
    } /* * This method blocks until the buffer becomes full */


    public synchronized T take() {
        try {
            while (isEmpty()) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        T element = doTake();
        notifyAll();
        return element;
    }

    public synchronized boolean isFull() {
        return count == buffer.length;
    }

    public synchronized boolean isEmpty() {
        return count == 0;
    }

    protected synchronized void doPut(T object) {
        buffer[putpos] = object;
        if (++putpos == buffer.length) {
            putpos = 0;
        }
        ++count;
    }

    protected synchronized T doTake() {
        T element = (T) buffer[takepos];
        if (++takepos == buffer.length) {
            takepos = 0;
        } --count; return element;
    }
}


