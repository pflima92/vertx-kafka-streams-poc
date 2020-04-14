package io.github.pflima92.poc;

import io.github.pflima92.poc.config.KafkaConfig;
import io.github.pflima92.poc.routes.PassengerRouter;
import io.github.pflima92.poc.routes.ReceiverRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Objects;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

@Slf4j
public class MainVerticle extends AbstractVerticle {

  private KafkaStreams streams;

  @Override
  public void start(Future<Void> startFuture) {

    CompositeFuture.all(
      startPassengerAsyncMap().future(),
      startKafkaStreams().future(),
      startWebServer().future())
      .onComplete(ar -> {
        startFuture.complete();
      });
  }

  private Promise startPassengerAsyncMap() {
    Promise promise = Promise.promise();
    vertx.deployVerticle(PassengerLoaderVerticle.class.getName(), promise);
    return promise;
  }

  private Promise<Void> startWebServer() {
    Promise<Void> startWebServerPromise = Promise.promise();
    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.get("/status").handler(ctx -> ctx.response().end());

    ReceiverRouter.create(vertx, router).bind();
    PassengerRouter.create(vertx, router).bind();

    server.requestHandler(router).listen(8080, ar -> {
      log.info("API service listening on port 8080");
      startWebServerPromise.complete();
    });
    return startWebServerPromise;
  }

  private Promise<Void> startKafkaStreams() {
    Promise<Void> startKafkaStreamsPromise = Promise.promise();
    vertx.executeBlocking(promise -> {
      log.info("Initializing KafkaStreams");
      Topology topology = TopologyProvider.create(vertx).provide();
      Properties streamsConfig = KafkaConfig.kafkaStreamsConfig();
      streams = new KafkaStreams(topology, streamsConfig);
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

  @Override
  public void stop() {
    if (Objects.nonNull(streams) && streams.state().isRunning()) {
      streams.close();
    }
  }

  public void exceptionHandler(Thread thread, Throwable throwable) {
    log.error("Fail processing processor thread {}", thread.getName(), throwable);
  }
}
