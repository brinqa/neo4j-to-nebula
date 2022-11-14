package com.brinqa.nebula.impl.rx;

import java.util.Map;

import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.reactive.RxResult;
import org.neo4j.driver.reactive.RxSession;
import org.neo4j.driver.reactive.RxTransaction;
import org.neo4j.driver.reactive.RxTransactionWork;

import io.reactivex.Flowable;

import com.brinqa.nebula.impl.SessionImpl;

import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

@AllArgsConstructor
public class RxSessionImpl implements RxSession {
  private final SessionImpl session;

  @Override
  public RxResult run(String query, Value parameters) {
    return run(new Query(query, parameters.asMap()));
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
  public RxResult run(Query query) {
    return run(query, TransactionConfig.empty());
  }

  @Override
  public RxResult run(String query, TransactionConfig config) {
    return run(new Query(query), config);
  }

  @Override
  public RxResult run(String query, Map<String, Object> parameters, TransactionConfig config) {
    return run(new Query(query, parameters), config);
  }

  @Override
  public Publisher<RxTransaction> beginTransaction() {
    return beginTransaction(TransactionConfig.empty());
  }

  @Override
  public <T> Publisher<T> readTransaction(RxTransactionWork<? extends Publisher<T>> work) {
    return readTransaction(work, TransactionConfig.empty());
  }

  @Override
  public <T> Publisher<T> writeTransaction(RxTransactionWork<? extends Publisher<T>> work) {
    return readTransaction(work);
  }

  @Override
  public <T> Publisher<T> writeTransaction(
      RxTransactionWork<? extends Publisher<T>> work, TransactionConfig config) {
    return readTransaction(work, config);
  }

  @Override
  public Bookmark lastBookmark() {
    throw new UnsupportedOperationException();
  }

  // =========================================================================a
  // Implementation
  // =========================================================================
  @Override
  public Publisher<RxTransaction> beginTransaction(TransactionConfig config) {
    return Flowable.just(new RxTransactionImpl(this, config));
  }

  @Override
  public <T> Publisher<T> readTransaction(
      RxTransactionWork<? extends Publisher<T>> work, TransactionConfig config) {
    return Flowable.defer(() -> work.execute(new RxTransactionImpl(this, config)));
  }

  @Override
  public RxResult run(Query query, TransactionConfig config) {
    final var result = session.executeQuery(query, config);
    return new RxResultImpl(result);
  }

  @Override
  public <T> Publisher<T> close() {
    return Flowable.empty();
  }
}
