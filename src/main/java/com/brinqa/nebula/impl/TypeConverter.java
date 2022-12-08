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

import com.vesoft.nebula.Value;
import org.apache.commons.lang.NotImplementedException;
import org.neo4j.driver.internal.types.TypeConstructor;
import org.neo4j.driver.internal.types.TypeRepresentation;
import org.neo4j.driver.types.Type;

/** Convert from Nebula type to Neo4j. */
public class TypeConverter {

  public static Type toNeo4jType(final Value nebulaValue) {
    final var typeConstructor = toNeo4jTypeRepresentation(nebulaValue);
    return new TypeRepresentation(typeConstructor);
  }

  private static TypeConstructor toNeo4jTypeRepresentation(Value value) {
    switch (value.getSetField()) {
      case Value.NVAL:
        return TypeConstructor.NULL;
      case Value.BVAL:
        return TypeConstructor.BOOLEAN;
      case Value.IVAL:
        return TypeConstructor.INTEGER;
      case Value.FVAL:
        return TypeConstructor.FLOAT;
      case Value.SVAL:
        return TypeConstructor.STRING;
      case Value.DVAL:
        return TypeConstructor.DATE;
      case Value.TVAL:
        return TypeConstructor.TIME;
      case Value.DTVAL:
        return TypeConstructor.DATE_TIME;
      case Value.VVAL:
        return TypeConstructor.NODE;
      case Value.LVAL:
      case Value.UVAL:
        return TypeConstructor.LIST;
      case Value.DUVAL:
        return TypeConstructor.DURATION;
      case Value.EVAL:
        return TypeConstructor.RELATIONSHIP;
      case Value.MVAL:
        return TypeConstructor.MAP;
      case Value.PVAL:
        return TypeConstructor.PATH;
      case Value.GVAL:
      case Value.GGVAL:
        throw new NotImplementedException();
      default:
        throw new IllegalArgumentException("Unknown field id " + value.getSetField());
    }
  }
}
