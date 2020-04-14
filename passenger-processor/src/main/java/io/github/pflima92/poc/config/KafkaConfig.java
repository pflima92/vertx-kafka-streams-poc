package io.github.pflima92.poc.config;

import java.util.Properties;
import lombok.experimental.UtilityClass;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.streams.StreamsConfig;

@UtilityClass
public class KafkaConfig {

  private final static String BOOTSTRAP_SERVERS = System.getenv("KAFKA_BOOTSTRAP_SERVERS");

  public Properties kafkaAdminClientConfig() {
    Properties config = new Properties();
    config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    return config;
  }

  public Properties kafkaStreamsConfig() {
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "passenger-processor-v1");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    config.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/passenger-processor-v1");
    config.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);
    config.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 8);
    return config;
  }


}
