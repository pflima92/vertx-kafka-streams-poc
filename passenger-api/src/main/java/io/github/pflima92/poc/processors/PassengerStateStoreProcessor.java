package io.github.pflima92.poc.processors;

import io.github.pflima92.poc.kafka.AbstractVertxProcessor;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class PassengerStateStoreProcessor extends AbstractVertxProcessor<String, JsonObject> {

  public final static String STATE_STORE_NAME = "passenger-state-store";
  public final static String PROCESSOR_NAME = "PassengerStateStoreProcessor";

  private KeyValueStore<String, JsonObject> stateStore;

  @Override
  public void init(ProcessorContext context) {
    stateStore = (KeyValueStore<String, JsonObject>) context.getStateStore(STATE_STORE_NAME);
  }

  @Override
  public void process(String key, JsonObject value) {
    stateStore.put(key, value);
    vertx.sharedData().<String, JsonObject>getAsyncMap("passenger", ar -> {
      ar.result().put(key, value, ar2 -> {
        // do nothing
      });
    });
  }
}
