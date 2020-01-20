package org.kafka.etl.ioc;

import com.google.inject.AbstractModule;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import static java.util.Objects.requireNonNull;

public class EtlContext extends AbstractModule {
    protected Vertx vertx;
    protected JsonObject properties;

    public EtlContext(Vertx vertx) {
        this.vertx = vertx;
    }

    protected void configure() {
        requireNonNull(vertx, "vertx must not be null");

        properties = vertx.getOrCreateContext().config();
        requireNonNull(properties, "properties must not be null");
        bind(JsonObject.class).toInstance(properties);
    }
}
