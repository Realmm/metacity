package org.metacity.database;

import java.util.concurrent.CountDownLatch;

/**
 * A latch is a simplified {@link CountDownLatch}
 * However it only needs to be counted down once, and the await exception is handled
 */
public class Latch extends CountDownLatch {

    public Latch() {
        super(1);
    }

    /**
     * Await for the latch to be counted down
     */
    @Override
    public void await() {
        try {
            super.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
