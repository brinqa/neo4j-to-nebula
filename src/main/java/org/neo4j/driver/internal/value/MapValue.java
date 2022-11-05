/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
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
package org.neo4j.driver.internal.value;

import static org.neo4j.driver.Values.ofObject;
import static org.neo4j.driver.Values.ofValue;
import static org.neo4j.driver.internal.util.Format.formatPairs;

import java.util.Map;
import java.util.function.Function;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.internal.util.Extract;
import org.neo4j.driver.types.Type;

public class MapValue extends ValueAdapter {
  private final Map<String, Value> val;

  public MapValue(Map<String, Value> val) {
    if (val == null) {
      throw new IllegalArgumentException("Cannot construct MapValue from null");
    }
    this.val = val;
  }

  @Override
  public boolean isEmpty() {
    return val.isEmpty();
  }

  @Override
  public Map<String, Object> asObject() {
    return asMap(ofObject());
  }

  @Override
  public Map<String, Object> asMap() {
    return Extract.map(val, ofObject());
  }

  @Override
  public <T> Map<String, T> asMap(Function<Value, T> mapFunction) {
    return Extract.map(val, mapFunction);
  }

  @Override
  public int size() {
    return val.size();
  }

  @Override
  public boolean containsKey(String key) {
    return val.containsKey(key);
  }

  @Override
  public Iterable<String> keys() {
    return val.keySet();
  }

  @Override
  public Iterable<Value> values() {
    return val.values();
  }

  @Override
  public <T> Iterable<T> values(Function<Value, T> mapFunction) {
    return Extract.map(val, mapFunction).values();
  }

  @Override
  public Value get(String key) {
    Value value = val.get(key);
    return value == null ? Values.NULL : value;
  }

  @Override
  public String toString() {
    return formatPairs(asMap(ofValue()));
  }

  @Override
  public Type type() {
    return InternalTypeSystem.TYPE_SYSTEM.MAP();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MapValue values = (MapValue) o;
    return val.equals(values.val);
  }

  @Override
  public int hashCode() {
    return val.hashCode();
  }
}
