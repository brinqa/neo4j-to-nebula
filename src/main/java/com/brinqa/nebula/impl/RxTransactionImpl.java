package com.brinqa.nebula.impl;

import java.util.Map;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.reactive.RxResult;
import org.neo4j.driver.reactive.RxTransaction;
import org.reactivestreams.Publisher;

public class RxTransactionImpl implements RxTransaction {

  /**
   * Register running of a query and return a reactive result stream. The query is not executed when
   * the reactive result is returned. Instead, the publishers in the result will actually start the
   * execution of the query.
   *
   * <p>This method takes a set of parameters that will be injected into the query by Neo4j. Using
   * parameters is highly encouraged, it helps avoid dangerous cypher injection attacks and improves
   * database performance as Neo4j can re-use query plans more often.
   *
   * <p>This particular method takes a {@link Value} as its input. This is useful if you want to
   * take a map-like value that you've gotten from a prior result and send it back as parameters.
   *
   * <p>If you are creating parameters programmatically, {@link #run(String, Map)} might be more
   * helpful, it converts your map to a {@link Value} for you.
   *
   * @param query text of a Neo4j query
   * @param parameters input parameters, should be a map Value, see {@link
   *     Values#parameters(Object...)}.
   * @return a reactive result.
   */
  @Override
  public RxResult run(String query, Value parameters) {
    return null;
  }

  /**
   * Register running of a query and return a reactive result stream. The query is not executed when
   * the reactive result is returned. Instead, the publishers in the result will actually start the
   * execution of the query.
   *
   * <p>This method takes a set of parameters that will be injected into the query by Neo4j. Using
   * parameters is highly encouraged, it helps avoid dangerous cypher injection attacks and improves
   * database performance as Neo4j can re-use query plans more often.
   *
   * <p>This version of run takes a {@link Map} of parameters. The values in the map must be values
   * that can be converted to Neo4j types. See {@link Values#parameters(Object...)} for a list of
   * allowed types.
   *
   * @param query text of a Neo4j query
   * @param parameters input data for the query
   * @return a reactive result.
   */
  @Override
  public RxResult run(String query, Map<String, Object> parameters) {
    return null;
  }

  /**
   * Register running of a query and return a reactive result stream. The query is not executed when
   * the reactive result is returned. Instead, the publishers in the result will actually start the
   * execution of the query.
   *
   * <p>This method takes a set of parameters that will be injected into the query by Neo4j. Using
   * parameters is highly encouraged, it helps avoid dangerous cypher injection attacks and improves
   * database performance as Neo4j can re-use query plans more often.
   *
   * <p>This version of run takes a {@link Record} of parameters, which can be useful if you want to
   * use the output of one query as input for another.
   *
   * @param query text of a Neo4j query
   * @param parameters input data for the query
   * @return a reactive result.
   */
  @Override
  public RxResult run(String query, Record parameters) {
    return null;
  }

  /**
   * Register running of a query and return a reactive result stream. The query is not executed when
   * the reactive result is returned. Instead, the publishers in the result will actually start the
   * execution of the query.
   *
   * @param query text of a Neo4j query
   * @return a reactive result.
   */
  @Override
  public RxResult run(String query) {
    return null;
  }

  /**
   * Register running of a query and return a reactive result stream. The query is not executed when
   * the reactive result is returned. Instead, the publishers in the result will actually start the
   * execution of the query.
   *
   * @param query a Neo4j query
   * @return a reactive result.
   */
  @Override
  public RxResult run(Query query) {
    return null;
  }

  /**
   * Commits the transaction. It completes without publishing anything if transaction is committed
   * successfully. Otherwise, errors when there is any error to commit.
   *
   * @return an empty publisher.
   */
  @Override
  public <T> Publisher<T> commit() {
    return null;
  }

  /**
   * Rolls back the transaction. It completes without publishing anything if transaction is rolled
   * back successfully. Otherwise, errors when there is any error to roll back.
   *
   * @return an empty publisher.
   */
  @Override
  public <T> Publisher<T> rollback() {
    return null;
  }

  /**
   * Close the transaction. If the transaction has been {@link #commit() committed} or {@link
   * #rollback() rolled back}, the close is optional and no operation is performed. Otherwise, the
   * transaction will be rolled back by default by this method.
   *
   * @return new {@link Publisher} that gets completed when close is successful, otherwise an error
   *     is signalled.
   */
  @Override
  public Publisher<Void> close() {
    return null;
  }
}
