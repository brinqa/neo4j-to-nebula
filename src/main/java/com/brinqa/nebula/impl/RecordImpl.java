/*
 * Copyright 2022 Brinqa, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.brinqa.nebula.impl;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.NoSuchRecordException;
import org.neo4j.driver.internal.value.NullValue;
import org.neo4j.driver.types.Entity;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.util.Pair;

import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RecordImpl implements Record {

  private final ResultSet resultSet;
  private final ResultSet.Record record;
  private final Map<String, Integer> column2Index;

  /**
   * Retrieve the keys of the underlying map
   *
   * @return all field keys in order
   */
  @Override
  public List<String> keys() {
    return resultSet.keys();
  }

  /**
   * Check if the list of keys contains the given key
   *
   * @param key the key
   * @return {@code true} if this map keys contains the given key otherwise {@code false}
   */
  @Override
  public boolean containsKey(String key) {
    return column2Index.containsKey(key);
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
    if (!containsKey(key)) {
      return NullValue.NULL;
    }
    final ValueWrapper valueWrapper = record.get(key);
    return new ValueImpl(record, valueWrapper);
  }

  /**
   * Retrieve the number of entries in this map
   *
   * @return the number of entries in this map
   */
  @Override
  public int size() {
    return record.size();
  }

  /**
   * Retrieve the values of the underlying map
   *
   * @return all field keys in order
   */
  @Override
  public List<Value> values() {
    return record.values().stream()
        .map(it -> new ValueImpl(record, it))
        .collect(Collectors.toList());
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
    return values().stream().map(mapFunction).collect(Collectors.toList());
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
   * Retrieve the index of the field with the given key
   *
   * @param key the give key
   * @return the index of the field as used by {@link #get(int)}
   * @throws NoSuchElementException if the given key is not from {@link #keys()}
   */
  @Override
  public int index(String key) {
    final Integer idx = column2Index.get(key);
    if (null == idx) {
      throw new NoSuchRecordException("Key does not exist: " + key);
    }
    return idx;
  }

  /**
   * Retrieve the value at the given field index
   *
   * @param index the index of the value
   * @return the value or a {@link NullValue} if the index is out of bounds
   * @throws ClientException if record has not been initialized
   */
  @Override
  public Value get(int index) {
    if (index >= record.size()) {
      return NullValue.NULL;
    }
    return new ValueImpl(record, record.get(index));
  }

  /**
   * Retrieve all record fields
   *
   * @return all fields in key order
   * @throws NoSuchRecordException if the associated underlying record is not available
   */
  @Override
  public List<Pair<String, Value>> fields() {
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

  private <T> T internalGetValue(String key, T defaultValue) {
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
    return internalGetValue(key, defaultValue);
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
