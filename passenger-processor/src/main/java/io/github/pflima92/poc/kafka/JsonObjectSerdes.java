package io.github.pflima92.poc.kafka;

import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.serialization.JsonObjectDeserializer;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class JsonObjectSerdes implements Serde<JsonObject> {

  public static JsonObjectSerdes serde(){
    return new JsonObjectSerdes();
  }

  @Override
  public Serializer<JsonObject> serializer() {
    return new JsonObjectSerializer();
  }

  @Override
  public Deserializer<JsonObject> deserializer() {
    return new JsonObjectDeserializer();
  }
}
