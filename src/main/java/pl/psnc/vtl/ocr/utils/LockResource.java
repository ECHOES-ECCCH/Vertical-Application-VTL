package pl.psnc.vtl.ocr.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.locks.Lock;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LockResource implements AutoCloseable {
    private final Lock lock;

    public LockResource lock() {
        lock.lock();
        return this;
    }

    @Override
    public void close() {
        lock.unlock();
    }

    public static LockResource withLock(Lock lock) {
        return new LockResource(lock).lock();
    }
}
