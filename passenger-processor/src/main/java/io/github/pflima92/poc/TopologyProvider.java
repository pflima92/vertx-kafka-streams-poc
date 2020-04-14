package io.github.pflima92.poc;

import static io.github.pflima92.poc.kafka.StreamsConstants.PASSENGER_RAW_SOURCE;
import static io.github.pflima92.poc.kafka.StreamsConstants.PASSENGER_SINK;

import io.github.pflima92.poc.kafka.AbstractVertxProcessor;
import io.github.pflima92.poc.kafka.JsonObjectSerdes;
import io.github.pflima92.poc.processors.PassengerRawProcessor;
import io.github.pflima92.poc.processors.PassengerStateStoreProcessor;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

@AllArgsConstructor
public class TopologyProvider {

  private final Vertx vertx;

  public static TopologyProvider create(Vertx vertx) {
    return new TopologyProvider(vertx);
  }

  public Topology provide() {
    Topology topology = new Topology();
    topology.addSource(PASSENGER_RAW_SOURCE,
      Serdes.String().deserializer(), JsonObjectSerdes.serde().deserializer(),
      "passenger-raw"
    );
    topology.addProcessor(
      PassengerRawProcessor.PROCESSOR_NAME, processorSupplier(PassengerRawProcessor.class),
      PASSENGER_RAW_SOURCE
    );
    topology.addSink(PASSENGER_SINK, "passenger",
      Serdes.String().serializer(),
      JsonObjectSerdes.serde().serializer(), PassengerRawProcessor.PROCESSOR_NAME
    );

    StoreBuilder<KeyValueStore<String, JsonObject>> passengerStoreBuilder = Stores
      .keyValueStoreBuilder(
        Stores.persistentKeyValueStore(PassengerStateStoreProcessor.STATE_STORE_NAME),
        Serdes.String(), JsonObjectSerdes.serde()
      ).withLoggingDisabled();

    topology.addGlobalStore(passengerStoreBuilder, "passenger-state-store-source",
      Serdes.String().deserializer(),
      JsonObjectSerdes.serde().deserializer(),
      "passenger",
      PassengerStateStoreProcessor.PROCESSOR_NAME,
      PassengerStateStoreProcessor::new
    );
    return topology;
  }

  @SneakyThrows
  private <K, V> ProcessorSupplier<K, V> processorSupplier(
    Class<? extends AbstractVertxProcessor<K, V>> clazz) {
    return () -> {
      try {
        AbstractVertxProcessor<K, V> processor = clazz.getDeclaredConstructor().newInstance();
        processor.setVertx(vertx);
        return processor;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }
}
