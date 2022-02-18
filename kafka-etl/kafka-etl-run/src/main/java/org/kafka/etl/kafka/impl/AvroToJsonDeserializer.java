package org.kafka.etl.kafka.impl;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.vertx.core.json.JsonObject;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.kafka.etl.kafka.IDeserializer;
import org.kafka.etl.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class AvroToJsonDeserializer implements IDeserializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(AvroToJsonDeserializer.class);

  public final Schema schema;
  private final DatumReader<?> reader;
  private final Integer dataBytesStartOffset;
  private final boolean failOnEmptyJson;

  public AvroToJsonDeserializer(String jsonSchema,
                                Integer dataBytesStartOffset,
                                boolean failOnEmptyJson) {
    this.schema = new Schema.Parser().parse(jsonSchema);
    this.dataBytesStartOffset = dataBytesStartOffset;
    this.failOnEmptyJson = failOnEmptyJson;
    reader = new ReflectDatumReader<>(schema);
  }

  @Override
  public void configure(Map<String, ?> map, boolean b) {
    // Nothing to do
  }

  @Override
  public String deserialize(String s, byte[] data) {
    LOGGER.debug(
        "[AvroToJsonDeserializer][deserialize] start deserializing the byte array using Avro; schema = {}",
        schema.getFullName());

    if (data == null) {
      LOGGER.debug("[AvroToJsonDeserializer][deserialize] No data received to be deserialized!");
      return null;
    }

    try {
      LOGGER.debug(
          "[AvroToJsonDeserializer][deserialize] Initializing data reader from Avro schema!");
      Object decodedValue = decodeData(data);
      String rtn = decodedValue.toString();

      if (JsonUtils.isValid(rtn)) {
        LOGGER.debug(
            "[AvroToJsonDeserializer][deserialize] decodedValue.toString is a valid JSON String : ",
            rtn);
        if (failOnEmptyJson && isEmptyJson(rtn)) {
          throw new IllegalStateException(
              "[AvroToJsonDeserializer][deserialize] deserializedValue.toString is empty : " + rtn);
        }

        return rtn;
      }

      if (decodedValue instanceof String && JsonUtils.isValid((String) decodedValue)) {
        rtn = (String) decodedValue;
        LOGGER.debug("[AvroToJsonDeserializer][deserialize] decodedValue is a valid JSON String : ",
            rtn);

        if (failOnEmptyJson && isEmptyJson(rtn)) {
          throw new IllegalStateException(
              "[AvroToJsonDeserializer][deserialize] deserializedValue is empty : " + rtn);
        }

        return rtn;
      }

      LOGGER.info(
          "[AvroToJsonDeserializer][deserialize] neither decodedValue.toString or decodedValue are valid JSON String");
      return JsonUtils.toJson(decodedValue);
    } catch (Exception e) {
      LOGGER.error(
          "[AvroToJsonDeserializer][deserialize] Error while deserializing data, e.type = {}, e.msg = {}",
          e.getClass().getSimpleName(),
          e.getMessage());
      throw new IllegalArgumentException(e);
    }
  }

  private Object decodeData(byte[] data) throws IOException {
    try {
      return defaultDataDecoding(stripFirstOffsets(data));
    } catch (ArrayIndexOutOfBoundsException e) {
      // Retry decoding data without strip/shift
      LOGGER.debug("[AvroToJsonDeserializer][deserialize] retry decoding without data strip/shift");
      return defaultDataDecoding(data);
    }
  }

  private Object defaultDataDecoding(byte[] data) throws IOException {
    BinaryDecoder
        decoder =
        DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(data), null);
    return reader.read(null, decoder);
  }

  private boolean isEmptyJson(String json) {
    JsonObject jo = new JsonObject(json);
    return !jo.stream().filter(e -> null != e.getValue() && isNotBlank(e.getValue().toString()))
        .findAny().isPresent();
  }

  public byte[] stripFirstOffsets(byte[] data) {
    return Arrays.copyOfRange(data, dataBytesStartOffset, data.length);
  }

  @Override
  public void close() {
    // Nothing to do
  }
}
