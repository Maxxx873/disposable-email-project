package com.disposableemail.apache.james.mailet.collector.subscriber;

import static com.disposableemail.apache.james.mailet.collector.BasicMailCollector.getMailSource;
import static com.mongodb.client.model.Filters.eq;

import java.time.Instant;

import javax.mail.internet.MimeMessage;

import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.disposableemail.apache.james.mailet.collector.pojo.Account;
import com.disposableemail.apache.james.mailet.collector.pojo.MailMessage;
import com.disposableemail.apache.james.mailet.collector.pojo.Source;
import com.mongodb.reactivestreams.client.MongoCollection;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Builder
@RequiredArgsConstructor
public class MessageCollectorSubscriber implements Subscriber<Account> {

    private final MongoCollection<MailMessage> messageCollection;
    private final MongoCollection<Account> accountCollection;
    private final MongoCollection<Source> sourceCollection;
    private final MailMessage message;
    private final MimeMessage mimeMessage;
    private final long messageSize;
    private final boolean enableUsedSizeUpdating;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageCollectorSubscriber.class);

    @Override
    public void onSubscribe(final Subscription subscription) {
        subscription.request(1);
        LOGGER.info("{} subscription done", MessageCollectorSubscriber.class.getSimpleName());
    }

    @Override
    public void onNext(final Account account) {
        LOGGER.info("Received Id for Account {} | Id: {}", account.getAddress(), account.getId());
        long increasedSize = messageSize + account.getUsed();
        message.setAccountId(account.getId().toString());
        var insertMessage = messageCollection.insertOne(message);
        var insertSource = sourceCollection.insertOne(getMailSource(mimeMessage));

        if (increasedSize <= account.getQuota() && enableUsedSizeUpdating) {
            Document updateAccountDoc = new Document("$set", new Document()
                    .append("used", increasedSize)
                    .append("updatedAt", Instant.now()));
            var updateAccount = accountCollection.updateOne(eq("address", account.getAddress()), updateAccountDoc);
            Flux.merge(insertMessage, updateAccount, insertSource)
                    .doOnNext(data -> LOGGER.info("Received data for Account {} | Id: {} | {}", account.getAddress(),
                            account.getId(), data.toString()))
                    .subscribe(new SourceCollectorSubscriber<>());
        } else {
            Flux.merge(insertMessage, insertSource)
                    .doOnNext(data -> LOGGER.info("Received data for Account {} | Id: {} | {}", account.getAddress(),
                            account.getId(), data.toString()))
                    .subscribe(new SourceCollectorSubscriber<>());
        }
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
