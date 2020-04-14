package io.github.pflima92.poc.kafka;


import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public abstract class AbstractVertxProcessor<K, V> extends AbstractProcessor<K, V> {

  @Setter
  @Getter
  protected Vertx vertx;

  @Override
  public void init(ProcessorContext context) {
    super.init(context);
    configure();
  }

  protected void configure() {
  }

  protected <K, V> KeyValueStore<K, V> getStateStore(String stateStoreName) {
    return (KeyValueStore<K, V>) context().getStateStore(stateStoreName);
  }
}
