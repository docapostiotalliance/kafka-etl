package org.kafka.etl.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.kafka.etl.kafka.IAdditionalConfig;
import org.kafka.etl.kafka.IConsumerManager;
import org.kafka.etl.kafka.IDeserializer;
import org.kafka.etl.load.IPartitionKeyCalculator;
import org.kafka.etl.kafka.IProducerCallback;
import org.kafka.etl.kafka.IProducerManager;
import org.kafka.etl.kafka.ITopicStreamer;
import org.kafka.etl.kafka.impl.AvroToJsonDeserializer;
import org.kafka.etl.kafka.impl.ConsumerManager;
import org.kafka.etl.kafka.impl.DefaultAdditionalConfig;
import org.kafka.etl.kafka.impl.DefaultDeserializer;
import org.kafka.etl.kafka.impl.DefaultPartitionKeyCalculator;
import org.kafka.etl.kafka.impl.DefaultProducerCallback;
import org.kafka.etl.kafka.impl.ProducerManager;
import org.kafka.etl.kafka.impl.TopicStreamer;
import org.kafka.etl.load.ILoad;
import org.kafka.etl.load.KafkaLoader;
import org.kafka.etl.transform.ITransform;
import org.kafka.etl.utils.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kafka.etl.ioc.BindedConstants.GROUP_ID;
import static org.kafka.etl.ioc.BindedConstants.INPUT_TOPIC;
import static org.kafka.etl.ioc.BindedConstants.OUTPUT_TOPIC;
import static org.kafka.etl.ioc.BindedConstants.POLL_TIMEOUT;
import static org.kafka.etl.utils.PropertiesUtils.fromJson;

public class EtlContext extends AbstractModule {
  private static final Logger LOGGER = LoggerFactory.getLogger(EtlContext.class);

  private static final String KEY_KAFKA_CONSUMER_HOST = "kafka.consumer.hosts";
  private static final String KEY_KAFKA_PRODUCER_HOST = "kafka.producer.hosts";
  private static final String KEY_KAFKA_POLL_MAX = "poll.size";
  private static final String KAFKA_SESSION_TIMEOUT = "kafka.session.timeout";
  private static final String KAFKA_REQUEST_TIMEOUT = "kafka.request.timeout";
  private static final String KAFKA_FETCH_RETRIES = "kafka.fetch.retries";

  private static final String KEY_TRANSFORMER = "transformer.class";
  private static final String KEY_TRANSFORM_JAR = "transformer.jar.path";
  private static final String KEY_LOADER = "loader.class";
  private static final String KEY_LOADER_JAR = "loader.jar.path";
  private static final String KEY_GROUP_ID = "group.id";
  private static final String KEY_INPUT_TOPIC = "topic.input";
  private static final String KEY_OUTPUT_TOPIC = "topic.output";
  private static final String KEY_POLL_TIMEOUT = "poll.timeout";
  private static final String KEY_CONSUMER_RECORD_SIZE = "consumer.record.size";
  private static final String KEY_FAIL_ON_EMPTY_JSON = "fail.on.empty.json";
  private static final String KEY_AVRO_DATA_BYTES_START_OFFSET = "avro.data.bytes.start.offset";
  private static final String KEY_PRODUCER_RECORD_SIZE = "producer.record.size";
  private static final String KEY_JSON_AVRO_SCHEMA = "avro.json.schema.path";
  private static final String KEY_PARTITION_KEY_CALCULATOR = "partition.key.calculator.class";
  private static final String KEY_PARTITION_KEY_CALCULATOR_JAR =
      "partition.key.calculator.jar.path";

  private static final String MSG_ERR_BAD_CLASS_TPL = "%s is not an instance of %s";
  private static final String MSG_ERR_INSTANCIATE_TRANSFORM_CLASS_TPL =
      "Error when trying to instanciate %s : e.type = %s, e.msg = %s";

  private static final String FILE_URL_PREFIX = "file:";

  private Vertx vertx;
  private JsonObject properties;

  public EtlContext(Vertx vertx) {
    this.vertx = vertx;
  }

  private IProducerManager createProducerManager(IAdditionalConfig additionalConfig) {
    String kafkaHost = properties.getString(KEY_KAFKA_PRODUCER_HOST);
    Integer requestTimeout = properties.getInteger(KAFKA_REQUEST_TIMEOUT);
    Integer fetchRetries = properties.getInteger(KAFKA_FETCH_RETRIES);

    LOGGER.info(
        "[EtlContext][createProducerManager] creating producer with kafkaHosts = {}, requestTimeout = {}, fetchRetries = {}",
        kafkaHost,
        requestTimeout,
        fetchRetries);
    return new ProducerManager(kafkaHost,
        requestTimeout,
        fetchRetries,
        additionalConfig.producerAdditionalConfig());
  }

  private IConsumerManager createConsumerManager(IDeserializer keyDeserializer,
                                                 IDeserializer valueDeserializer) {
    String kafkaHost = properties.getString(KEY_KAFKA_CONSUMER_HOST);
    Integer requestTimeout = properties.getInteger(KAFKA_REQUEST_TIMEOUT);
    Integer sessionTimeout = properties.getInteger(KAFKA_SESSION_TIMEOUT);
    Integer pollSize = properties.getInteger(KEY_KAFKA_POLL_MAX);

    LOGGER.info(
        "[EtlContext][createConsumerManager] creating consumer with kafkaHosts = {}, requestTimeout = {}, sessionTimeout = {}, pollSize = {}",
        kafkaHost,
        requestTimeout,
        sessionTimeout,
        pollSize);
    return new ConsumerManager(kafkaHost,
        requestTimeout,
        sessionTimeout,
        pollSize,
        keyDeserializer,
        valueDeserializer);
  }

  private ILoad createLoader(JsonObject properties) {
    String className = properties.getString(KEY_LOADER);
    String jarPath = properties.getString(KEY_LOADER_JAR);

    try {
      Class<?> clazz = null;

      if (isBlank(jarPath)) {
        clazz = Class.forName(className);
      } else {
        String jarUrl = jarPath.startsWith(FILE_URL_PREFIX) ? jarPath : FILE_URL_PREFIX + jarPath;
        URLClassLoader child =
            new URLClassLoader(new URL[] {new URL(jarUrl)}, this.getClass().getClassLoader());
        clazz = Class.forName(className, true, child);
      }

      Constructor<?> constructor = clazz.getConstructor();
      Object instance = constructor.newInstance();

      if (!(instance instanceof ILoad)) {
        throw new IllegalArgumentException(
            String.format(MSG_ERR_BAD_CLASS_TPL, className, ILoad.class.getSimpleName()));
      }

      return ((ILoad) instance).init(fromJson(properties));
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | MalformedURLException e) {
      throw new IllegalArgumentException(String.format(MSG_ERR_INSTANCIATE_TRANSFORM_CLASS_TPL,
          className,
          e.getClass().getSimpleName(),
          e.getMessage()));
    }
  }

  private ITransform createTransformer() {
    String className = properties.getString(KEY_TRANSFORMER);
    String jarPath = properties.getString(KEY_TRANSFORM_JAR);

    try {
      Object instance = getInstance(className, jarPath);

      if (!(instance instanceof ITransform)) {
        throw new IllegalArgumentException(
            String.format(MSG_ERR_BAD_CLASS_TPL, className, ITransform.class.getSimpleName()));
      }

      return (ITransform) instance;
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | MalformedURLException e) {
      throw new IllegalArgumentException(String.format(MSG_ERR_INSTANCIATE_TRANSFORM_CLASS_TPL,
          className,
          e.getClass().getSimpleName(),
          e.getMessage()));
    }
  }

  private IPartitionKeyCalculator getPartitionKeyCalculatorInstance() {
    String className = properties.getString(KEY_PARTITION_KEY_CALCULATOR);
    String jarPath = properties.getString(KEY_PARTITION_KEY_CALCULATOR_JAR);

    if (StringUtils.isBlank(className) || StringUtils.isBlank(jarPath)) {
      return new DefaultPartitionKeyCalculator();
    }

    try {
      Object instance = getInstance(className, jarPath);

      if (!(instance instanceof IPartitionKeyCalculator)) {
        throw new IllegalArgumentException(String.format(MSG_ERR_BAD_CLASS_TPL, className));
      }

      return (IPartitionKeyCalculator) instance;
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | MalformedURLException e) {
      throw new IllegalArgumentException(String.format(MSG_ERR_INSTANCIATE_TRANSFORM_CLASS_TPL,
          className,
          e.getClass().getSimpleName(),
          e.getMessage()));
    }
  }

  private Object getInstance(String className, String jarPath) throws ClassNotFoundException,
      MalformedURLException,
      NoSuchMethodException,
      InstantiationException,
      IllegalAccessException,
      InvocationTargetException {

    Class<?> clazz = null;

    if (isBlank(jarPath)) {
      clazz = Class.forName(className);
    } else {
      String jarUrl = jarPath.startsWith(FILE_URL_PREFIX) ? jarPath : FILE_URL_PREFIX + jarPath;
      URLClassLoader child =
          new URLClassLoader(new URL[] {new URL(jarUrl)}, this.getClass().getClassLoader());
      clazz = Class.forName(className, true, child);
    }

    Constructor<?> constructor = clazz.getConstructor();
    return constructor.newInstance();
  }

  private Optional<String> searchAvroSchema() {
    String avroSchemaPath = properties.getString(KEY_JSON_AVRO_SCHEMA, EMPTY);
    if (isBlank(avroSchemaPath) || !FileHelper.existFile(avroSchemaPath)) {
      return Optional.empty();
    }

    String avroSchemaContent = FileHelper.file2stringQuietly(avroSchemaPath);
    if (isBlank(avroSchemaContent)) {
      return Optional.empty();
    }

    return Optional.of(avroSchemaContent);
  }

  @Override
  protected void configure() {
    requireNonNull(vertx, "vertx must not be null");

    properties = vertx.getOrCreateContext().config();
    requireNonNull(properties, "properties must not be null");

    LOGGER.info("[EtlContext][configure] loading the following configuration : {}",
        properties.toString());
    bind(JsonObject.class).toInstance(properties);

    bindConstant().annotatedWith(Names.named(GROUP_ID)).to(properties.getString(KEY_GROUP_ID));
    bindConstant().annotatedWith(Names.named(INPUT_TOPIC))
        .to(properties.getString(KEY_INPUT_TOPIC));
    String outputTopic = properties.getString(KEY_OUTPUT_TOPIC);
    if (isNotBlank(outputTopic)) {
      bindConstant().annotatedWith(Names.named(OUTPUT_TOPIC)).to(outputTopic);
    }
    bindConstant().annotatedWith(Names.named(POLL_TIMEOUT))
        .to(properties.getInteger(KEY_POLL_TIMEOUT));

    Integer producerRecordSize = properties.getInteger(KEY_PRODUCER_RECORD_SIZE);
    Integer consumerRecordSize = properties.getInteger(KEY_CONSUMER_RECORD_SIZE);
    Integer avroDataBytesStartOffset = properties.getInteger(KEY_AVRO_DATA_BYTES_START_OFFSET, 0);
    Boolean failOnEmptyJson = properties.getBoolean(KEY_FAIL_ON_EMPTY_JSON, true);

    IAdditionalConfig config = new DefaultAdditionalConfig.Builder()
        .consumerRecordSize(consumerRecordSize).producerRecordSize(producerRecordSize).build();
    bind(IAdditionalConfig.class).toInstance(config);
    bind(IProducerManager.class).toInstance(createProducerManager(config));
    Optional<String> avroSchema = searchAvroSchema();
    bind(
        IConsumerManager.class)
            .toInstance(
                createConsumerManager(new DefaultDeserializer(),
                    avroSchema.isPresent()
                        ? new AvroToJsonDeserializer(avroSchema.get(),
                            avroDataBytesStartOffset,
                            failOnEmptyJson)
                        : new DefaultDeserializer()));
    bind(ITransform.class).toInstance(createTransformer());
    bind(IProducerCallback.class).to(DefaultProducerCallback.class);
    bind(IPartitionKeyCalculator.class).toInstance(getPartitionKeyCalculatorInstance());

    String loaderClassName = properties.getString(KEY_LOADER);
    String loaderJarPath = properties.getString(KEY_LOADER_JAR);

    if (isNotBlank(outputTopic)) {
      bind(ILoad.class).to(KafkaLoader.class);
    } else if (isNotBlank(loaderClassName) && isNotBlank(loaderJarPath)) {
      bind(ILoad.class).toInstance(createLoader(properties));
    } else {
      bind(ILoad.class).toInstance(new ILoad.Default());
    }

    bind(ITopicStreamer.class).to(TopicStreamer.class);
  }
}
