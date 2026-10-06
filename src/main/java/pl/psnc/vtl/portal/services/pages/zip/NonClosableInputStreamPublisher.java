package pl.psnc.vtl.portal.services.pages.zip;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.function.Supplier;

public class NonClosableInputStreamPublisher implements HttpRequest.BodyPublisher {

    private final Supplier<? extends InputStream> streamSupplier;


    public NonClosableInputStreamPublisher(Supplier<? extends InputStream> streamSupplier) {
        this.streamSupplier = (Supplier) Objects.requireNonNull(streamSupplier);
    }


    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        InputStream is = (InputStream) this.streamSupplier.get();
        PullPublisher publisher;
        if (is == null) {
            Throwable t = new IOException("streamSupplier returned null");
            publisher = new PullPublisher((Iterable) null, t);
        } else {
            publisher = new PullPublisher(this.iterableOf(is), (Throwable) null);
        }

        publisher.subscribe(subscriber);
    }


    protected Iterable<ByteBuffer> iterableOf(InputStream is) {
        return () -> {
            return new NonClosableInputStreamPublisher.StreamIterator(is);
        };
    }


    public long contentLength() {
        return -1L;
    }


    public static class StreamIterator implements Iterator<ByteBuffer> {

        final InputStream is;
        final Supplier<? extends ByteBuffer> bufSupplier;
        volatile ByteBuffer nextBuffer;
        volatile boolean need2Read;
        volatile boolean haveNext;


        StreamIterator(InputStream is) {
            this(is, () -> ByteBuffer.allocate(16384));
        }


        StreamIterator(InputStream is, Supplier<? extends ByteBuffer> bufSupplier) {
            this.need2Read = true;
            this.is = is;
            this.bufSupplier = bufSupplier;
        }


        private int read() {
            this.nextBuffer = (ByteBuffer) this.bufSupplier.get();
            this.nextBuffer.clear();
            byte[] buf = this.nextBuffer.array();
            int offset = this.nextBuffer.arrayOffset();
            int cap = this.nextBuffer.capacity();

            try {
                int n = this.is.read(buf, offset, cap);
                if (n == -1) {
                    return -1;
                } else {
                    this.nextBuffer.limit(n);
                    this.nextBuffer.position(0);
                    return n;
                }
            } catch (IOException var5) {
                return -1;
            }
        }


        public synchronized boolean hasNext() {
            if (this.need2Read) {
                this.haveNext = this.read() != -1;
                if (this.haveNext) {
                    this.need2Read = false;
                }

                return this.haveNext;
            } else {
                return this.haveNext;
            }
        }


        public synchronized ByteBuffer next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            } else {
                this.need2Read = true;
                return this.nextBuffer;
            }
        }
    }

}

