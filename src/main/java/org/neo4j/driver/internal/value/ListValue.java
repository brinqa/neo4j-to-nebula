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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.internal.util.Extract;
import org.neo4j.driver.types.Type;

public class ListValue extends ValueAdapter {
  private final Value[] values;

  public ListValue(Value... values) {
    if (values == null) {
      throw new IllegalArgumentException("Cannot construct ListValue from null");
    }
    this.values = values;
  }

  @Override
  public boolean isEmpty() {
    return values.length == 0;
  }

  @Override
  public List<Object> asObject() {
    return asList(ofObject());
  }

  @Override
  public List<Object> asList() {
    return Extract.list(values, ofObject());
  }

  @Override
  public <T> List<T> asList(Function<Value, T> mapFunction) {
    return Extract.list(values, mapFunction);
  }

  @Override
  public int size() {
    return values.length;
  }

  @Override
  public Value get(int index) {
    return index >= 0 && index < values.length ? values[index] : Values.NULL;
  }

  @Override
  public <T> Iterable<T> values(final Function<Value, T> mapFunction) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          private int cursor = 0;

          @Override
          public boolean hasNext() {
            return cursor < values.length;
          }

          @Override
          public T next() {
            return mapFunction.apply(values[cursor++]);
          }

          @Override
          public void remove() {}
        };
      }
    };
  }

  @Override
  public Type type() {
    return InternalTypeSystem.TYPE_SYSTEM.LIST();
  }

  @Override
  public String toString() {
    return Arrays.toString(values);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ListValue otherValues = (ListValue) o;
    return Arrays.equals(values, otherValues.values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }
}
