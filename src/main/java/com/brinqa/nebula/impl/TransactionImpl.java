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
