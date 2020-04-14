package io.github.pflima92.poc.config;

import static java.lang.Integer.parseInt;

import io.vertx.core.json.JsonObject;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MongoConfig {

  public JsonObject config() {
    return new JsonObject()
      .put("host", System.getenv("MONGO_HOST"))
      .put("port", parseInt(System.getenv("MONGO_PORT")))
      .put("username", "poc")
      .put("password", "poc")
      .put("db_name", "poc");
  }
}
