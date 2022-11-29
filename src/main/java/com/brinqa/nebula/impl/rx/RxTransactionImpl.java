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
package com.brinqa.nebula.impl.rx;

import io.reactivex.Flowable;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.reactive.RxResult;
import org.neo4j.driver.reactive.RxTransaction;
import org.reactivestreams.Publisher;

@AllArgsConstructor
public class RxTransactionImpl implements RxTransaction {
  private final RxSessionImpl session;
  private final TransactionConfig transactionConfig;

  @Override
  public RxResult run(String query, Value parameters) {
    return run(new Query(query, parameters));
  }

  @Override
  public RxResult run(String query, Map<String, Object> parameters) {
    return run(new Query(query, parameters));
  }

  @Override
  public RxResult run(String query, Record parameters) {
    return run(new Query(query, parameters.asMap()));
  }

  @Override
  public RxResult run(String query) {
    return run(new Query(query));
  }

  @Override
  public <T> Publisher<T> commit() {
    return Flowable.empty();
  }

  @Override
  public <T> Publisher<T> rollback() {
    return Flowable.empty();
  }

  @Override
  public Publisher<Void> close() {
    return Flowable.empty();
  }

  // =========================================================================
  // Implementation
  // =========================================================================
  @Override
  public RxResult run(Query query) {
    return this.session.run(query, transactionConfig);
  }
}
