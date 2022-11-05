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
package org.neo4j.driver.summary;

import java.util.function.Function;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.Neo4jException;

/**
 * The type of query executed.
 *
 * @since 1.0
 */
public enum QueryType {
  READ_ONLY,
  READ_WRITE,
  WRITE_ONLY,
  SCHEMA_WRITE;

  private static final String UNEXPECTED_TYPE_MSG_FMT = "Unknown query type: `%s`.";
  private static final Function<String, ClientException> UNEXPECTED_TYPE_EXCEPTION_SUPPLIER =
      (type) -> new ClientException(String.format(UNEXPECTED_TYPE_MSG_FMT, type));

  public static QueryType fromCode(String type) {
    return fromCode(type, UNEXPECTED_TYPE_EXCEPTION_SUPPLIER);
  }

  public static QueryType fromCode(
      String type, Function<String, ? extends Neo4jException> exceptionFunction) {
    switch (type) {
      case "r":
        return QueryType.READ_ONLY;
      case "rw":
        return QueryType.READ_WRITE;
      case "w":
        return QueryType.WRITE_ONLY;
      case "s":
        return QueryType.SCHEMA_WRITE;
      default:
        if (exceptionFunction != null) {
          throw exceptionFunction.apply(type);
        } else {
          return null;
        }
    }
  }
}
