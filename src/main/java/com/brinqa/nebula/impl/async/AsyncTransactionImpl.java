/*
 * Copyright 2002 Brinqa, Inc. All rights reserved.
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

  // =========================================================================
  // Implementation
  // =========================================================================
  @Override
  public CompletionStage<ResultCursor> runAsync(Query query) {
    return asyncSession.runAsync(query, transactionConfig);
  }
}
