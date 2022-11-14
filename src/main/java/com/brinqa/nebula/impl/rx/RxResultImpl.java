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
