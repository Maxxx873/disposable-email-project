package com.disposableemail.apache.james.mailet.collector.subscriber;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceCollectorSubscriber<T> implements Subscriber<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceCollectorSubscriber.class);

    @Override
    public void onSubscribe(final Subscription s) {
        s.request(Long.MAX_VALUE);
        LOGGER.info("onSubscribe done");
    }

    @Override
    public void onNext(final T t) {
        LOGGER.info("Exist: {}", t);
    }

    @Override
    public void onError(final Throwable t) {
        LOGGER.error("Failed");
        t.printStackTrace();
    }

    @Override
    public void onComplete() {
        LOGGER.info("Completed");
    }

}
