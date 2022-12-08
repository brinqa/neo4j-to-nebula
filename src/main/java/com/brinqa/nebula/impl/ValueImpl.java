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

import com.vesoft.nebula.Row;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import lombok.AllArgsConstructor;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.*;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
public class ValueImpl implements Value {

    private final ResultSet resultSet;

    @Override
    public int size() {
        int totalValuesNum = 0;
        for (int i = 0; i < resultSet.rowsSize(); i++) {
            ResultSet.Record record = resultSet.rowValues(i);
            for (ValueWrapper value : record.values()) {
                totalValuesNum += 1;
            }
        }
        return totalValuesNum;
    }

    @Override
    public Iterable<Value> values() {
        return null;
    }

    @Override
    public <T> Iterable<T> values(Function<Value, T> mapFunction) {
        return null;
    }

    @Override
    public Map<String, Object> asMap() {
        return null;
    }

    @Override
    public <T> Map<String, T> asMap(Function<Value, T> mapFunction) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return resultSet.rowsSize() <= 0;
    }

    @Override
    public Iterable<String> keys() {
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public Value get(String key) {
        return null;
    }

    @Override
    public Value get(int index) {
        return null;
    }

    @Override
    public Type type() {
        return null;
    }

    @Override
    public boolean hasType(Type type) {
        return false;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public Object asObject() {
        return null;
    }

    @Override
    public <T> T computeOrDefault(Function<Value, T> mapper, T defaultValue) {
        return null;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public boolean asBoolean(boolean defaultValue) {
        return false;
    }

    @Override
    public byte[] asByteArray() {
        return new byte[0];
    }

    @Override
    public byte[] asByteArray(byte[] defaultValue) {
        return new byte[0];
    }

    @Override
    public String asString() {
        return null;
    }

    @Override
    public String asString(String defaultValue) {
        return null;
    }

    @Override
    public Number asNumber() {
        return null;
    }

    @Override
    public long asLong() {
        return 0;
    }

    @Override
    public long asLong(long defaultValue) {
        return 0;
    }

    @Override
    public int asInt() {
        return 0;
    }

    @Override
    public int asInt(int defaultValue) {
        return 0;
    }

    @Override
    public double asDouble() {
        return 0;
    }

    @Override
    public double asDouble(double defaultValue) {
        return 0;
    }

    @Override
    public float asFloat() {
        return 0;
    }

    @Override
    public float asFloat(float defaultValue) {
        return 0;
    }

    @Override
    public List<Object> asList() {
        return null;
    }

    @Override
    public List<Object> asList(List<Object> defaultValue) {
        return null;
    }

    @Override
    public <T> List<T> asList(Function<Value, T> mapFunction) {
        return null;
    }

    @Override
    public <T> List<T> asList(Function<Value, T> mapFunction, List<T> defaultValue) {
        return null;
    }

    @Override
    public Entity asEntity() {
        return null;
    }

    @Override
    public Node asNode() {
        return null;
    }

    @Override
    public Relationship asRelationship() {
        return null;
    }

    @Override
    public Path asPath() {
        return null;
    }

    @Override
    public LocalDate asLocalDate() {
        return null;
    }

    @Override
    public OffsetTime asOffsetTime() {
        return null;
    }

    @Override
    public LocalTime asLocalTime() {
        return null;
    }

    @Override
    public LocalDateTime asLocalDateTime() {
        return null;
    }

    @Override
    public OffsetDateTime asOffsetDateTime() {
        return null;
    }

    @Override
    public ZonedDateTime asZonedDateTime() {
        return null;
    }

    @Override
    public IsoDuration asIsoDuration() {
        return null;
    }

    @Override
    public Point asPoint() {
        return null;
    }

    @Override
    public LocalDate asLocalDate(LocalDate defaultValue) {
        return null;
    }

    @Override
    public OffsetTime asOffsetTime(OffsetTime defaultValue) {
        return null;
    }

    @Override
    public LocalTime asLocalTime(LocalTime defaultValue) {
        return null;
    }

    @Override
    public LocalDateTime asLocalDateTime(LocalDateTime defaultValue) {
        return null;
    }

    @Override
    public OffsetDateTime asOffsetDateTime(OffsetDateTime defaultValue) {
        return null;
    }

    @Override
    public ZonedDateTime asZonedDateTime(ZonedDateTime defaultValue) {
        return null;
    }

    @Override
    public IsoDuration asIsoDuration(IsoDuration defaultValue) {
        return null;
    }

    @Override
    public Point asPoint(Point defaultValue) {
        return null;
    }

    @Override
    public Map<String, Object> asMap(Map<String, Object> defaultValue) {
        return null;
    }

    @Override
    public <T> Map<String, T> asMap(Function<Value, T> mapFunction, Map<String, T> defaultValue) {
        return null;
    }

    @Override
    public Value get(String key, Value defaultValue) {
        return null;
    }

    @Override
    public Object get(String key, Object defaultValue) {
        return null;
    }

    @Override
    public Number get(String key, Number defaultValue) {
        return null;
    }

    @Override
    public Entity get(String key, Entity defaultValue) {
        return null;
    }

    @Override
    public Node get(String key, Node defaultValue) {
        return null;
    }

    @Override
    public Path get(String key, Path defaultValue) {
        return null;
    }

    @Override
    public Relationship get(String key, Relationship defaultValue) {
        return null;
    }

    @Override
    public List<Object> get(String key, List<Object> defaultValue) {
        return null;
    }

    @Override
    public <T> List<T> get(String key, List<T> defaultValue, Function<Value, T> mapFunc) {
        return null;
    }

    @Override
    public Map<String, Object> get(String key, Map<String, Object> defaultValue) {
        return null;
    }

    @Override
    public <T> Map<String, T> get(String key, Map<String, T> defaultValue, Function<Value, T> mapFunc) {
        return null;
    }

    @Override
    public int get(String key, int defaultValue) {
        return 0;
    }

    @Override
    public long get(String key, long defaultValue) {
        return 0;
    }

    @Override
    public boolean get(String key, boolean defaultValue) {
        return false;
    }

    @Override
    public String get(String key, String defaultValue) {
        return null;
    }

    @Override
    public float get(String key, float defaultValue) {
        return 0;
    }

    @Override
    public double get(String key, double defaultValue) {
        return 0;
    }
}
