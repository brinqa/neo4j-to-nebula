package com.brinqa.nebula.impl;

import static com.vesoft.nebula.client.graph.net.Session.value2Nvalue;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Throwables;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class SessionImpl implements Session {

  private final String spaceName;
  private final ConnectionPool pool;
  private final AtomicBoolean openState = new AtomicBoolean(true);

  public SessionImpl(final ConnectionPool pool, final String spaceName) {
    this.pool = pool;
    this.spaceName = spaceName;
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
    throw new UnsupportedOperationException();
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
        final var rsp = c.execute(stmt, Map.of());
        if (!rsp.isSucceeded()) {
          // FIXME: Fail if the space doesn't exist, retry for connection issues.
          throw new ClientException("Failed to use space: " + spaceName, rsp.getErrorMessage());
        }
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
        .forEach(
            (key, value) -> {
              final var k = key.getBytes(UTF_8);
              final var v = value2Nvalue(value);
              map.put(k, v);
            });
    return map;
  }
}
