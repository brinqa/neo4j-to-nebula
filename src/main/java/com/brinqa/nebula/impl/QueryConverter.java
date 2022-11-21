/*
 * Copyright 2002 Brinqa, Inc. All rights reserved.
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import org.neo4j.driver.Query;

public class QueryConverter {

  /** Simple pattern cache to improve query replacement speed. */
  private static final Cache<String, Pattern> CACHE =
      CacheBuilder.newBuilder().maximumSize(256).build();

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
  /**
   * Unfortunately Nebula doesn't have support for parameters.
   *
   * @param query neo4j based query.
   * @return Nebula based query.
   */
  public static String toText(Query query) {
    String workingQuery = query.text();
    final Map<String, Object> parameters = query.parameters().asMap();

    for (final Entry<String, Object> entry : parameters.entrySet()) {
      final var namedParameter = entry.getKey();
      final var parameterValue = convertToString(entry.getValue());

      final var regex = new String[] {"\\$\\{" + namedParameter + "}", "\\$" + namedParameter};

      for (String r : regex) {
        final Pattern pattern;
        try {
          pattern = CACHE.get(r, () -> Pattern.compile(r));
          workingQuery = pattern.matcher(workingQuery).replaceAll(parameterValue);
        } catch (ExecutionException e) {
          throw new IllegalArgumentException(e.getCause());
        }
      }
    }
    return workingQuery;
  }

  static String convertToString(Object value) {
    if (value instanceof Number) {
      return value.toString();
    }
    if (value instanceof String) {
      return String.format("\"%s\"", value);
    }
    if (value instanceof Temporal) {
      return toDateTimeParameter((Temporal) value);
    }
    if (value instanceof Date) {
      return toDateTimeParameter(((Date) value).toInstant());
    }
    return "NULL";
  }

  static String toDateTimeParameter(Temporal dt) {
    String datetimeString = DATE_TIME_FORMATTER.format(dt);
    return String.format("datetime(\"%s\")", datetimeString);
  }
}
