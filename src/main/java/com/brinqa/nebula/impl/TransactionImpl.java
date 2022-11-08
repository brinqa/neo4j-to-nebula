package com.brinqa.nebula.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AllArgsConstructor;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.Value;

@AllArgsConstructor
public class TransactionImpl implements Transaction {

  private final SessionImpl session;
  private final TransactionConfig config;

  private final AtomicBoolean openState = new AtomicBoolean(true);

  @Override
  public Result run(String query, Value parameters) {
    return session.run(new Query(query, parameters), config);
  }

  @Override
  public Result run(String query, Map<String, Object> parameters) {
    return session.run(query, parameters, config);
  }

  @Override
  public Result run(String query, Record parameters) {
    return session.run(new Query(query, parameters.asMap()), config);
  }

  @Override
  public Result run(String query) {
    return session.run(query, config);
  }

  @Override
  public Result run(Query query) {
    return session.run(query);
  }

  @Override
  public void commit() {
    // no transactions in nebula
  }

  @Override
  public void rollback() {
    // no transactions in nebula
  }

  @Override
  public void close() {
    openState.set(false);
  }

  @Override
  public boolean isOpen() {
    return openState.get();
  }
}
