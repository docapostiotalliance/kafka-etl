package org.kafka.etl.kafka.impl;

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
import java.util.Arrays;
import java.util.Map;

public class AvroToJsonDeserializer implements IDeserializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(AvroToJsonDeserializer.class);

  public final Schema schema;
  private final DatumReader<?> reader;
  private final Integer dataBytesStartOffset;

  public AvroToJsonDeserializer(String jsonSchema, Integer dataBytesStartOffset) {
    this.schema = new Schema.Parser().parse(jsonSchema);
    this.dataBytesStartOffset = dataBytesStartOffset;
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
      BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(
          new ByteArrayInputStream(Arrays.copyOfRange(data, dataBytesStartOffset, data.length - 1)),
          null);
      return JsonUtils.toJson(reader.read(null, decoder));
    } catch (Exception e) {
      LOGGER.error(
          "AvroToJsonDeserializer][deserialize] Error while deserializing data, e.type = {}, e.msg = {}",
          e.getClass().getSimpleName(),
          e.getMessage());
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public void close() {
    // Nothing to do
  }
}
