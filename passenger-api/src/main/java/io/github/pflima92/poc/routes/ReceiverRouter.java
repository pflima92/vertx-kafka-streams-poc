package io.github.pflima92.poc.routes;

import io.github.pflima92.poc.config.KafkaConfig;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiverRouter {

  private final Vertx vertx;
  private final Router router;
  private final KafkaProducer<String, JsonObject> producer;

  public ReceiverRouter(Vertx vertx, Router router) {
    this.vertx = vertx;
    this.router = router;
    this.producer = KafkaProducer.create(vertx, KafkaConfig.kafkaProducerConfig());
  }

  public static ReceiverRouter create(Vertx vertx, Router router) {
    return new ReceiverRouter(vertx, router);
  }

  public void bind() {
    router.post("/receiver/passenger").handler(this::receivePassenger);
  }

  private void receivePassenger(RoutingContext ctx) {

    JsonObject passenger = ctx.getBodyAsJson();
    String key = passenger.getString("passport");

    KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord
      .create("passenger-raw", key, passenger);
    producer.send(record, ar -> {
      if (ar.failed()) {
        ctx.response().setStatusCode(500).end("Fail to process due to " + ar.cause().getMessage());
        return;
      }
      log.info("Sent key=[{}] to passenger-raw ", key);
      ctx.response()
        .putHeader("content-type", "application/json")
        .end(passenger.toString());
    });
  }

}
