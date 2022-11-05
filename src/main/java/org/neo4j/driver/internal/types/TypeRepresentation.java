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
package org.neo4j.driver.internal.types;

import static org.neo4j.driver.internal.types.TypeConstructor.LIST;

import org.neo4j.driver.Value;
import org.neo4j.driver.types.Type;

public class TypeRepresentation implements Type {
  private final TypeConstructor tyCon;

  public TypeRepresentation(TypeConstructor tyCon) {
    this.tyCon = tyCon;
  }

  @Override
  public boolean isTypeOf(Value value) {
    return tyCon.covers(value);
  }

  @Override
  public String name() {
    if (tyCon == LIST) {
      return "LIST OF ANY?";
    }

    return tyCon.toString();
  }

  public TypeConstructor constructor() {
    return tyCon;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TypeRepresentation that = (TypeRepresentation) o;

    return tyCon == that.tyCon;
  }

  @Override
  public int hashCode() {
    return tyCon.hashCode();
  }
}
