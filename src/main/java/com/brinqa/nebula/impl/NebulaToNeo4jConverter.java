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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.vesoft.nebula.Date;
import com.vesoft.nebula.Value;
import com.vesoft.nebula.client.graph.data.DateTimeWrapper;
import com.vesoft.nebula.client.graph.data.DurationWrapper;
import com.vesoft.nebula.client.graph.data.TimeWrapper;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.neo4j.driver.internal.InternalIsoDuration;
import org.neo4j.driver.internal.value.BooleanValue;
import org.neo4j.driver.internal.value.DateTimeValue;
import org.neo4j.driver.internal.value.DateValue;
import org.neo4j.driver.internal.value.DurationValue;
import org.neo4j.driver.internal.value.FloatValue;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.internal.value.MapValue;
import org.neo4j.driver.internal.value.NullValue;
import org.neo4j.driver.internal.value.StringValue;
import org.neo4j.driver.internal.value.TimeValue;
import org.neo4j.driver.internal.value.ValueAdapter;
import org.neo4j.driver.types.IsoDuration;

/** Convert from Nebula type to Neo4j. */
public class NebulaToNeo4jConverter {

  /**
   * Use the Nebula value wrapper to help translate to the Neo4j value.
   *
   * @param valueWrapper nebula value.
   * @return neo4j value.
   */
  public static ValueAdapter toValue(final ValueWrapper valueWrapper) {
    final var value = valueWrapper.getValue();
    switch (value.getSetField()) {
      case 0: // Empty
        return new ListValue();
      case Value.BVAL:
        return BooleanValue.fromBoolean((Boolean) (value.getFieldValue()));
      case Value.IVAL:
        return new IntegerValue(value.getIVal());
      case Value.FVAL:
        return new FloatValue(value.getFVal());
      case Value.SVAL:
        return new StringValue(new String(value.getSVal(), UTF_8));
      case Value.DVAL:
        return new DateValue(toLocalDate(value.getDVal()));
      case Value.TVAL:
        return new TimeValue(toOffsetTime(valueWrapper.asTime()));
      case Value.DTVAL:
        final var dt = valueWrapper.asDateTime();
        return new DateTimeValue(toZoneDateTime(dt));
      case Value.LVAL:
        return toListValue(valueWrapper.asList());
      case Value.UVAL:
        return toListValue(valueWrapper.asSet());
      case Value.DUVAL:
        return new DurationValue(toIsoDuration(valueWrapper.asDuration()));
      case Value.MVAL:
        try {
          return toMapValue(valueWrapper.asMap());
        } catch (UnsupportedEncodingException e) {
          throw new IllegalStateException(e);
        }
      case Value.EVAL:
        // Edge/Relationship
      case Value.PVAL:
        // Path
      case Value.VVAL:
        // Vertex/Node
      case Value.GVAL:
      case Value.GGVAL:
      case Value.NVAL: // valid
        return (ValueAdapter) NullValue.NULL;
        // FIXME throw new NotImplementedException();
      default:
        throw new IllegalArgumentException("Unknown field id " + value.getSetField());
    }
  }

  public static MapValue toMapValue(Map<String, ValueWrapper> map) {
    final Map<String, org.neo4j.driver.Value> ret =
        map.entrySet().stream()
            .map(e -> new SimpleEntry<>(e.getKey(), toValue(e.getValue())))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    return new MapValue(ret);
  }

  public static IsoDuration toIsoDuration(DurationWrapper duration) {
    final var d =
        Duration.of(duration.getSeconds(), ChronoUnit.SECONDS)
            .plus(Duration.of(duration.getMicroseconds(), ChronoUnit.MICROS));
    return new InternalIsoDuration(
        duration.getMonths(), d.toDaysPart(), d.toSecondsPart(), d.toNanosPart());
  }

  public static ListValue toListValue(Collection<ValueWrapper> valueWrappers) {
    return new ListValue(
        valueWrappers.stream().map(NebulaToNeo4jConverter::toValue).toArray(ValueAdapter[]::new));
  }

  public static ZonedDateTime toZoneDateTime(DateTimeWrapper dt) {
    final var zoneOffset = ZoneOffset.ofTotalSeconds(dt.getTimezoneOffset());
    final var localDateTime =
        LocalDateTime.of(
            dt.getYear(),
            dt.getMonth(),
            dt.getDay(),
            dt.getHour(),
            dt.getMinute(),
            dt.getSecond(),
            dt.getMicrosec() * 1000);
    return ZonedDateTime.of(localDateTime, zoneOffset);
  }

  public static LocalDate toLocalDate(Date date) {
    return LocalDate.of(date.year, date.month, date.day);
  }

  public static OffsetTime toOffsetTime(TimeWrapper time) {
    final var nanoSec = time.getMicrosec() * 1000;
    final var localTime = LocalTime.of(time.getHour(), time.getMinute(), time.getSecond(), nanoSec);
    final var zoneOffset = ZoneOffset.ofTotalSeconds(time.getTimezoneOffset());
    return OffsetTime.of(localTime, zoneOffset);
  }
}
