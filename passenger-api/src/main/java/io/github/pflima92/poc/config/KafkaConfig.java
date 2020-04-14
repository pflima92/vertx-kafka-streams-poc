package io.github.pflima92.poc.config;

import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import java.util.Properties;
import lombok.experimental.UtilityClass;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;

@UtilityClass
public class KafkaConfig {

  private final static String BOOTSTRAP_SERVERS = System.getenv("KAFKA_BOOTSTRAP_SERVERS");

  public Properties kafkaStreamsConfig() {
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "passenger-api-v1");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    config.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/passenger-api-v1");
    config.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
    config.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
    return config;
  }

  public Properties kafkaProducerConfig() {
    Properties config = new Properties();
    config.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class.getName());
    config.put(ProducerConfig.ACKS_CONFIG, "1");
    return config;
  }
}
