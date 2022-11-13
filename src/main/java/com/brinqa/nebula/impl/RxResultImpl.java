package com.brinqa.nebula.impl;

import java.util.List;
import org.neo4j.driver.Record;
import org.neo4j.driver.reactive.RxResult;
import org.neo4j.driver.summary.ResultSummary;
import org.reactivestreams.Publisher;

public class RxResultImpl implements RxResult {

  @Override
  public Publisher<List<String>> keys() {
    return null;
  }

  @Override
  public Publisher<Record> records() {
    return null;
  }

  @Override
  public Publisher<ResultSummary> consume() {
    return null;
  }
}
