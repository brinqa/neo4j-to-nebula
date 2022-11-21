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
package com.brinqa.nebula.impl.rx;

import com.brinqa.nebula.impl.ResultImpl;
import io.reactivex.Flowable;
import java.util.List;
import lombok.AllArgsConstructor;
import org.neo4j.driver.Record;
import org.neo4j.driver.reactive.RxResult;
import org.neo4j.driver.summary.ResultSummary;
import org.reactivestreams.Publisher;

@AllArgsConstructor
public class RxResultImpl implements RxResult {
  ResultImpl result;

  @Override
  public Publisher<List<String>> keys() {
    return Flowable.just(result.keys());
  }

  @Override
  public Publisher<Record> records() {
    return Flowable.fromIterable(() -> result);
  }

  @Override
  public Publisher<ResultSummary> consume() {
    return Flowable.just(result.consume());
  }
}
