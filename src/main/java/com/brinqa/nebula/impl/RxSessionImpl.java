package com.brinqa.nebula.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.reactive.RxResult;
import org.neo4j.driver.reactive.RxSession;
import org.neo4j.driver.reactive.RxTransaction;
import org.neo4j.driver.reactive.RxTransactionWork;
import org.reactivestreams.Publisher;

public class RxSessionImpl implements RxSession {

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

  /**
   * Begin a new <em>unmanaged {@linkplain RxTransaction transaction}</em> with the specified {@link
   * TransactionConfig configuration}. At most one transaction may exist in a session at any point
   * in time. To maintain multiple concurrent transactions, use multiple concurrent sessions.
   *
   * <p>It by default is executed in a Network IO thread, as a result no blocking operation is
   * allowed in this thread.
   *
   * @param config configuration for the new transaction.
   * @return a new {@link RxTransaction}
   */
  @Override
  public Publisher<RxTransaction> beginTransaction(TransactionConfig config) {
    return null;
  }

  /**
   * Execute given unit of reactive work in a {@link AccessMode#READ read} reactive transaction with
   * the specified {@link TransactionConfig configuration}.
   *
   * <p>Transaction will automatically be committed unless given unit of work fails or {@link
   * RxTransaction#commit() transaction commit} fails. It will also not be committed if explicitly
   * rolled back via {@link RxTransaction#rollback()}.
   *
   * <p>Returned publisher and given {@link RxTransactionWork} is completed/executed by an IO thread
   * which should never block. Otherwise IO operations on this and potentially other network
   * connections might deadlock. Please do not chain blocking operations like {@link
   * CompletableFuture#get()} on the returned publisher and do not use them inside the {@link
   * RxTransactionWork}.
   *
   * @param work the {@link RxTransactionWork} to be applied to a new read transaction. Operation
   *     executed by the given work must NOT include any blocking operation.
   * @param config the transaction configuration.
   * @return a {@link Publisher publisher} completed with the same result as returned by the given
   *     unit of work. publisher can be completed exceptionally if given work or commit fails.
   */
  @Override
  public <T> Publisher<T> readTransaction(
      RxTransactionWork<? extends Publisher<T>> work, TransactionConfig config) {
    return null;
  }

  /**
   * Run a query in an auto-commit transaction with specified {@link TransactionConfig
   * configuration} and return a reactive result stream. The query is not executed when the reactive
   * result is returned. Instead, the publishers in the result will actually start the execution of
   * the query.
   *
   * <h2>Example</h2>
   *
   * <pre>{@code
   * Map<String, Object> metadata = new HashMap<>();
   * metadata.put("type", "update name");
   *
   * TransactionConfig config = TransactionConfig.builder()
   *                 .withTimeout(Duration.ofSeconds(3))
   *                 .withMetadata(metadata)
   *                 .build();
   *
   * Query query = new Query("MATCH (n) WHERE n.name = $myNameParam RETURN n.age");
   * RxResult result = rxSession.run(query.withParameters(Values.parameters("myNameParam", "Bob")));
   * }</pre>
   *
   * @param query a Neo4j query.
   * @param config configuration for the new transaction.
   * @return a reactive result.
   */
  @Override
  public RxResult run(Query query, TransactionConfig config) {
    return null;
  }

  /**
   * Signal that you are done using this session. In the default driver usage, closing and accessing
   * sessions is very low cost.
   *
   * <p>This operation is not needed if 1) all results created in the session have been fully
   * consumed and 2) all transactions opened by this session have been either committed or rolled
   * back.
   *
   * <p>This method is a fallback if you failed to fulfill the two requirements above. This
   * publisher is completed when all outstanding queries in the session have completed, meaning any
   * writes you performed are guaranteed to be durably stored. It might be completed exceptionally
   * when there are unconsumed errors from previous queries or transactions.
   *
   * @return an empty publisher that represents the reactive close.
   */
  @Override
  public <T> Publisher<T> close() {
    return null;
  }
}
