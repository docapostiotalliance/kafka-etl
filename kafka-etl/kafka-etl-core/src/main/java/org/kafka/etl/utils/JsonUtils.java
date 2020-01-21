package org.kafka.etl.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JsonUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

  public static final ObjectMapper MAPPER = initObjectMapper();
  public static final ObjectMapper FLAT_MAPPER = initFlatObjectMapper();

  private static ObjectMapper initFlatObjectMapper() {
    ObjectMapper objectMapper = initObjectMapper();
    objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
    return objectMapper;
  }

  private JsonUtils() {
    // Prevent class instanciation
  }

  public static ObjectMapper initObjectMapper() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module()).registerModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
        .setDateFormat(new ISO8601DateFormat())
        .setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false))
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    return objectMapper;
  }

  public static ObjectMapper getMapper() {
    return MAPPER;
  }

  public static String toJson(final Object o) {
    try {
      return MAPPER.writeValueAsString(o);
    } catch (IOException ioe) {
      String msg = "Error convert to json from object " + o.toString();
      throw new IllegalStateException(msg, ioe);
    }
  }

  public static String toJson(final Object o, Class<?> type) {
    try {
      JavaType javaType = MAPPER.getTypeFactory().constructType(type);
      return MAPPER.writerFor(javaType).writeValueAsString(o);
    } catch (IOException ioe) {
      String msg = "Error convert to json from object " + o.toString();
      throw new IllegalStateException(msg, ioe);
    }
  }

  public static String toJsonQuietly(final Object o) {
    if (null != o) {
      return toJson(o);
    }

    return "null";
  }

  /**
   * ToJson with potential filtered field list.
   */
  @JsonRawValue
  public static String toJsonWithFilterList(final Object o, final String filteredFields) {
    if (null == o) {
      return StringUtils.EMPTY;
    }

    String[] arrayFilteredFields = null;
    if (StringUtils.isNotEmpty(filteredFields) && filteredFields.contains(",")) {
      arrayFilteredFields = filteredFields.split(",");
    } else if (StringUtils.isNotEmpty(filteredFields)) {
      arrayFilteredFields = new String[1];
      arrayFilteredFields[0] = filteredFields;
    }

    if (null != arrayFilteredFields && arrayFilteredFields.length >= 1) {
      return toJsonFiltered(o, arrayFilteredFields);
    } else {
      return toJson(o);
    }
  }

  public static String toJsonFiltered(final Object o, final String[] includeFieldNames) {
    try {

      SimpleFilterProvider filter = new SimpleFilterProvider();
      filter.addFilter("customFilter",
          SimpleBeanPropertyFilter.filterOutAllExcept(includeFieldNames));

      ObjectWriter writer = MAPPER.writer(filter);
      return writer.writeValueAsString(o);
    } catch (IOException ioe) {
      String msg = "Error convert to json from object " + o.toString();
      throw new IllegalStateException(msg, ioe);
    }
  }

  public static String toJsonExclusionFiltered(final Object o, final String[] excludeFieldNames) {
    try {

      SimpleFilterProvider filter = new SimpleFilterProvider();
      filter.addFilter("customFilter2",
          SimpleBeanPropertyFilter.serializeAllExcept(excludeFieldNames));

      filter.addFilter("excludeFieldsFilter",
          SimpleBeanPropertyFilter.serializeAllExcept(excludeFieldNames));

      ObjectWriter writer = MAPPER.writer(filter);
      return writer.writeValueAsString(o);
    } catch (IOException ioe) {
      String msg = "Error convert to json from object " + o.toString();
      throw new IllegalStateException(msg, ioe);
    }
  }

  public static <T> T fromJson(final String input, final Class<T> resourceClass) {
    try {
      return MAPPER.readValue(input, resourceClass);
    } catch (IOException ioe) {
      String msg =
          "Error converting from json {" + input + "} " + "to object " + resourceClass.getName();
      throw new IllegalStateException(msg, ioe);
    }
  }

  public static Map<String, String> toStringMap(final Object jsonObject) {
    return MAPPER.convertValue(jsonObject, new TypeReference<Map<String, String>>() {
    });
  }

  public static Map<String, String> toStringMap(final String jsonString) {
    return fromJson(jsonString, new TypeReference<Map<String, String>>() {
    });
  }

  public static Map<String, Object> toObjectMap(final String jsonString) {
    return fromJson(jsonString, new TypeReference<Map<String, Object>>() {
    });
  }

  public static List<Map<String, Object>> toListObjectMap(final String jsonString) {
    return fromJson(jsonString, new TypeReference<List<Map<String, Object>>>() {
    });
  }

  public static List<Map<String, String>> toListofStringMap(final String jsonString) {
    return fromJson(jsonString, new TypeReference<List<Map<String, String>>>() {
    });
  }

  public static Map<String, Object> toMap(final Object jsonObject) {
    return MAPPER.convertValue(jsonObject, new TypeReference<Map<String, Object>>() {
    });
  }

  public static List<String> toStringList(final String jsonString) {
    return fromJson(jsonString, new TypeReference<List<String>>() {
    });
  }

  public static String[] toStringArray(final String jsonString) {
    return fromJson(jsonString, new TypeReference<String[]>() {
    });
  }

  public static String[][] toStringMatrix(final String jsonString) {
    return fromJson(jsonString, new TypeReference<String[][]>() {
    });
  }

  public static <T> T fromJson(final String input, final TypeReference<T> expectedType) {
    try {
      return MAPPER.readValue(input, expectedType);
    } catch (IOException ioe) {
      String msg =
          "Error convert from json {" + input + "} " + "to object " + expectedType.toString();
      throw new IllegalStateException(msg, ioe);
    }
  }

  public static String findValue(final String input, final String key) {
    try {
      JsonNode node = getMapper().readValue(input, JsonNode.class);
      return node.findValue(key).asText();
    } catch (IOException ioe) {
      final String msg = "Impossible to find '" + key + "' in {" + input + "}";
      throw new IllegalStateException(msg, ioe);
    }
  }

  /**
   * Try to transform a string input to Map, if it is ok return this map, otherwise, try to
   * transform it to a list of map
   */
  public static Map<String, String> transformMap(String json) {
    final ObjectMapper mapper = new ObjectMapper();
    boolean continu = false;
    Map<String, String> transformedJsonMap = new HashMap<>();
    try {
      transformedJsonMap = mapper.readValue(json, new TypeReference<Map<String, String>>() {
      });
      continu = true;
    } catch (IOException ioe) {
      LOGGER.info("cannot transform json to Map, maybe it is a list of map..");
    }

    if (continu) {
      return transformedJsonMap;
    }

    List<Map<String, String>> transformedJsonListOfMap = new ArrayList<>();
    try {
      transformedJsonListOfMap =
          mapper.readValue(json, new TypeReference<List<Map<String, String>>>() {
          });
    } catch (IOException ioe) {
      LOGGER.error("cannot transform json to List of Map", ioe);
    }

    final Map<String, String> valuesMap = new HashMap<>();
    if (CollectionUtils.isEmpty(transformedJsonListOfMap)) {
      LOGGER.error("La liste listHashValue est vide");
      throw new RuntimeException("There is no value to the output of the decoding rule");
    }

    for (final Map<String, String> map : transformedJsonListOfMap) {
      valuesMap.putAll(map);
    }

    if (CollectionUtils.sizeIsEmpty(valuesMap)) {
      throw new RuntimeException(
          "Json is null, La liste des données brutes transformées est vide !");
    }
    return valuesMap;
  }

  public static Map<String, Map<String, String>> toMapOfMap(String json) {
    final ObjectMapper mapper = new ObjectMapper();
    Map<String, Map<String, String>> transformedJsonListOfMap = new HashMap<>();

    try {
      transformedJsonListOfMap =
          mapper.readValue(json, new TypeReference<Map<String, Map<String, String>>>() {
          });
    } catch (IOException ioe) {
      LOGGER.error("cannot transform json to Map of Map", ioe);
    }

    if (CollectionUtils.sizeIsEmpty(transformedJsonListOfMap)) {
      LOGGER.error("Then resulting map is empty");
      throw new RuntimeException("There is no value to the output of the decoding rule");
    }

    return transformedJsonListOfMap;
  }

  public static <T> T fromJson(String data, Type type) {
    try {
      JavaType javaType = JsonUtils.getMapper().getTypeFactory().constructType(type);
      return JsonUtils.getMapper().readValue(data, javaType);
    } catch (IOException ioe) {
      final String msg = "Error convert from json {" + data + "} to object " + type.toString();
      throw new IllegalArgumentException(msg, ioe);
    }
  }

  public static <T> T fromJson(Map<String, Object> map, Type type) {
    return fromJson(toJson(map), type);
  }

  public static String toJson(Map<String, Object> map) {
    try {
      return MAPPER.writeValueAsString(map);
    } catch (IOException ioe) {
      final String msg = "Error convert " + map + " to json string";
      throw new IllegalArgumentException(msg, ioe);

    }
  }

  public static String toFlatJson(Map<String, Object> map) {
    try {
      return FLAT_MAPPER.writeValueAsString(map);
    } catch (IOException ioe) {
      final String msg = "Error convert " + map + " to json string";
      throw new IllegalArgumentException(msg, ioe);

    }
  }

}

