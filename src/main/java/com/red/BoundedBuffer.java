/**
 * "If the buffer is full, wait until it's not full, then put the object in the buffer and notify all waiting threads."
 *
 * The put() method is synchronized, so only one thread can be in it at a time. The first thing it does is check if the
 * buffer is full. If it is, it calls wait(), which puts the thread to sleep until another thread calls notifyAll(). If the
 * buffer is not full, it puts the object in the buffer and calls notifyAll() to wake up any threads that are waiting
 */
package com.red;

public class BoundedBuffer<T> {
    private final Object[] buffer;
    private int putpos, takepos, count;

    // Creating a new array of objects with the size of the bound.
    public BoundedBuffer(int bound) {
        buffer = new Object[bound];
    }

    /**
     * If the buffer is full, wait until it's not full, then put the object in the buffer and notify all waiting threads.
     *
     * @param object The object to be put into the buffer.
     */
    public synchronized void put(T object) {
        try {
            while (isFull()) {
                wait();
            }
        } catch (InterruptedException e) { e.printStackTrace(); }
        doPut(object); notifyAll();
    }


    /**
     * "Wait until the queue is not empty, then take an element and notify all waiting threads."
     *
     * The wait() method is called on the queue object. This causes the current thread to wait until another thread calls
     * notify() or notifyAll() on the same object
     *
     * @return The first element in the queue.
     */
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

    /**
     * "If the queue is empty, wait until it is not empty, then take the newest element."
     *
     * The function is synchronized, so only one thread can be in it at a time.
     *
     * The first thing it does is check if the queue is empty. If it is, it calls wait(), which puts the thread to sleep
     * until another thread calls notifyAll().
     *
     * If the queue is not empty, it takes the newest element and calls notifyAll() to wake up any threads that are waiting
     *
     * @return The newest element in the queue.
     */
    public synchronized T take_newer() {
        try {
            while (isEmpty()) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        T element = doTake_newer();
        notifyAll();
        return element;
    }

    /**
     * If the count is equal to the length of the buffer, then the buffer is full.
     *
     * @return The value of the count variable.
     */
    public synchronized boolean isFull() {
        return count == buffer.length;
    }

    /**
     * If the count is 0, then the queue is empty.
     *
     * @return The boolean value of whether the count is equal to 0.
     */
    public synchronized boolean isEmpty() {
        return count == 0;
    }

    /**
     * If the buffer is full, wait until it's not full, then put the object in the buffer.
     *
     * @param object The object to be put into the buffer.
     */
    protected synchronized void doPut(T object) {
        buffer[putpos] = object;
        if (++putpos == buffer.length) {
            putpos = 0;
        }
        ++count;
    }

    /**
     * > The function takes the last element in the buffer and decrements the putpos and count variables
     *
     * @return The last element in the buffer.
     */
    protected synchronized T doTake_newer() {
        T element = (T) buffer[putpos-1];
        --putpos;
        --count;
        return element;
    }

    /**
     * The function takes the element at the current take position, increments the take position, and decrements the count
     *
     * @return The element at the current take position.
     */
    protected synchronized T doTake() {
        T element = (T) buffer[takepos];
        if (++takepos == buffer.length) {
            takepos = 0;
        }
        --count;
        return element;
    }
}


