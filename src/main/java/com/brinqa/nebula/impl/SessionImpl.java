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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.google.common.base.Throwables;

import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.ClientException;

import com.vesoft.nebula.client.graph.data.ResultSet;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;

import static com.vesoft.nebula.client.graph.net.Session.value2Nvalue;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class SessionImpl implements Session {

  private final String spaceName;
  private final ConnectionPool pool;
  private final AtomicBoolean openState = new AtomicBoolean(true);

  private final Retry useSpaceRetry;

  public SessionImpl(final ConnectionPool pool, final String spaceName) {
    this.pool = pool;
    this.spaceName = spaceName;

    final RetryConfig retryConfig =
        RetryConfig.<ResultSet>custom()
            .maxAttempts(40)
            .failAfterMaxAttempts(true)
            .waitDuration(Duration.ofSeconds(1))
            .retryOnResult(SessionImpl::useSpaceRetryPredicate)
            .build();
    this.useSpaceRetry = Retry.of("useSpace", retryConfig);
  }

  static boolean useSpaceRetryPredicate(ResultSet rs) {
    if (rs.isSucceeded()) {
      return false;
    }
    if (NebulaErrorCodes.E_SPACE_NOT_FOUND != rs.getErrorCode()) {
      final var msg = "Failed on use space, Code: {}, Message: {}";
      log.error(msg, rs.getErrorCode(), rs.getErrorMessage());
      throw new ClientException(rs.getErrorMessage());
    }
    return true;
  }

  @Override
  public Result run(String query, Value parameters) {
    final Query q = new Query(query, parameters);
    return run(q, TransactionConfig.empty());
  }

  @Override
  public Result run(String query, Map<String, Object> parameters) {
    final Query q = new Query(query, parameters);
    return run(q, TransactionConfig.empty());
  }

  @Override
  public Result run(String query, Record parameters) {
    return run(query, parameters.asMap());
  }

  @Override
  public Result run(String query) {
    return run(new Query(query));
  }

  @Override
  public Result run(Query query) {
    return run(query, TransactionConfig.empty());
  }

  @Override
  public Transaction beginTransaction() {
    return beginTransaction(TransactionConfig.empty());
  }

  @Override
  public Transaction beginTransaction(TransactionConfig config) {
    return new TransactionImpl(this, config);
  }

  @Override
  public <T> T readTransaction(TransactionWork<T> work) {
    return readTransaction(work, TransactionConfig.empty());
  }

  @Override
  public <T> T readTransaction(TransactionWork<T> work, TransactionConfig config) {
    return work.execute(new TransactionImpl(this, config));
  }

  @Override
  public <T> T writeTransaction(TransactionWork<T> work) {
    return readTransaction(work);
  }

  @Override
  public <T> T writeTransaction(TransactionWork<T> work, TransactionConfig config) {
    return readTransaction(work, config);
  }

  @Override
  public Result run(String query, TransactionConfig config) {
    return run(new Query(query), config);
  }

  @Override
  public Result run(String query, Map<String, Object> parameters, TransactionConfig config) {
    return run(new Query(query, parameters), config);
  }

  @Override
  public Result run(Query query, TransactionConfig config) {
    return executeQuery(query, config);
  }

  @Override
  public Bookmark lastBookmark() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void reset() {
    log.debug("No implemented.");
  }

  @Override
  public void close() {
    this.openState.set(false);
  }

  @Override
  public boolean isOpen() {
    return this.openState.get();
  }

  // =========================================================================
  // Internal methods
  // =========================================================================

  public void ping() {
    run("YIELD 1;");
  }

  /**
   * Execute a query on the first available Graph Service connection from the pool. Attempt to retry
   * for various transient issues.
   *
   * <ul>
   *   <li>Unknown Space (could be waiting on schema change retry)
   *   <li>Values not found on VID?
   * </ul>
   *
   * @param query
   * @param config
   * @return
   */
  public ResultImpl executeQuery(Query query, TransactionConfig config) {
    // FIXME: use the config timeout, use resilience4j for timeout
    return withConnection(
        connection -> {
          final long now = System.nanoTime();
          // create nebula parameters
          final var params = toNebulaParameters(query);
          // execute the query
          final var resultSet = connection.execute(query.text(), params);
          // time the query
          final long time = System.nanoTime() - now;
          if (!resultSet.isSucceeded()) {
            throw new ClientException("Failed query.", resultSet.getErrorMessage());
          }
          // build the neo4j summary results
          final var summary =
              new ResultSummaryImpl(time, query, spaceName, connection.getAddress());
          // build out neo4j result
          return new ResultImpl(resultSet, summary);
        });
  }

  <T> T withConnection(Function<Connection, T> consumer) {
    try {
      // FIXME: Retry if there's some other error
      final Connection c = this.pool.borrowObject();
      if (c.updateCurrentSpace(this.spaceName)) {
        final var stmt = String.format("USE %s;", this.spaceName);
        this.useSpaceRetry.executeSupplier(() -> c.execute(stmt, Map.of()));
      }
      try {
        return consumer.apply(c);
      } catch (Exception e) {
        // FIXME: determine if this exception should retried
        Throwables.throwIfUnchecked(e);
        // FIXME: Convert to the proper Neo4j exception
        throw new ClientException("BAD", e);
      } finally {
        this.pool.returnObject(c);
      }
    } catch (Exception e) {
      // FIXME: determine if this exception should retried
      Throwables.throwIfUnchecked(e);
      // FIXME: Convert to the proper Neo4j exception
      throw new IllegalArgumentException(e);
    }
  }

  Map<byte[], com.vesoft.nebula.Value> toNebulaParameters(Query query) {
    final var map = new HashMap<byte[], com.vesoft.nebula.Value>();
    query
        .parameters()
        .asMap()
        .forEach((key, value) -> map.put(key.getBytes(UTF_8), value2Nvalue(value)));
    return map;
  }
}
