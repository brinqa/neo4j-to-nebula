package com.brinqa.nebula.impl;

import static com.brinqa.nebula.impl.SocketFactoryUtil.newFactory;

import com.brinqa.nebula.DriverConfig;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vesoft.nebula.client.graph.data.HostAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.SocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Metrics;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.reactive.RxSession;
import org.neo4j.driver.types.TypeSystem;

@Slf4j
public class DriverImpl implements Driver {

  // static fields
  private final DriverConfig driverConfig;
  private final SocketFactory socketFactory;

  // active fields
  private final Cache<Long, SessionImpl> sessions;
  private final AtomicInteger roundRobinIdx = new AtomicInteger();

  public DriverImpl(final DriverConfig driverConfig) throws UnknownHostException {
    this.socketFactory =
        driverConfig.isEnableSsl()
            ? newFactory(driverConfig.getSslParam())
            : SocketFactory.getDefault();
    this.driverConfig = driverConfig;

    this.sessions =
        CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .maximumSize(driverConfig.getMaxSessions())
            .<Long, SessionImpl>removalListener(
                notification -> {
                  final var key = notification.getKey();
                  final var session = notification.getValue();
                  switch (notification.getCause()) {
                    case EXPLICIT:
                      break;
                    case REPLACED:
                      break;
                    case COLLECTED:
                      break;
                    case EXPIRED:
                      log.info("Session {} expired.", key);
                      break;
                    case SIZE:
                      log.warn(
                          "Exceeded max number of sessions {} check for leaks.",
                          this.driverConfig.getMaxSessions());
                      assert session != null;
                      session.close();
                      break;
                  }
                })
            .build();
  }

  /**
   * Return a flag to indicate whether encryption is used for this driver.
   *
   * @return true if the driver requires encryption, false otherwise
   */
  @Override
  public boolean isEncrypted() {
    return this.driverConfig.isEnableSsl();
  }

  /**
   * Create a new general purpose {@link Session} with default {@link SessionConfig session
   * configuration}.
   *
   * <p>Alias to {@link #session(SessionConfig)}}.
   *
   * @return a new {@link Session} object.
   */
  @Override
  public Session session() {
    return session(SessionConfig.defaultConfig());
  }

  /**
   * Create a new {@link Session} with a specified {@link SessionConfig session configuration}. Use
   * {@link SessionConfig#forDatabase(String)} to obtain a general purpose session configuration for
   * the specified database.
   *
   * @param sessionConfig specifies session configurations for this session.
   * @return a new {@link Session} object.
   * @see SessionConfig
   */
  @Override
  public Session session(SessionConfig sessionConfig) {
    return newSession(sessionConfig);
  }

  /**
   * Create a new general purpose {@link RxSession} with default {@link SessionConfig session
   * configuration}. The {@link RxSession} provides a reactive way to run queries and process
   * results.
   *
   * <p>Alias to {@link #rxSession(SessionConfig)}}.
   *
   * @return a new {@link RxSession} object.
   */
  @Override
  public RxSession rxSession() {
    return rxSession(SessionConfig.defaultConfig());
  }

  /**
   * Create a new {@link RxSession} with a specified {@link SessionConfig session configuration}.
   * Use {@link SessionConfig#forDatabase(String)} to obtain a general purpose session configuration
   * for the specified database. The {@link RxSession} provides a reactive way to run queries and
   * process results.
   *
   * @param sessionConfig used to customize the session.
   * @return a new {@link RxSession} object.
   */
  @Override
  public RxSession rxSession(SessionConfig sessionConfig) {
    throw new UnsupportedOperationException();
  }

  /**
   * Create a new general purpose {@link AsyncSession} with default {@link SessionConfig session
   * configuration}. The {@link AsyncSession} provides an asynchronous way to run queries and
   * process results.
   *
   * <p>Alias to {@link #asyncSession(SessionConfig)}}.
   *
   * @return a new {@link AsyncSession} object.
   */
  @Override
  public AsyncSession asyncSession() {
    return asyncSession(SessionConfig.defaultConfig());
  }

  /**
   * Create a new {@link AsyncSession} with a specified {@link SessionConfig session configuration}.
   * Use {@link SessionConfig#forDatabase(String)} to obtain a general purpose session configuration
   * for the specified database. The {@link AsyncSession} provides an asynchronous way to run
   * queries and process results.
   *
   * @param sessionConfig used to customize the session.
   * @return a new {@link AsyncSession} object.
   */
  @Override
  public AsyncSession asyncSession(SessionConfig sessionConfig) {
    throw new UnsupportedOperationException();
  }

  /**
   * Close all the resources assigned to this driver, including open connections and IO threads.
   *
   * <p>This operation works the same way as {@link #closeAsync()} but blocks until all resources
   * are closed.
   */
  @Override
  public void close() {
    for (final Session session : new ArrayList<>(sessions.asMap().values())) {
      session.close();
    }
    // remove all the sessions
    sessions.invalidateAll();
  }

  /**
   * Close all the resources assigned to this driver, including open connections and IO threads.
   *
   * <p>This operation is asynchronous and returns a {@link CompletionStage}. This stage is
   * completed with {@code null} when all resources are closed. It is completed exceptionally if
   * termination fails.
   *
   * @return a {@link CompletionStage completion stage} that represents the asynchronous close.
   */
  @Override
  public CompletionStage<Void> closeAsync() {
    return CompletableFuture.supplyAsync(
        () -> {
          close();
          return null;
        });
  }

  /**
   * Returns the driver metrics if metrics reporting is enabled via {@link
   * Config.ConfigBuilder#withDriverMetrics()}. Otherwise, a {@link ClientException} will be thrown.
   *
   * @return the driver metrics if enabled.
   * @throws ClientException if the driver metrics reporting is not enabled.
   */
  @Override
  public Metrics metrics() {
    return null;
  }

  /**
   * Returns true if the driver metrics reporting is enabled via {@link
   * Config.ConfigBuilder#withDriverMetrics()}, otherwise false.
   *
   * @return true if the metrics reporting is enabled.
   */
  @Override
  public boolean isMetricsEnabled() {
    return false;
  }

  /**
   * This will return the type system supported by the driver. The types supported on a particular
   * server a session is connected against might not contain all of the types defined here.
   *
   * @return type system used by this query runner for classifying values
   */
  @Override
  public TypeSystem defaultTypeSystem() {
    throw new UnsupportedOperationException();
  }

  /**
   * This verifies if the driver can connect to a remote server or a cluster by establishing a
   * network connection with the remote and possibly exchanging a few data before closing the
   * connection.
   *
   * <p>It throws exception if fails to connect. Use the exception to further understand the cause
   * of the connectivity problem. Note: Even if this method throws an exception, the driver still
   * need to be closed via {@link #close()} to free up all resources.
   */
  @Override
  public void verifyConnectivity() {
    try {
      verifyConnectivityAsync().toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      Throwables.throwIfUnchecked(e);
      throw new IllegalStateException(Throwables.getRootCause(e));
    }
  }

  /**
   * This verifies if the driver can connect to a remote server or cluster by establishing a network
   * connection with the remote and possibly exchanging a few data before closing the connection.
   *
   * <p>This operation is asynchronous and returns a {@link CompletionStage}. This stage is
   * completed with {@code null} when the driver connects to the remote server or cluster
   * successfully. It is completed exceptionally if the driver failed to connect the remote server
   * or cluster. This exception can be used to further understand the cause of the connectivity
   * problem. Note: Even if this method complete exceptionally, the driver still need to be closed
   * via {@link #closeAsync()} to free up all resources.
   *
   * @return a {@link CompletionStage completion stage} that represents the asynchronous
   *     verification.
   */
  @Override
  public CompletionStage<Void> verifyConnectivityAsync() {
    return CompletableFuture.supplyAsync(
        () -> {
          try (final var session = newSession(SessionConfig.defaultConfig())) {
            session.ping();
          }
          return null;
        });
  }

  /**
   * Returns true if the server or cluster the driver connects to supports multi-databases,
   * otherwise false.
   *
   * @return true if the server or cluster the driver connects to supports multi-databases,
   *     otherwise false.
   */
  @Override
  public boolean supportsMultiDb() {
    return true;
  }

  /**
   * Asynchronous check if the server or cluster the driver connects to supports multi-databases.
   *
   * @return a {@link CompletionStage completion stage} that returns true if the server or cluster
   *     the driver connects to supports multi-databases, otherwise false.
   */
  @Override
  public CompletionStage<Boolean> supportsMultiDbAsync() {
    return CompletableFuture.completedFuture(true);
  }

  // ===========================================================================
  // Internal Methods
  // ===========================================================================

  /**
   * Build a session rotate through the available addresses upto 2x per address to fine a proper
   * session.
   */
  SessionImpl newSession(SessionConfig config) {
    // create new session
    try {
      final var address = hostAddress();
      final var timeout = driverConfig.getTimeout();
      final var username = driverConfig.getUsername();
      final var password = driverConfig.getPassword();
      final var spaceName = config.database().orElse(driverConfig.getSpaceName());

      final var connection =
          new NebulaConnection(address, username, password, timeout, socketFactory);

      return new SessionImpl(this, connection, spaceName);
    } catch (UnknownHostException e) {
      throw new RuntimeException("Get session failed: " + e.getMessage());
    }
  }

  void invalidate(SessionImpl session) {
    this.sessions.invalidate(session.getSessionId());
  }

  HostAddress hostAddress() throws UnknownHostException {
    final var idx = roundRobinIdx.getAndIncrement() % driverConfig.getAddresses().size();
    final var target = driverConfig.getAddresses().get(idx);
    return hostToIp(target);
  }

  static HostAddress hostToIp(HostAddress addr) throws UnknownHostException {
    final var ip = InetAddress.getByName(addr.getHost()).getHostAddress();
    return new HostAddress(ip, addr.getPort());
  }
}
