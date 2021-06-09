package org.metacity.database;

import java.util.concurrent.CountDownLatch;

/**
 * A simple callback class
 * @param <T> The expected return type of the callback
 */
public abstract class DatabaseResultCallback<T> {

    private CountDownLatch waiter;

    /**
     * Accept the callback, returning a value, triggering the {@code await()} function
     * @param value The value to return
     */
    public final void accept(T value) {
        try {
            onReceived(value);
        } finally {
            if (waiter != null) {
                waiter.countDown();
            }
        }
    }

    /**
     * Triggered once a value is accepted
     * @param value The accepted value
     */
    protected abstract void onReceived(T value);

    /**
     * Await for this callback to be accepted
     */
    public final void await() {
        waiter = new CountDownLatch(1);
        try {
            waiter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
