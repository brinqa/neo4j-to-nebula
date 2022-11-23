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
package com.brinqa.nebula.impl.async;

import com.brinqa.nebula.DriverConfig;
import com.brinqa.nebula.impl.ResultCursorImpl;
import com.brinqa.nebula.impl.SessionImpl;
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

  private final DriverConfig driverConfig;
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
  public CompletionStage<Void> closeAsync() {
    return CompletableFuture.supplyAsync(
        () -> {
          session.close();
          return null;
        });
  }

  @Override
  public CompletionStage<ResultCursor> runAsync(Query query, TransactionConfig config) {
    // FIXME: create an exception so its easier to track the stack trace
    // so if this does fail it can be set for the return
    return CompletableFuture.supplyAsync(
        () -> {
          final Result result = session.run(query, config);
          return new ResultCursorImpl(result);
        });
  }
}
