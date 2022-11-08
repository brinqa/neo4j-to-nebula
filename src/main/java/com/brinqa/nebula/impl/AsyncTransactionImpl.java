package com.brinqa.nebula.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.AllArgsConstructor;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.async.AsyncTransaction;
import org.neo4j.driver.async.ResultCursor;

@AllArgsConstructor
public class AsyncTransactionImpl implements AsyncTransaction {

  private final AsyncSessionImpl asyncSession;
  private final TransactionConfig transactionConfig;

  @Override
  public CompletionStage<ResultCursor> runAsync(Query query) {
    return asyncSession.runAsync(query, transactionConfig);
  }

  @Override
  public CompletionStage<ResultCursor> runAsync(String query, Value parameters) {
    return runAsync(new Query(query, parameters.asMap()));
  }

  @Override
  public CompletionStage<ResultCursor> runAsync(String query, Map<String, Object> parameters) {
    return runAsync(new Query(query, parameters));
  }

  @Override
  public CompletionStage<ResultCursor> runAsync(String query, Record parameters) {
    return runAsync(new Query(query, parameters.asMap()));
  }

  @Override
  public CompletionStage<ResultCursor> runAsync(String query) {
    return runAsync(new Query(query));
  }

  @Override
  public CompletionStage<Void> commitAsync() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletionStage<Void> rollbackAsync() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletionStage<Void> closeAsync() {
    return CompletableFuture.completedFuture(null);
  }
}
