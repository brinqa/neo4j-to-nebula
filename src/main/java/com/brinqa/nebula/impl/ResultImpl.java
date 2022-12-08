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

import com.vesoft.nebula.client.graph.data.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.neo4j.driver.Record;
import org.neo4j.driver.Records;
import org.neo4j.driver.Result;
import org.neo4j.driver.exceptions.NoSuchRecordException;
import org.neo4j.driver.summary.ResultSummary;

public class ResultImpl implements Result {

  private final ResultSet resultSet;
  private final ResultSummary resultSummary;

  // built-in constructor
  private final AtomicInteger index = new AtomicInteger();
  private final Map<String, Integer> column2Index = new HashMap<>();

  public ResultImpl(ResultSet resultSet, ResultSummary resultSummary) {
    this.resultSet = resultSet;
    this.resultSummary = resultSummary;

    final var names = resultSet.getColumnNames();
    for (int i = 0; i < names.size(); i++) {
      column2Index.put(names.get(i), i);
    }
  }

  /**
   * Retrieve the keys of the records this result contains.
   *
   * @return all keys
   */
  @Override
  public List<String> keys() {
    return resultSet.getColumnNames();
  }

  /**
   * Test if there is another record we can navigate to in this result.
   *
   * @return true if {@link #next()} will return another record
   */
  @Override
  public boolean hasNext() {
    return index.get() < resultSet.rowsSize();
  }

  /**
   * Navigate to and retrieve the next {@link Record} in this result.
   *
   * @return the next record
   * @throws NoSuchRecordException if there is no record left in the stream
   */
  @Override
  public Record next() {
    if (hasNext()) {
      return get(index.getAndIncrement());
    }
    throw new NoSuchRecordException("No more records left.");
  }

  /**
   * Return the first record in the result, failing if there is not exactly one record left in the
   * stream
   *
   * <p>Calling this method always exhausts the result, even when {@link NoSuchRecordException} is
   * thrown.
   *
   * @return the first and only record in the stream
   * @throws NoSuchRecordException if there is not exactly one record left in the stream
   */
  @Override
  public Record single() throws NoSuchRecordException {
    int size = resultSet.rowsSize();
    if (size != 1) {
      throw new NoSuchRecordException("Invalid number of records returned: " + size);
    }
    return get(0);
  }

  /**
   * Investigate the next upcoming record without moving forward in the result.
   *
   * @return the next record
   * @throws NoSuchRecordException if there is no record left in the stream
   */
  @Override
  public Record peek() {
    if ((index.get() + 1) >= resultSet.rowsSize()) {
      throw new NoSuchRecordException("Peek ahead of actual size of the index.");
    }
    return get(index.get() + 1);
  }

  private Record get(int idx) {
    final var record = resultSet.rowValues(idx);
    return new RecordImpl(resultSet, record, column2Index);
  }

  /**
   * Convert this result to a sequential {@link Stream} of records.
   *
   * <p>Result is exhausted when a terminal operation on the returned stream is executed.
   *
   * @return sequential {@link Stream} of records. Empty stream if this result has already been
   *     consumed or is empty.
   */
  @Override
  public Stream<Record> stream() {
    return list().stream();
  }

  /**
   * Retrieve and store the entire result stream. This can be used if you want to iterate over the
   * stream multiple times or to store the whole result for later use.
   *
   * <p>Note that this method can only be used if you know that the query that yielded this result
   * returns a finite stream. Some queries can yield infinite results, in which case calling this
   * method will lead to running out of memory.
   *
   * <p>Calling this method exhausts the result.
   *
   * @return list of all remaining immutable records
   */
  @Override
  public List<Record> list() {
    return IntStream.range(0, resultSet.rowsSize())
        .mapToObj(this::get)
        .collect(Collectors.toList());
  }

  /**
   * Retrieve and store a projection of the entire result. This can be used if you want to iterate
   * over the stream multiple times or to store the whole result for later use.
   *
   * <p>Note that this method can only be used if you know that the query that yielded this result
   * returns a finite stream. Some queries can yield infinite results, in which case calling this
   * method will lead to running out of memory.
   *
   * <p>Calling this method exhausts the result.
   *
   * @param mapFunction a function to map from Record to T. See {@link Records} for some predefined
   *     functions.
   * @return list of all mapped remaining immutable records
   */
  @Override
  public <T> List<T> list(Function<Record, T> mapFunction) {
    return list().stream().map(mapFunction).collect(Collectors.toList());
  }

  /**
   * Return the result summary.
   *
   * <p>If the records in the result is not fully consumed, then calling this method will exhausts
   * the result.
   *
   * <p>If you want to access unconsumed records after summary, you shall use {@link Result#list()}
   * to buffer all records into memory before summary.
   *
   * @return a summary for the whole query result.
   */
  @Override
  public ResultSummary consume() {
    return this.resultSummary;
  }
}
