package com.brinqa.nebula.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.value.LossyCoercion;
import org.neo4j.driver.exceptions.value.Uncoercible;
import org.neo4j.driver.internal.value.NullValue;
import org.neo4j.driver.types.Entity;
import org.neo4j.driver.types.IsoDuration;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Point;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.types.Type;
import org.neo4j.driver.types.TypeSystem;

import com.vesoft.nebula.client.graph.data.ResultSet.Record;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ValueImpl implements Value {
  private final Record record;
  private final ValueWrapper valueWrapper;

  /**
   * If the underlying value is a collection type, return the number of values in the collection.
   *
   * <p>For {@link TypeSystem#LIST()} list} values, this will return the size of the list.
   *
   * <p>For {@link TypeSystem#MAP() map} values, this will return the number of entries in the map.
   *
   * <p>For {@link TypeSystem#NODE() node} and {@link TypeSystem#RELATIONSHIP()} relationship}
   * values, this will return the number of properties.
   *
   * <p>For {@link TypeSystem#PATH() path} values, this returns the length (number of relationships)
   * in the path.
   *
   * @return the number of values in an underlying collection
   */
  @Override
  public int size() {
    return 0;
  }

  /**
   * Retrieve all values of the underlying collection
   *
   * @return all values in unspecified order
   */
  @Override
  public Iterable<Value> values() {
    return null;
  }

  /**
   * Map and retrieve all values of the underlying collection
   *
   * @param mapFunction a function to map from Value to T. See {@link Values} for some predefined
   *     functions, such as {@link Values#ofBoolean()}, {@link Values#ofList(Function)}.
   * @return the result of mapping all values in unspecified order
   */
  @Override
  public <T> Iterable<T> values(Function<Value, T> mapFunction) {
    return null;
  }

  /**
   * Return the underlying map as a map of string keys and values converted using {@link
   * Value#asObject()}.
   *
   * <p>This is equivalent to calling {@link #asMap(Function)} with {@link Values#ofObject()}.
   *
   * @return the value as a Java map
   */
  @Override
  public Map<String, Object> asMap() {
    return null;
  }

  /**
   * @param mapFunction a function to map from Value to T. See {@link Values} for some predefined
   *     functions, such as {@link Values#ofBoolean()}, {@link Values#ofList(Function)}.
   * @return the value as a map from string keys to values of type T obtained from mapping he
   *     original map values, if possible
   * @see Values for a long list of built-in conversion functions
   */
  @Override
  public <T> Map<String, T> asMap(Function<Value, T> mapFunction) {
    return null;
  }

  /**
   * If this value represents a list or map, test if the collection is empty.
   *
   * @return {@code true} if size() is 0, otherwise {@code false}
   */
  @Override
  public boolean isEmpty() {
    return valueWrapper.isEmpty();
  }

  /**
   * If the underlying value supports {@link #get(String) key-based indexing}, return an iterable of
   * the keys in the map, this applies to {@link TypeSystem#MAP() map}, {@link #asNode() node} and
   * {@link TypeSystem#RELATIONSHIP()} relationship} values.
   *
   * @return the keys in the value
   */
  @Override
  public Iterable<String> keys() {
    return null;
  }

  /**
   * Check if the list of keys contains the given key
   *
   * @param key the key
   * @return {@code true} if this map keys contains the given key otherwise {@code false}
   */
  @Override
  public boolean containsKey(String key) {
    return false;
  }

  /**
   * Retrieve the value of the property with the given key
   *
   * @param key the key of the property
   * @return the property's value or a {@link NullValue} if no such key exists
   * @throws ClientException if record has not been initialized
   */
  @Override
  public Value get(String key) {
    return null;
  }

  /**
   * Retrieve the value at the given index
   *
   * @param index the index of the value
   * @return the value or a {@link NullValue} if the index is out of bounds
   * @throws ClientException if record has not been initialized
   */
  @Override
  public Value get(int index) {
    return null;
  }

  /**
   * @return The type of this value as defined in the Neo4j type system
   */
  @Override
  public Type type() {
    return null;
  }

  /**
   * Test if this value is a value of the given type
   *
   * @param type the given type
   * @return type.isTypeOf(this)
   */
  @Override
  public boolean hasType(Type type) {
    return false;
  }

  /**
   * @return {@code true} if the value is a Boolean value and has the value True.
   */
  @Override
  public boolean isTrue() {
    return false;
  }

  /**
   * @return {@code true} if the value is a Boolean value and has the value False.
   */
  @Override
  public boolean isFalse() {
    return false;
  }

  /**
   * @return {@code true} if the value is a Null, otherwise {@code false}
   */
  @Override
  public boolean isNull() {
    return false;
  }

  /**
   * This returns a java standard library representation of the underlying value, using a java type
   * that is "sensible" given the underlying type. The mapping for common types is as follows:
   *
   * <ul>
   *   <li>{@link TypeSystem#NULL()} - {@code null}
   *   <li>{@link TypeSystem#LIST()} - {@link List}
   *   <li>{@link TypeSystem#MAP()} - {@link Map}
   *   <li>{@link TypeSystem#BOOLEAN()} - {@link Boolean}
   *   <li>{@link TypeSystem#INTEGER()} - {@link Long}
   *   <li>{@link TypeSystem#FLOAT()} - {@link Double}
   *   <li>{@link TypeSystem#STRING()} - {@link String}
   *   <li>{@link TypeSystem#BYTES()} - {@literal byte[]}
   *   <li>{@link TypeSystem#DATE()} - {@link LocalDate}
   *   <li>{@link TypeSystem#TIME()} - {@link OffsetTime}
   *   <li>{@link TypeSystem#LOCAL_TIME()} - {@link LocalTime}
   *   <li>{@link TypeSystem#DATE_TIME()} - {@link ZonedDateTime}
   *   <li>{@link TypeSystem#LOCAL_DATE_TIME()} - {@link LocalDateTime}
   *   <li>{@link TypeSystem#DURATION()} - {@link IsoDuration}
   *   <li>{@link TypeSystem#POINT()} - {@link Point}
   *   <li>{@link TypeSystem#NODE()} - {@link Node}
   *   <li>{@link TypeSystem#RELATIONSHIP()} - {@link Relationship}
   *   <li>{@link TypeSystem#PATH()} - {@link Path}
   * </ul>
   *
   * <p>Note that the types in {@link TypeSystem} refers to the Neo4j type system where {@link
   * TypeSystem#INTEGER()} and {@link TypeSystem#FLOAT()} are both 64-bit precision. This is why
   * these types return java {@link Long} and {@link Double}, respectively.
   *
   * @return the value as a Java Object
   */
  @Override
  public Object asObject() {
    return null;
  }

  /**
   * Apply the mapping function on the value if the value is not a {@link NullValue}, or the default
   * value if the value is a {@link NullValue}.
   *
   * @param mapper The mapping function defines how to map a {@link Value} to T.
   * @param defaultValue the value to return if the value is a {@link NullValue}
   * @return The value after applying the given mapping function or the default value if the value
   *     is {@link NullValue}.
   */
  @Override
  public <T> T computeOrDefault(Function<Value, T> mapper, T defaultValue) {
    return null;
  }

  /**
   * @return the value as a Java boolean, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public boolean asBoolean() {
    return false;
  }

  /**
   * @param defaultValue return this value if the value is a {@link NullValue}.
   * @return the value as a Java boolean, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public boolean asBoolean(boolean defaultValue) {
    return false;
  }

  /**
   * @return the value as a Java byte array, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public byte[] asByteArray() {
    return new byte[0];
  }

  /**
   * @param defaultValue default to this value if the original value is a {@link NullValue}
   * @return the value as a Java byte array, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public byte[] asByteArray(byte[] defaultValue) {
    return new byte[0];
  }

  /**
   * @return the value as a Java String, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public String asString() {
    return null;
  }

  /**
   * @param defaultValue return this value if the value is null.
   * @return the value as a Java String, if possible
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public String asString(String defaultValue) {
    return null;
  }

  /**
   * @return the value as a Java Number, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public Number asNumber() {
    return null;
  }

  /**
   * Returns a Java long if no precision is lost in the conversion.
   *
   * @return the value as a Java long.
   * @throws LossyCoercion if it is not possible to convert the value without loosing precision.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public long asLong() {
    return 0;
  }

  /**
   * Returns a Java long if no precision is lost in the conversion.
   *
   * @param defaultValue return this default value if the value is a {@link NullValue}.
   * @return the value as a Java long.
   * @throws LossyCoercion if it is not possible to convert the value without loosing precision.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public long asLong(long defaultValue) {
    return 0;
  }

  /**
   * Returns a Java int if no precision is lost in the conversion.
   *
   * @return the value as a Java int.
   * @throws LossyCoercion if it is not possible to convert the value without loosing precision.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public int asInt() {
    return 0;
  }

  /**
   * Returns a Java int if no precision is lost in the conversion.
   *
   * @param defaultValue return this default value if the value is a {@link NullValue}.
   * @return the value as a Java int.
   * @throws LossyCoercion if it is not possible to convert the value without loosing precision.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public int asInt(int defaultValue) {
    return 0;
  }

  /**
   * Returns a Java double if no precision is lost in the conversion.
   *
   * @return the value as a Java double.
   * @throws LossyCoercion if it is not possible to convert the value without loosing precision.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public double asDouble() {
    return 0;
  }

  /**
   * Returns a Java double if no precision is lost in the conversion.
   *
   * @param defaultValue default to this value if the value is a {@link NullValue}.
   * @return the value as a Java double.
   * @throws LossyCoercion if it is not possible to convert the value without loosing precision.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public double asDouble(double defaultValue) {
    return 0;
  }

  /**
   * Returns a Java float if no precision is lost in the conversion.
   *
   * @return the value as a Java float.
   * @throws LossyCoercion if it is not possible to convert the value without loosing precision.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public float asFloat() {
    return 0;
  }

  /**
   * Returns a Java float if no precision is lost in the conversion.
   *
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a Java float.
   * @throws LossyCoercion if it is not possible to convert the value without loosing precision.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public float asFloat(float defaultValue) {
    return 0;
  }

  /**
   * If the underlying type can be viewed as a list, returns a java list of values, where each value
   * has been converted using {@link #asObject()}.
   *
   * @return the value as a Java list of values, if possible
   * @see #asObject()
   */
  @Override
  public List<Object> asList() {
    return null;
  }

  /**
   * If the underlying type can be viewed as a list, returns a java list of values, where each value
   * has been converted using {@link #asObject()}.
   *
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a Java list of values, if possible
   * @see #asObject()
   */
  @Override
  public List<Object> asList(List<Object> defaultValue) {
    return null;
  }

  /**
   * @param mapFunction a function to map from Value to T. See {@link Values} for some predefined
   *     functions, such as {@link Values#ofBoolean()}, {@link Values#ofList(Function)}.
   * @return the value as a list of T obtained by mapping from the list elements, if possible
   * @see Values for a long list of built-in conversion functions
   */
  @Override
  public <T> List<T> asList(Function<Value, T> mapFunction) {
    return null;
  }

  /**
   * @param mapFunction a function to map from Value to T. See {@link Values} for some predefined
   *     functions, such as {@link Values#ofBoolean()}, {@link Values#ofList(Function)}.
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a list of T obtained by mapping from the list elements, if possible
   * @see Values for a long list of built-in conversion functions
   */
  @Override
  public <T> List<T> asList(Function<Value, T> mapFunction, List<T> defaultValue) {
    return null;
  }

  /**
   * @return the value as a {@link Entity}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public Entity asEntity() {
    return null;
  }

  /**
   * @return the value as a {@link Node}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public Node asNode() {
    return null;
  }

  /**
   * @return the value as a {@link Relationship}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public Relationship asRelationship() {
    return null;
  }

  /**
   * @return the value as a {@link Path}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public Path asPath() {
    return null;
  }

  /**
   * @return the value as a {@link LocalDate}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public LocalDate asLocalDate() {
    return null;
  }

  /**
   * @return the value as a {@link OffsetTime}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public OffsetTime asOffsetTime() {
    return null;
  }

  /**
   * @return the value as a {@link LocalTime}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public LocalTime asLocalTime() {
    return null;
  }

  /**
   * @return the value as a {@link LocalDateTime}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public LocalDateTime asLocalDateTime() {
    return null;
  }

  /**
   * @return the value as a {@link OffsetDateTime}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public OffsetDateTime asOffsetDateTime() {
    return null;
  }

  /**
   * @return the value as a {@link ZonedDateTime}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public ZonedDateTime asZonedDateTime() {
    return null;
  }

  /**
   * @return the value as a {@link IsoDuration}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public IsoDuration asIsoDuration() {
    return null;
  }

  /**
   * @return the value as a {@link Point}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public Point asPoint() {
    return null;
  }

  /**
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a {@link LocalDate}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public LocalDate asLocalDate(LocalDate defaultValue) {
    return null;
  }

  /**
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a {@link OffsetTime}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public OffsetTime asOffsetTime(OffsetTime defaultValue) {
    return null;
  }

  /**
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a {@link LocalTime}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public LocalTime asLocalTime(LocalTime defaultValue) {
    return null;
  }

  /**
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a {@link LocalDateTime}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public LocalDateTime asLocalDateTime(LocalDateTime defaultValue) {
    return null;
  }

  /**
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a {@link OffsetDateTime}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public OffsetDateTime asOffsetDateTime(OffsetDateTime defaultValue) {
    return null;
  }

  /**
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a {@link ZonedDateTime}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public ZonedDateTime asZonedDateTime(ZonedDateTime defaultValue) {
    return null;
  }

  /**
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a {@link IsoDuration}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public IsoDuration asIsoDuration(IsoDuration defaultValue) {
    return null;
  }

  /**
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a {@link Point}, if possible.
   * @throws Uncoercible if value types are incompatible.
   */
  @Override
  public Point asPoint(Point defaultValue) {
    return null;
  }

  /**
   * Return as a map of string keys and values converted using {@link Value#asObject()}.
   *
   * <p>This is equivalent to calling {@link #asMap(Function, Map)} with {@link Values#ofObject()}.
   *
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a Java map
   */
  @Override
  public Map<String, Object> asMap(Map<String, Object> defaultValue) {
    return null;
  }

  /**
   * @param mapFunction a function to map from Value to T. See {@link Values} for some predefined
   *     functions, such as {@link Values#ofBoolean()}, {@link Values#ofList(Function)}.
   * @param defaultValue default to this value if the value is a {@link NullValue}
   * @return the value as a map from string keys to values of type T obtained from mapping he
   *     original map values, if possible
   * @see Values for a long list of built-in conversion functions
   */
  @Override
  public <T> Map<String, T> asMap(Function<Value, T> mapFunction, Map<String, T> defaultValue) {
    return null;
  }

  /**
   * Retrieve the value with the given key. If no value found by the key, then the default value
   * provided would be returned.
   *
   * @param key the key of the value
   * @param defaultValue the default value that would be returned if no value found by the key in
   *     the map
   * @return the value found by the key or the default value if no such key exists
   */
  @Override
  public Value get(String key, Value defaultValue) {
    return null;
  }

  /**
   * Retrieve the object with the given key. If no object found by the key, then the default object
   * provided would be returned.
   *
   * @param key the key of the object
   * @param defaultValue the default object that would be returned if no object found by the key in
   *     the map
   * @return the object found by the key or the default object if no such key exists
   */
  @Override
  public Object get(String key, Object defaultValue) {
    return null;
  }

  /**
   * Retrieve the number with the given key. If no number found by the key, then the default number
   * provided would be returned.
   *
   * @param key the key of the number
   * @param defaultValue the default number that would be returned if no number found by the key in
   *     the map
   * @return the number found by the key or the default number if no such key exists
   */
  @Override
  public Number get(String key, Number defaultValue) {
    return null;
  }

  /**
   * Retrieve the entity with the given key. If no entity found by the key, then the default entity
   * provided would be returned.
   *
   * @param key the key of the entity
   * @param defaultValue the default entity that would be returned if no entity found by the key in
   *     the map
   * @return the entity found by the key or the default entity if no such key exists
   */
  @Override
  public Entity get(String key, Entity defaultValue) {
    return null;
  }

  /**
   * Retrieve the node with the given key. If no node found by the key, then the default node
   * provided would be returned.
   *
   * @param key the key of the node
   * @param defaultValue the default node that would be returned if no node found by the key in the
   *     map
   * @return the node found by the key or the default node if no such key exists
   */
  @Override
  public Node get(String key, Node defaultValue) {
    return null;
  }

  /**
   * Retrieve the path with the given key. If no path found by the key, then the default path
   * provided would be returned.
   *
   * @param key the key of the property
   * @param defaultValue the default path that would be returned if no path found by the key in the
   *     map
   * @return the path found by the key or the default path if no such key exists
   */
  @Override
  public Path get(String key, Path defaultValue) {
    return null;
  }

  /**
   * Retrieve the value with the given key. If no value found by the key, then the default value
   * provided would be returned.
   *
   * @param key the key of the property
   * @param defaultValue the default value that would be returned if no value found by the key in
   *     the map
   * @return the value found by the key or the default value if no such key exists
   */
  @Override
  public Relationship get(String key, Relationship defaultValue) {
    return null;
  }

  /**
   * Retrieve the list of objects with the given key. If no value found by the key, then the default
   * value provided would be returned.
   *
   * @param key the key of the value
   * @param defaultValue the default value that would be returned if no value found by the key in
   *     the map
   * @return the list of objects found by the key or the default value if no such key exists
   */
  @Override
  public List<Object> get(String key, List<Object> defaultValue) {
    return null;
  }

  /**
   * Retrieve the list with the given key. If no value found by the key, then the default list
   * provided would be returned.
   *
   * @param key the key of the value
   * @param defaultValue the default value that would be returned if no value found by the key in
   *     the map
   * @param mapFunc the map function that defines how to map each element of the list from {@link
   *     Value} to T
   * @return the converted list found by the key or the default list if no such key exists
   */
  @Override
  public <T> List<T> get(String key, List<T> defaultValue, Function<Value, T> mapFunc) {
    return null;
  }

  /**
   * Retrieve the map with the given key. If no value found by the key, then the default value
   * provided would be returned.
   *
   * @param key the key of the property
   * @param defaultValue the default value that would be returned if no value found by the key in
   *     the map
   * @return the map found by the key or the default value if no such key exists
   */
  @Override
  public Map<String, Object> get(String key, Map<String, Object> defaultValue) {
    return null;
  }

  /**
   * Retrieve the map with the given key. If no value found by the key, then the default map
   * provided would be returned.
   *
   * @param key the key of the value
   * @param defaultValue the default value that would be returned if no value found by the key in
   *     the map
   * @param mapFunc the map function that defines how to map each value in map from {@link Value} to
   *     T
   * @return the converted map found by the key or the default map if no such key exists.
   */
  @Override
  public <T> Map<String, T> get(
      String key, Map<String, T> defaultValue, Function<Value, T> mapFunc) {
    return null;
  }

  /**
   * Retrieve the java integer with the given key. If no integer found by the key, then the default
   * integer provided would be returned.
   *
   * @param key the key of the property
   * @param defaultValue the default integer that would be returned if no integer found by the key
   *     in the map
   * @return the integer found by the key or the default integer if no such key exists
   */
  @Override
  public int get(String key, int defaultValue) {
    return 0;
  }

  /**
   * Retrieve the java long number with the given key. If no value found by the key, then the
   * default value provided would be returned.
   *
   * @param key the key of the property
   * @param defaultValue the default value that would be returned if no value found by the key in
   *     the map
   * @return the java long number found by the key or the default value if no such key exists
   */
  @Override
  public long get(String key, long defaultValue) {
    return 0;
  }

  /**
   * Retrieve the java boolean with the given key. If no value found by the key, then the default
   * value provided would be returned.
   *
   * @param key the key of the property
   * @param defaultValue the default value that would be returned if no value found by the key in
   *     the map
   * @return the java boolean found by the key or the default value if no such key exists
   */
  @Override
  public boolean get(String key, boolean defaultValue) {
    return false;
  }

  /**
   * Retrieve the java string with the given key. If no string found by the key, then the default
   * string provided would be returned.
   *
   * @param key the key of the property
   * @param defaultValue the default string that would be returned if no string found by the key in
   *     the map
   * @return the string found by the key or the default string if no such key exists
   */
  @Override
  public String get(String key, String defaultValue) {
    return null;
  }

  /**
   * Retrieve the java float number with the given key. If no value found by the key, then the
   * default value provided would be returned.
   *
   * @param key the key of the property
   * @param defaultValue the default value that would be returned if no value found by the key in
   *     the map
   * @return the java float number found by the key or the default value if no such key exists
   */
  @Override
  public float get(String key, float defaultValue) {
    return 0;
  }

  /**
   * Retrieve the java double number with the given key. If no value found by the key, then the
   * default value provided would be returned.
   *
   * @param key the key of the property
   * @param defaultValue the default value that would be returned if no value found by the key in
   *     the map
   * @return the java double number found by the key or the default value if no such key exists
   */
  @Override
  public double get(String key, double defaultValue) {
    return 0;
  }
}
