package com.brinqa.nebula.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import com.brinqa.nebula.DriverConfig;

import lombok.extern.slf4j.Slf4j;

import static com.vesoft.nebula.client.graph.net.Session.value2Nvalue;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class SessionImpl implements Session {

  private final String spaceName;
  private final ConnectionPool pool;
  private final DriverConfig driverConfig;
  private final AtomicBoolean openState = new AtomicBoolean(true);

  public SessionImpl(
      final DriverConfig driverConfig, final ConnectionPool pool, final String spaceName) {
    this.pool = pool;
    this.spaceName = spaceName;
    this.driverConfig = driverConfig;
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

  //=========================================================================
  // Internal methods
  //=========================================================================

  public void ping() {
    run("YIELD 1;");
  }

  ResultImpl executeQuery(Query query, TransactionConfig config) {
    Connection connection = null;
    ClientException lastException = null;
    try {
      connection = this.pool.borrowObject();
      for (int i = 0; i < driverConfig.getMaxRetries(); i++) {
        try {
          return internalExecuteQuery(connection, query, config);
        } catch (ClientException e) {
          lastException = e;
          log.debug("Retrying failed client exception");
        }
      }
    }
    catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new ServiceUnavailableException("Failed to get connection.", e);
    }
    finally {
      if (null != connection) {
        pool.returnObject(connection);
      }
    }
    throw new ClientException("");
  }

  /** TODO: Use timeout in transaction configuration. */
  ResultImpl internalExecuteQuery(Connection connection, Query query, TransactionConfig config) {
    // initialize space
    if (connection.updateCurrentSpace(this.spaceName)) {
      final var stmt = String.format("USE %s;", this.spaceName);
      final var rsp = connection.execute(stmt, Map.of());
      if (!rsp.isSucceeded()) {
        throw new ClientException("Failed to use space: " + spaceName, rsp.getErrorMessage());
      }
    }
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
    final var summary = new ResultSummaryImpl(time, query, spaceName, connection.getAddress());
    // build out neo4j result
    return new ResultImpl(resultSet, summary);
  }

  Map<byte[], com.vesoft.nebula.Value> toNebulaParameters(Query query) {
    final var parameterMap = query.parameters().asMap();
    final Map<byte[], com.vesoft.nebula.Value> map = new HashMap<>();
    parameterMap.forEach((key, value) -> map.put(key.getBytes(UTF_8), value2Nvalue(value)));
    return map;
  }
}
