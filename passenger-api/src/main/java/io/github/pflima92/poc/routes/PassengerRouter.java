package io.github.pflima92.poc.routes;

import io.github.pflima92.poc.config.MongoConfig;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class PassengerRouter {

  private final Vertx vertx;
  private final Router router;
  private final MongoClient mongoClient;

  public PassengerRouter(Vertx vertx, Router router) {
    this.vertx = vertx;
    this.router = router;
    this.mongoClient = MongoClient.createShared(vertx, MongoConfig.config());
  }

  public static PassengerRouter create(Vertx vertx, Router router) {
    return new PassengerRouter(vertx, router);
  }

  public void bind() {
    router.get("/passengers").handler(this::findPassengers);
    router.get("/passengers/:id").handler(this::findPassengerByKey);
    router.get("/passengers/cached").handler(this::findCachedPassengers);
    router.get("/metrics").handler(this::countMetrics);
  }

  private void findPassengerByKey(RoutingContext ctx) {
    String key = ctx.request().getParam("id");
    vertx.sharedData().<String, JsonObject>getAsyncMap("passenger", ar -> {
      ar.result().get(key, ar2 -> {

        if (ar2.failed()) {
          ctx.response().setStatusCode(409)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("message", ar.cause().getMessage()).toString());
          return;
        }

        ctx.response()
          .putHeader("content-type", "application/json")
          .end(ar2.result().toString());
      });
    });
  }

  private void countMetrics(RoutingContext ctx) {
    vertx.sharedData().<String, JsonObject>getAsyncMap("passenger", ar -> {
      ar.result().size(ar2 -> {

        mongoClient.count("passeneger", new JsonObject(), arDb -> {

          ctx.response()
            .putHeader("content-type", "application/json")
            .end(new JsonObject()
              .put("countAsyncMap", ar2.result())
              .put("countDb", arDb.result())
              .toString()
            );
        });
      });
    });
  }

  private void findCachedPassengers(RoutingContext ctx) {
    vertx.sharedData().<String, JsonObject>getAsyncMap("passenger", ar -> {
      ar.result().values(ar2 -> {
        ctx.response()
          .putHeader("content-type", "application/json")
          .end(new JsonArray(ar2.result()).toString());
      });
    });
  }

  private void findPassengers(RoutingContext ctx) {

    mongoClient.find("passenger", new JsonObject(), ar -> {
      if (ar.failed()) {
        ctx.response().setStatusCode(500).end("Fail to process due to " + ar.cause().getMessage());
        return;
      }

      ctx.response()
        .putHeader("content-type", "application/json")
        .end(ar.result().toString());
    });
  }
}
