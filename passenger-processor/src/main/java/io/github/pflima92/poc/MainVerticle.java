package io.github.pflima92.poc;

import io.github.pflima92.poc.config.KafkaConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.admin.NewTopic;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

@Slf4j
public class MainVerticle extends AbstractVerticle {

  private final static Set<String> REQUIRED_TOPICS;

  static {
    REQUIRED_TOPICS = new HashSet<>();
    REQUIRED_TOPICS.add("passenger-raw");
    REQUIRED_TOPICS.add("passenger");
  }

  @Override
  public void start(Promise<Void> startFuture) {

    vertx.deployVerticle(PassengerWriterVerticle.class.getName(), new DeploymentOptions()
      .setWorker(true)
      .setWorkerPoolSize(12)
    );

    setupKafkaTopics().future().onSuccess(ar -> {
      log.info("Topics configured successfully");
      startKafkaStreams().future().onSuccess(ar2 -> {
        startFuture.complete();
      });
    }).onFailure(e -> {
      startFuture.fail(e);
    });
  }

  private Promise<Void> setupKafkaTopics() {
    Promise<Void> setupKafkaTopicsPromise = Promise.promise();

    KafkaAdminClient kafkaAdminClient = KafkaAdminClient
      .create(vertx, KafkaConfig.kafkaAdminClientConfig());

    kafkaAdminClient.listTopics(ar -> {

      Set<String> topics = ar.result();

      List<NewTopic> listToCreate = REQUIRED_TOPICS.stream().filter(t -> !topics.contains(t))
        .map(t -> new NewTopic()
          .setName(t)
          .setNumPartitions(32)
          .setReplicationFactor((short) 3))
        .collect(Collectors.toList());

      if (listToCreate.isEmpty()) {
        setupKafkaTopicsPromise.complete();
        return;
      }

      kafkaAdminClient.createTopics(listToCreate, ar2 -> {
        if (ar2.failed()) {
          setupKafkaTopicsPromise.fail(ar.cause().getMessage());
          return;
        }
        setupKafkaTopicsPromise.complete();
      });
    });
    return setupKafkaTopicsPromise;
  }

  private Promise<Void> startKafkaStreams() {
    Promise<Void> startKafkaStreamsPromise = Promise.promise();
    vertx.executeBlocking(promise -> {
      log.info("Initializing KafkaStreams");
      Topology topology = TopologyProvider.create(vertx).provide();
      Properties streamsConfig = KafkaConfig.kafkaStreamsConfig();
      KafkaStreams streams = new KafkaStreams(topology, streamsConfig);
      streams.setUncaughtExceptionHandler(this::exceptionHandler);
      streams.start();
      log.info("Topology created: [{}]", topology.describe());
      log.info("Stream has started {}", streams.state());

      promise.complete();
    }, res -> {
      startKafkaStreamsPromise.complete();
    });
    return startKafkaStreamsPromise;
  }

  public void exceptionHandler(Thread thread, Throwable throwable) {
    log.error("Fail processing processor thread {}", thread.getName(), throwable);
  }
}
