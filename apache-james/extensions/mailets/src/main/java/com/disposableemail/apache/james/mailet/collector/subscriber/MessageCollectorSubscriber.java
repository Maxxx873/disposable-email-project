package com.disposableemail.apache.james.mailet.collector.subscriber;

import com.disposableemail.apache.james.mailet.collector.pojo.Account;
import com.disposableemail.apache.james.mailet.collector.pojo.MailMessage;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class MessageCollectorSubscriber implements Subscriber<Account> {

    private final MongoCollection<MailMessage> messageCollection;
    private final MailMessage message;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageCollectorSubscriber.class);

    @Override
    public void onSubscribe(final Subscription subscription) {
        subscription.request(1);
        LOGGER.info("{} subscription done", MessageCollectorSubscriber.class.getSimpleName());
    }

    @Override
    public void onNext(final Account account) {
        LOGGER.info("Received Id for Account {} | Id: {}", account.getAddress(), account.getId());
        message.setAccountId(account.getId().toString());
        messageCollection.insertOne(message).subscribe(new SourceCollectorSubscriber<>());
    }

    @Override
    public void onError(final Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void onComplete() {
        LOGGER.info("Done");
    }

}
