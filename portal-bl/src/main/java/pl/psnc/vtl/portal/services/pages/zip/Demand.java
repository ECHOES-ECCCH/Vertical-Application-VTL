package pl.psnc.vtl.portal.services.pages.zip;

import java.util.concurrent.atomic.AtomicLong;

public final class Demand {

    private final AtomicLong val = new AtomicLong();


    public Demand() {
    }


    public boolean increase(long n) {
        if (n <= 0L) {
            throw new IllegalArgumentException(String.valueOf(n));
        } else {
            long prev = this.val.getAndAccumulate(n, (p, i) -> {
                return p + i < 0L ? 9223372036854775807L : p + i;
            });
            return prev == 0L;
        }
    }


    public boolean increaseIfFulfilled() {
        return this.val.compareAndSet(0L, 1L);
    }


    public long decreaseAndGet(long n) {
        if (n <= 0L) {
            throw new IllegalArgumentException(String.valueOf(n));
        } else {
            long p;
            long d;
            do {
                d = this.val.get();
                p = Math.min(d, n);
            } while (!this.val.compareAndSet(d, d - p));

            return p;
        }
    }


    public boolean tryDecrement() {
        return this.decreaseAndGet(1L) == 1L;
    }


    public boolean isFulfilled() {
        return this.val.get() == 0L;
    }


    public void reset() {
        this.val.set(0L);
    }


    public long get() {
        return this.val.get();
    }


    public String toString() {
        return String.valueOf(this.val.get());
    }
}

