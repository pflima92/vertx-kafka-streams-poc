package io.github.pflima92.poc.kafka;


import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;

public abstract class AbstractVertxProcessor<K, V> extends AbstractProcessor<K, V> {

  @Setter
  @Getter
  protected Vertx vertx;

  public void configure(){
  }

  @Override
  public void init(ProcessorContext context) {
    super.init(context);
    configure();
  }
}
