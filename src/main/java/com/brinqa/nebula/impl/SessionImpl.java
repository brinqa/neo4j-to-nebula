package com.brinqa.nebula.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import com.facebook.thrift.TException;
import com.vesoft.nebula.ErrorCode;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.net.AuthResult;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.SyncConnection;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SessionImpl implements Session {

  private final long sessionID;
  private final DriverImpl driver;
  private final NebulaConnection connection;

  /**
   * Run a query and return a result stream.
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
   * <h2>Example</h2>
   *
   * <pre class="doctest:QueryRunnerDocIT#parameterTest">{@code
   * Result result = session.run( "MATCH (n) WHERE n.name = $myNameParam RETURN (n)",
   *                                       Values.parameters( "myNameParam", "Bob" ) );
   * }</pre>
   *
   * @param query text of a Neo4j query
   * @param parameters input parameters, should be a map Value, see {@link
   *     Values#parameters(Object...)}.
   * @return a stream of result values and associated metadata
   */
  @Override
  public Result run(String query, Value parameters) {
    final Query q = new Query(query, parameters);
    return run(q, TransactionConfig.empty());
  }

  /**
   * Run a query and return a result stream.
   *
   * <p>This method takes a set of parameters that will be injected into the query by Neo4j. Using
   * parameters is highly encouraged, it helps avoid dangerous cypher injection attacks and improves
   * database performance as Neo4j can re-use query plans more often.
   *
   * <p>This version of run takes a {@link Map} of parameters. The values in the map must be values
   * that can be converted to Neo4j types. See {@link Values#parameters(Object...)} for a list of
   * allowed types.
   *
   * <h2>Example</h2>
   *
   * <pre class="doctest:QueryRunnerDocIT#parameterTest">{@code
   * Map<String, Object> parameters = new HashMap<String, Object>();
   * parameters.put("myNameParam", "Bob");
   *
   * Result result = session.run( "MATCH (n) WHERE n.name = $myNameParam RETURN (n)",
   *                                       parameters );
   * }</pre>
   *
   * @param query text of a Neo4j query
   * @param parameters input data for the query
   * @return a stream of result values and associated metadata
   */
  @Override
  public Result run(String query, Map<String, Object> parameters) {
    final Query q = new Query(query, parameters);
    return run(q, TransactionConfig.empty());
  }

  /**
   * Run a query and return a result stream.
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
   * @return a stream of result values and associated metadata
   */
  @Override
  public Result run(String query, Record parameters) {
    return run(query, parameters.asMap());
  }

  /**
   * Run a query and return a result stream.
   *
   * @param query text of a Neo4j query
   * @return a stream of result values and associated metadata
   */
  @Override
  public Result run(String query) {
    return run(new Query(query));
  }

  /**
   * Run a query and return a result stream.
   *
   * <h2>Example</h2>
   *
   * <pre class="doctest:QueryRunnerDocIT#queryObjectTest">{@code
   * Query query = new Query( "MATCH (n) WHERE n.name = $myNameParam RETURN n.age" );
   * Result result = session.run( query.withParameters( Values.parameters( "myNameParam", "Bob" )  ) );
   * }</pre>
   *
   * @param query a Neo4j query
   * @return a stream of result values and associated metadata
   */
  @Override
  public Result run(Query query) {
    return run(query, TransactionConfig.empty());
  }

  /**
   * Begin a new <em>unmanaged {@linkplain Transaction transaction}</em>. At most one transaction
   * may exist in a session at any point in time. To maintain multiple concurrent transactions, use
   * multiple concurrent sessions.
   *
   * @return a new {@link Transaction}
   */
  @Override
  public Transaction beginTransaction() {
    return beginTransaction(TransactionConfig.empty());
  }

  /**
   * Begin a new <em>unmanaged {@linkplain Transaction transaction}</em> with the specified {@link
   * TransactionConfig configuration}. At most one transaction may exist in a session at any point
   * in time. To maintain multiple concurrent transactions, use multiple concurrent sessions.
   *
   * @param config configuration for the new transaction.
   * @return a new {@link Transaction}
   */
  @Override
  public Transaction beginTransaction(TransactionConfig config) {
    return new TransactionImpl(this, config);
  }

  /**
   * Execute a unit of work in a managed {@link AccessMode#READ read} transaction.
   *
   * <p>This transaction will automatically be committed unless an exception is thrown during query
   * execution or by the user code.
   *
   * <p>Managed transactions should not generally be explicitly committed (via {@link
   * Transaction#commit()}).
   *
   * @param work the {@link TransactionWork} to be applied to a new read transaction.
   * @return a result as returned by the given unit of work.
   */
  @Override
  public <T> T readTransaction(TransactionWork<T> work) {
    return readTransaction(work, TransactionConfig.empty());
  }

  /**
   * Execute a unit of work in a managed {@link AccessMode#READ read} transaction with the specified
   * {@link TransactionConfig configuration}.
   *
   * <p>This transaction will automatically be committed unless an exception is thrown during query
   * execution or by the user code.
   *
   * <p>Managed transactions should not generally be explicitly committed (via {@link
   * Transaction#commit()}).
   *
   * @param work the {@link TransactionWork} to be applied to a new read transaction.
   * @param config configuration for all transactions started to execute the unit of work.
   * @return a result as returned by the given unit of work.
   */
  @Override
  public <T> T readTransaction(TransactionWork<T> work, TransactionConfig config) {
    return work.execute(new TransactionImpl(this, config));
  }

  /**
   * Execute a unit of work in a managed {@link AccessMode#WRITE write} transaction.
   *
   * <p>This transaction will automatically be committed unless an exception is thrown during query
   * execution or by the user code.
   *
   * <p>Managed transactions should not generally be explicitly committed (via {@link
   * Transaction#commit()}).
   *
   * @param work the {@link TransactionWork} to be applied to a new write transaction.
   * @return a result as returned by the given unit of work.
   */
  @Override
  public <T> T writeTransaction(TransactionWork<T> work) {
    return readTransaction(work);
  }

  /**
   * Execute a unit of work in a managed {@link AccessMode#WRITE write} transaction with the
   * specified {@link TransactionConfig configuration}.
   *
   * <p>This transaction will automatically be committed unless an exception is thrown during query
   * execution or by the user code.
   *
   * <p>Managed transactions should not generally be explicitly committed (via {@link
   * Transaction#commit()}).
   *
   * @param work the {@link TransactionWork} to be applied to a new write transaction.
   * @param config configuration for all transactions started to execute the unit of work.
   * @return a result as returned by the given unit of work.
   */
  @Override
  public <T> T writeTransaction(TransactionWork<T> work, TransactionConfig config) {
    return readTransaction(work, config);
  }

  /**
   * Run a query in a managed auto-commit transaction with the specified {@link TransactionConfig
   * configuration}, and return a result stream.
   *
   * @param query text of a Neo4j query.
   * @param config configuration for the new transaction.
   * @return a stream of result values and associated metadata.
   */
  @Override
  public Result run(String query, TransactionConfig config) {
    return run(new Query(query), config);
  }

  /**
   * Run a query with parameters in a managed auto-commit transaction with the specified {@link
   * TransactionConfig configuration}, and return a result stream.
   *
   * <p>This method takes a set of parameters that will be injected into the query by Neo4j. Using
   * parameters is highly encouraged, it helps avoid dangerous cypher injection attacks and improves
   * database performance as Neo4j can re-use query plans more often.
   *
   * <p>This version of run takes a {@link Map} of parameters. The values in the map must be values
   * that can be converted to Neo4j types. See {@link Values#parameters(Object...)} for a list of
   * allowed types.
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
   * Map<String, Object> parameters = new HashMap<>();
   * parameters.put("myNameParam", "Bob");
   *
   * Result result = session.run("MATCH (n) WHERE n.name = $myNameParam RETURN (n)", parameters, config);
   * }</pre>
   *
   * @param query text of a Neo4j query.
   * @param parameters input data for the query.
   * @param config configuration for the new transaction.
   * @return a stream of result values and associated metadata.
   */
  @Override
  public Result run(String query, Map<String, Object> parameters, TransactionConfig config) {
    return run(new Query(query, parameters), config);
  }

  /**
   * Run a query in a managed auto-commit transaction with the specified {@link TransactionConfig
   * configuration}, and return a result stream.
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
   * Result result = session.run(query.withParameters(Values.parameters("myNameParam", "Bob")));
   * }</pre>
   *
   * @param query a Neo4j query.
   * @param config configuration for the new transaction.
   * @return a stream of result values and associated metadata.
   */
  @Override
  public Result run(Query query, TransactionConfig config) {
    try {
      final ResultSet resultSet = session.execute(query.text());
      return new ResultImpl(resultSet);
    } catch (IOErrorException e) {
      // NOTE:
      throw new RuntimeException(e);
    }
  }

  /**
   * Return the bookmark received following the last completed {@linkplain Transaction transaction}.
   * If no bookmark was received or if this transaction was rolled back, the bookmark value will be
   * null.
   *
   * @return a reference to a previous transaction
   */
  @Override
  public Bookmark lastBookmark() {
    throw new UnsupportedOperationException();
  }

  /**
   * Reset the current session. This sends an immediate RESET signal to the server which both
   * interrupts any query that is currently executing and ignores any subsequently queued queries.
   * Following the reset, the current transaction will have been rolled back and any outstanding
   * failures will have been acknowledged.
   *
   * @deprecated This method should not be used and violates the expected usage pattern of {@link
   *     Session} objects. They are expected to be not thread-safe and should not be shared between
   *     thread. However this method is only useful when {@link Session} object is passed to another
   *     monitoring thread that calls it when appropriate. It is not useful when {@link Session} is
   *     used in a single thread because in this case {@link #close()} can be used. Since version
   *     3.1, Neo4j database allows users to specify maximum transaction execution time and contains
   *     procedures to list and terminate running queries. These functions should be used instead of
   *     calling this method.
   */
  @Override
  public void reset() {
    throw new UnsupportedOperationException();
  }

  /**
   * Signal that you are done using this session. In the default driver usage, closing and accessing
   * sessions is very low cost.
   */
  @Override
  public void close() {
    if (available.getAndSet(false)) {
      session.release();
    }
  }

  /**
   * Detect whether this resource is still open
   *
   * @return true if the resource is open
   */
  @Override
  public boolean isOpen() {
    return available.get();
  }


  //===========================================================================
  // Internal Methods
  //===========================================================================

}
