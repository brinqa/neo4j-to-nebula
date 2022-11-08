package com.brinqa.nebula.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.AllArgsConstructor;
import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.AsyncTransaction;
import org.neo4j.driver.async.AsyncTransactionWork;
import org.neo4j.driver.async.ResultCursor;

@AllArgsConstructor
public class AsyncSessionImpl implements AsyncSession {

  private final SessionImpl session;

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
  public CompletionStage<AsyncTransaction> beginTransactionAsync() {
    return beginTransactionAsync(TransactionConfig.empty());
  }

  @Override
  public <T> CompletionStage<T> readTransactionAsync(
      AsyncTransactionWork<CompletionStage<T>> work) {
    return readTransactionAsync(work, TransactionConfig.empty());
  }

  @Override
  public <T> CompletionStage<T> writeTransactionAsync(
      AsyncTransactionWork<CompletionStage<T>> work) {
    return readTransactionAsync(work);
  }

  @Override
  public <T> CompletionStage<T> writeTransactionAsync(
      AsyncTransactionWork<CompletionStage<T>> work, TransactionConfig config) {
    return readTransactionAsync(work, config);
  }

  @Override
  public CompletionStage<ResultCursor> runAsync(Query query) {
    return runAsync(query, TransactionConfig.empty());
  }

  @Override
  public CompletionStage<ResultCursor> runAsync(String query, TransactionConfig config) {
    return runAsync(new Query(query), config);
  }

  @Override
  public CompletionStage<ResultCursor> runAsync(
      String query, Map<String, Object> parameters, TransactionConfig config) {
    return runAsync(new Query(query, parameters), config);
  }

  @Override
  public Bookmark lastBookmark() {
    throw new UnsupportedOperationException();
  }

  @Override
  public CompletionStage<Void> closeAsync() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletionStage<AsyncTransaction> beginTransactionAsync(TransactionConfig config) {
    return CompletableFuture.completedFuture(new AsyncTransactionImpl(this, config));
  }

  @Override
  public <T> CompletionStage<T> readTransactionAsync(
      AsyncTransactionWork<CompletionStage<T>> work, TransactionConfig config) {
    return work.execute(new AsyncTransactionImpl(this, config));
  }

  // =========================================================================
  // Implementation
  // =========================================================================

  @Override
  public CompletionStage<ResultCursor> runAsync(Query query, TransactionConfig config) {
    return CompletableFuture.supplyAsync(
        () -> {
          final Result result = session.run(query, config);
          return new ResultCursorImpl(result);
        });
  }
}
