package io.github.pflima92.poc;

import io.github.pflima92.poc.config.MongoConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PassengerWriterVerticle extends AbstractVerticle {

  private MongoClient mongoClient;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    mongoClient = MongoClient.createShared(vertx, MongoConfig.config());
  }

  @Override
  public void start() {
    vertx.eventBus().consumer("passenger-processed", this::handler);
  }

  private void handler(Message<JsonObject> msg) {
    JsonObject passenger = msg.body();
    mongoClient.save("passenger", passenger, ar -> {
      if (ar.failed()) {
        log.error("Fail to save passenger", ar.cause());
        return;
      }
      log.info("Saved successfully [{}]", passenger);
    });
  }
}
