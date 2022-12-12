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
import com.vesoft.nebula.client.graph.data.Node;
import com.vesoft.nebula.client.graph.data.PathWrapper;
import com.vesoft.nebula.client.graph.data.Relationship;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.neo4j.driver.internal.InternalIsoDuration;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalPath;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.internal.value.BooleanValue;
import org.neo4j.driver.internal.value.DateTimeValue;
import org.neo4j.driver.internal.value.DateValue;
import org.neo4j.driver.internal.value.DurationValue;
import org.neo4j.driver.internal.value.FloatValue;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.internal.value.MapValue;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.internal.value.NullValue;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.driver.internal.value.StringValue;
import org.neo4j.driver.internal.value.TimeValue;
import org.neo4j.driver.internal.value.ValueAdapter;
import org.neo4j.driver.types.IsoDuration;
import org.neo4j.driver.types.Path.Segment;

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
    if (null == value) {
      return (ValueAdapter) NullValue.NULL;
    }

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
        final var s = new String(value.getSVal(), UTF_8);
        return new StringValue(s);
      case Value.DVAL:
        var ld = toLocalDate(value.getDVal());
        return new DateValue(ld);
      case Value.TVAL:
        final var odt = toOffsetTime(valueWrapper.asTime());
        return new TimeValue(odt);
      case Value.DTVAL:
        final var dt = valueWrapper.asDateTime();
        return new DateTimeValue(toZoneDateTime(dt));
      case Value.LVAL:
        return toListValue(valueWrapper.asList());
      case Value.UVAL:
        return toListValue(valueWrapper.asSet());
      case Value.DUVAL:
        final var d = toIsoDuration(valueWrapper.asDuration());
        return new DurationValue(d);
      case Value.MVAL:
        try {
          return toMapValue(valueWrapper.asMap());
        } catch (UnsupportedEncodingException e) {
          throw new IllegalStateException(e);
        }
      case Value.VVAL:
        return toNodeValue(valueWrapper);
      case Value.EVAL:
        return toRelationshipValue(valueWrapper);
      case Value.PVAL:
        return toPathValue(valueWrapper);
      case Value.GVAL:
      case Value.GGVAL:
      case Value.NVAL: // valid
        return (ValueAdapter) NullValue.NULL;
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
    final var seconds =
        d.toSecondsPart()
            + TimeUnit.HOURS.toSeconds(d.toHoursPart())
            + TimeUnit.MINUTES.toSeconds(d.toMinutes());
    final var nanos = d.toNanosPart() + TimeUnit.MILLISECONDS.toNanos(d.toMillisPart());
    return new InternalIsoDuration(duration.getMonths(), d.toDaysPart(), seconds, (int) nanos);
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

  public static NodeValue toNodeValue(ValueWrapper valueWrapper) {
    try {
      return toNodeValue(valueWrapper.asNode());
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static NodeValue toNodeValue(Node n) {
    final var labels = n.labels();
    final var map =
        labels.stream()
            .flatMap(
                label -> {
                  try {
                    return n.properties(label).entrySet().stream();
                  } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e);
                  }
                })
            .map(NebulaToNeo4jConverter::toEntry)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (value, value2) -> value));
    // find the ID, this is tricky as this will be the BRINQA ID
    final long id = n.getId().asLong();
    return new NodeValue(new InternalNode(id, n.labels(), map));
  }

  public static RelationshipValue toRelationshipValue(final ValueWrapper valueWrapper) {
    final var relationship = valueWrapper.asRelationship();
    return toRelationshipValue(relationship);
  }

  public static RelationshipValue toRelationshipValue(final Relationship relationship) {
    try {
      final var map =
          relationship.properties().entrySet().stream()
              .map(NebulaToNeo4jConverter::toEntry)
              .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (value, value2) -> value));
      // NOTE: using Neo4j concept of ID based on Brinqa providing an ID
      final var srcId = relationship.srcId().asLong();
      final var destId = relationship.dstId().asLong();
      final var type = relationship.edgeName();
      // NOTE: there's no `id` for a relationship
      final var rel = new InternalRelationship(Long.MAX_VALUE, srcId, destId, type, map);
      return new RelationshipValue(rel);
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  public static PathValue toPathValue(final ValueWrapper valueWrapper) {
    try {
      final PathWrapper path = valueWrapper.asPath();

      final List<Segment> segments =
          path.getSegments().stream()
              .map(NebulaToNeo4jConverter::buildSegment)
              .collect(Collectors.toList());

      final List<org.neo4j.driver.types.Node> nodes =
          path.getNodes().stream()
              .map(node -> toNodeValue(node).asNode())
              .collect(Collectors.toList());

      final List<org.neo4j.driver.types.Relationship> relationships =
          path.getRelationships().stream()
              .map(r -> toRelationshipValue(r).asRelationship())
              .collect(Collectors.toList());

      return new PathValue(new InternalPath(segments, nodes, relationships));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  static Segment buildSegment(PathWrapper.Segment s) {
    final var src = toNodeValue(s.getStartNode()).asNode();
    final var dest = toNodeValue(s.getEndNode()).asNode();
    final var rel = toRelationshipValue(s.getRelationShip()).asRelationship();
    return new InternalPath.SelfContainedSegment(src, rel, dest);
  }

  static Map.Entry<String, org.neo4j.driver.Value> toEntry(Map.Entry<String, ValueWrapper> p) {
    final var key = p.getKey();
    final var value = NebulaToNeo4jConverter.toValue(p.getValue());
    return new SimpleEntry<>(key, value);
  }
}
