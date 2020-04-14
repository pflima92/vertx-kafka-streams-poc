package io.github.pflima92.poc;

import io.github.pflima92.poc.config.MongoConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PassengerLoaderVerticle extends AbstractVerticle {

  private MongoClient mongoClient;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    this.mongoClient = MongoClient.createShared(vertx, MongoConfig.config());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    mongoClient.find("passenger", new JsonObject(), ar -> {
      if (ar.failed()) {
        log.error("Fail to load passenger cache", ar.cause());
        startPromise.fail(ar.cause());
        return;
      }

      List<JsonObject> passengers = ar.result();
      vertx.sharedData().<String, JsonObject>getAsyncMap("passenger", ar2 -> {
        AsyncMap<String, JsonObject> passengersAyncMap = ar2.result();
        List<Future> futures = passengers.stream().map(p -> {
          Promise<Void> promise = Promise.promise();
          String key = p.getString("_id");
          passengersAyncMap.put(key, p, promise);
          return promise.future();
        }).collect(Collectors.toList());

        CompositeFuture.all(futures).onComplete(cAr -> {
          if (cAr.failed()) {
            startPromise.fail(cAr.cause());
            return;
          }
          log.info("Loaded {} passenger to asyncMap", passengers.size());
          startPromise.complete();
        });
      });
    });
  }
}
