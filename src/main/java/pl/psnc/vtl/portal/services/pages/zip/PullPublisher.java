package pl.psnc.vtl.portal.services.pages.zip;

import java.util.Iterator;
import java.util.concurrent.Flow;

public class PullPublisher<T> implements Flow.Publisher<T> {

    private final Iterable<T> iterable;
    private final Throwable throwable;


    PullPublisher(Iterable<T> iterable, Throwable throwable) {
        this.iterable = iterable;
        this.throwable = throwable;
    }


    PullPublisher(Iterable<T> iterable) {
        this(iterable, (Throwable) null);
    }


    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        PullPublisher.Subscription sub;
        if (this.throwable != null) {
            assert this.iterable == null : "non-null iterable: " + this.iterable;

            sub = new PullPublisher.Subscription(subscriber, (Iterator) null, this.throwable);
        } else {
            assert this.throwable == null : "non-null exception: " + this.throwable;

            sub = new PullPublisher.Subscription(subscriber, this.iterable.iterator(), (Throwable) null);
        }

        subscriber.onSubscribe(sub);
        if (this.throwable != null) {
            sub.pullScheduler.runOrSchedule();
        }

    }


    private class Subscription implements java.util.concurrent.Flow.Subscription {

        private final Flow.Subscriber<? super T> subscriber;
        private final Iterator<T> iter;
        private volatile boolean completed;
        private volatile boolean cancelled;
        private volatile Throwable error;
        final SequentialScheduler pullScheduler = new SequentialScheduler(new PullPublisher.Subscription.PullTask());
        private final Demand demand = new Demand();


        Subscription(Flow.Subscriber<? super T> subscriber, Iterator<T> iter, Throwable throwable) {
            this.subscriber = subscriber;
            this.iter = iter;
            this.error = throwable;
        }


        public void request(long n) {
            if (!this.cancelled) {
                if (n <= 0L) {
                    this.error = new IllegalArgumentException("illegal non-positive request:" + n);
                } else {
                    this.demand.increase(n);
                }

                this.pullScheduler.runOrSchedule();
            }
        }


        public void cancel() {
            this.cancelled = true;
        }


        final class PullTask extends SequentialScheduler.CompleteRestartableTask {

            PullTask() {
            }


            protected void run() {
                if (!PullPublisher.Subscription.this.completed && !PullPublisher.Subscription.this.cancelled) {
                    Throwable t = PullPublisher.Subscription.this.error;
                    if (t != null) {
                        PullPublisher.Subscription.this.completed = true;
                        PullPublisher.Subscription.this.pullScheduler.stop();
                        PullPublisher.Subscription.this.subscriber.onError(t);
                    } else {
                        while (PullPublisher.Subscription.this.demand.tryDecrement()
                                && !PullPublisher.Subscription.this.cancelled && PullPublisher.Subscription.this.iter
                                .hasNext()) {
                            PullPublisher.Subscription.this.subscriber
                                    .onNext(PullPublisher.Subscription.this.iter.next());
                        }

                        if (!PullPublisher.Subscription.this.iter.hasNext()
                                && !PullPublisher.Subscription.this.cancelled) {
                            PullPublisher.Subscription.this.completed = true;
                            PullPublisher.Subscription.this.pullScheduler.stop();
                            PullPublisher.Subscription.this.subscriber.onComplete();
                        }

                    }
                }
            }
        }
    }
}
