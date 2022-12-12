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

import java.util.Map;
import java.util.function.Function;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;

/** Nebula has the */
public class NebulaNode implements Node {

  /**
   * A unique id for this Entity. Ids are guaranteed to remain stable for the duration of the
   * session they were found in, but may be re-used for other entities after that. As such, if you
   * want a public identity to use for your entities, attaching an explicit 'id' property or similar
   * persistent and unique identifier is a better choice.
   *
   * @return the id of this entity
   */
  @Override
  public long id() {
    return 0;
  }

  /**
   * Retrieve the keys of the underlying map
   *
   * @return all map keys in unspecified order
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
   * Retrieve the number of entries in this map
   *
   * @return the number of entries in this map
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
   * Return all labels.
   *
   * @return a label Collection
   */
  @Override
  public Iterable<String> labels() {
    return null;
  }

  /**
   * Test if this node has a given label
   *
   * @param label the label
   * @return {@code true} if this node has the label otherwise {@code false}
   */
  @Override
  public boolean hasLabel(String label) {
    return false;
  }
}
