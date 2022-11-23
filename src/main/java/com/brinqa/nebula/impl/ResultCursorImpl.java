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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.async.ResultCursor;
import org.neo4j.driver.summary.ResultSummary;

/**
 * The Thrift client returns the full result, there's no support for lazy fetch or paginate through
 * large results. This implementation just returns the results.
 */
@RequiredArgsConstructor
public class ResultCursorImpl implements ResultCursor {

  private final Result result;

  @Override
  public List<String> keys() {
    return result.keys();
  }

  @Override
  public CompletionStage<ResultSummary> consumeAsync() {
    return CompletableFuture.completedFuture(result.consume());
  }

  @Override
  public CompletionStage<Record> nextAsync() {
    return CompletableFuture.completedFuture(result.next());
  }

  @Override
  public CompletionStage<Record> peekAsync() {
    return CompletableFuture.completedFuture(result.peek());
  }

  @Override
  public CompletionStage<Record> singleAsync() {
    return CompletableFuture.completedFuture(result.single());
  }

  @Override
  public CompletionStage<ResultSummary> forEachAsync(Consumer<Record> action) {
    result.forEachRemaining(action);
    return consumeAsync();
  }

  @Override
  public CompletionStage<List<Record>> listAsync() {
    return CompletableFuture.completedFuture(result.list());
  }

  @Override
  public <T> CompletionStage<List<T>> listAsync(Function<Record, T> mapFunction) {
    return CompletableFuture.completedFuture(result.list(mapFunction));
  }
}
