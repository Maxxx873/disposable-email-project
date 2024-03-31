package com.disposableemail.apache.james.mailet;

import com.disposableemail.apache.james.mailet.collector.BasicMailCollector;
import com.disposableemail.apache.james.mailet.collector.ReactiveSourceCollector;
import com.disposableemail.apache.james.mailet.collector.SyncSourceCollector;
import com.disposableemail.apache.james.mailet.collector.pojo.Account;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import org.apache.mailet.base.test.FakeMail;
import org.apache.mailet.base.test.FakeMailetConfig;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class CollectorBenchmarkRunner {

    @Benchmark
    public FakeMail reactiveMailet(BenchmarkState state) throws MessagingException {
        state.reactiveMailet.service(state.mail);
        return state.mail;
    }

    @Benchmark
    public FakeMail syncMailet(BenchmarkState state) throws MessagingException {
        state.syncMailet.service(state.mail);
        return state.mail;
    }


    public static void main(String[] args) throws Exception {
        final Options options = new OptionsBuilder()
                .include(CollectorBenchmarkRunner.class.getSimpleName())
                .result("apache-james/extensions/mailets/map-jmh.json").resultFormat(ResultFormatType.JSON).build();
        new Runner(options).run();
    }

    @State(Scope.Thread)
    public static class BenchmarkState {

        protected static final String CONNECTION_STRING = "mongodb://%s:%d";
        protected static final String DATABASE_NAME = "test";
        protected static final String SOURCE_COLLECTION_NAME = "source";
        protected static final String MESSAGE_COLLECTION_NAME = "message";
        protected static final String ACCOUNT_COLLECTION_NAME = "account";
        private static final String IP = "localhost";
        private CodecRegistry pojoCodecRegistry;
        protected BasicMailCollector reactiveMailet;
        protected BasicMailCollector syncMailet;
        protected FakeMailetConfig mailetConfig;
        private com.mongodb.client.MongoDatabase mongoDatabase;
        private com.mongodb.client.MongoClient mongoClient;
        private MimeMessage mimeMessage;
        private FakeMail mail;
        private MongoCollection<Account> accountCollection;
        private static final DockerMongoDB DOCKER_MONGO = new DockerMongoDB();

        @Setup
        public void setup() throws IOException, MessagingException {
            DOCKER_MONGO.start();

            mimeMessage = new MimeMessage(null, new FileInputStream("apache-james/extensions/mailets/src/test/resources/test_mail_html_no_attachments.eml"));
            mail = FakeMail.defaultFakeMail();
            mail.setMessage(mimeMessage);

            pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));

            mailetConfig = FakeMailetConfig.builder()
                    .mailetName("SourceCollector")
                    .setProperty("connectionString", String.format(CONNECTION_STRING, IP, DOCKER_MONGO.getMappedPort()))
                    .setProperty("database", DATABASE_NAME)
                    .setProperty("sourceCollectionName", SOURCE_COLLECTION_NAME)
                    .setProperty("messageCollectionName", MESSAGE_COLLECTION_NAME)
                    .setProperty("accountCollectionName", ACCOUNT_COLLECTION_NAME)
                    .build();

            reactiveMailet = new ReactiveSourceCollector();
            syncMailet = new SyncSourceCollector();
            syncMailet.init(mailetConfig);
            reactiveMailet.init(mailetConfig);

            mongoClient = com.mongodb.client.MongoClients.create(String.format(CONNECTION_STRING, IP, DOCKER_MONGO.getMappedPort()));
            mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);

            accountCollection = mongoDatabase.getCollection(ACCOUNT_COLLECTION_NAME, Account.class).withCodecRegistry(pojoCodecRegistry);
            Account account = new Account();
            account.setAddress("t3@example.com");
            accountCollection.insertOne(account);
        }

        @TearDown
        public void cleanUp() {
            DOCKER_MONGO.stop();
        }
    }
}
