package org.kafka.etl.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import com.google.inject.AbstractModule;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.kafka.etl.kafka.IConsumerManager;
import org.kafka.etl.kafka.IProducerManager;
import org.kafka.etl.kafka.impl.ConsumerManager;
import org.kafka.etl.kafka.impl.ProducerManager;
import org.kafka.etl.transform.ITransform;

import static java.util.Objects.requireNonNull;

public class EtlContext extends AbstractModule {
  private static final String KEY_KAFKA_CONSUMER_HOST = "kafka.consumer.hosts";
  private static final String KEY_KAFKA_POLL_MAX = "kafka.poll.max";
  private static final String KAFKA_SESSION_TIMEOUT = "kafka.session.timeout";
  private static final String KAFKA_REQUEST_TIMEOUT = "kafka.request.timeout";
  private static final String KAFKA_FETCH_RETRIES = "kafka.kafkaFetchRetries";

  private static final String KEY_TRANSFORMER = "transformer.class";

  private static final String MSG_ERR_BAD_CLASS_TPL = "%s is not an instance of ITransform";
  private static final String MSG_ERR_INSTANCIATE_TRANSFORM_CLASS_TPL =
      "Error when trying to instanciate %s : e.type = %s, e.msg = %s";

  private Vertx vertx;
  private JsonObject properties;

  public EtlContext(Vertx vertx) {
    this.vertx = vertx;
  }

  private IProducerManager createProducerManager() {
    return new ProducerManager(properties.getString(KEY_KAFKA_CONSUMER_HOST),
        properties.getInteger(KAFKA_REQUEST_TIMEOUT),
        properties.getInteger(KAFKA_FETCH_RETRIES),
        new HashMap<>());
  }

  private IConsumerManager createConsumerManager() {
    return new ConsumerManager(properties.getString(KEY_KAFKA_CONSUMER_HOST),
        properties.getInteger(KAFKA_REQUEST_TIMEOUT),
        properties.getInteger(KAFKA_SESSION_TIMEOUT),
        properties.getInteger(KEY_KAFKA_POLL_MAX));
  }

  private ITransform createTransformer() {
    String className = properties.getString(KEY_TRANSFORMER);

    try {
      Class<?> clazz = Class.forName(className);
      Constructor<?> constructor = clazz.getConstructor();
      Object instance = constructor.newInstance();

      if (!(instance instanceof ITransform)) {
        throw new IllegalArgumentException(String.format(MSG_ERR_BAD_CLASS_TPL, className));
      }

      return (ITransform) instance;
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new IllegalArgumentException(String.format(MSG_ERR_INSTANCIATE_TRANSFORM_CLASS_TPL,
          className,
          e.getClass().getSimpleName(),
          e.getMessage()));
    }
  }

  protected void configure() {
    requireNonNull(vertx, "vertx must not be null");

    properties = vertx.getOrCreateContext().config();
    requireNonNull(properties, "properties must not be null");
    bind(JsonObject.class).toInstance(properties);

    bind(IProducerManager.class).toInstance(createProducerManager());
    bind(IConsumerManager.class).toInstance(createConsumerManager());
    bind(ITransform.class).toInstance(createTransformer());
  }
}
