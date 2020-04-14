package io.github.pflima92.poc.processors;

import io.github.pflima92.poc.kafka.AbstractVertxProcessor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.To;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class PassengerRawProcessor extends AbstractVertxProcessor<String, JsonObject> {

  public static final String PROCESSOR_NAME = "PassengerRawProcessor";

  private KeyValueStore<String, JsonObject> passengerStateStore;


  @Override
  protected void configure() {
    passengerStateStore = getStateStore(PassengerStateStoreProcessor.STATE_STORE_NAME);
  }

  @Override
  public void process(String key, JsonObject passenger) {
    log.info("Processing inbound - Passenger k=[{}] v=[{}]", key, passenger.toString());

    // Merge Passenger
    JsonObject oldPassenger = Optional.ofNullable(passengerStateStore.get(key))
      .orElse(new JsonObject().put("_id", key));

    JsonObject mergedPassenger = mergePassenger(oldPassenger, passenger);

    vertx.eventBus().send("passenger-processed", mergedPassenger);

    context().forward(key, mergedPassenger, To.child("passenger-sink"));
  }

  private JsonObject mergePassenger(JsonObject old, JsonObject passenger) {
    Instant now = LocalDateTime.now().toInstant(ZoneOffset.UTC);

    JsonObject merged = passenger.mergeIn(old);

    // events
    String currentEvent = passenger.getString("event", "Unknown");
    JsonArray events = merged.getJsonArray("events", new JsonArray());
    events.add(new JsonObject()
      .put("event", currentEvent)
      .put("timestamp", now)
    );

    merged.put("events", events);

    if (!merged.containsKey("created_at")) {
      merged.put("created_at", now);
    }
    merged.put("last_change", now);
    return merged;
  }
}
