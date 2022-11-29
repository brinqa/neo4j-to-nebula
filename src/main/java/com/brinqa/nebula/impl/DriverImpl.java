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
package com.brinqa.nebula.impl;

import com.brinqa.nebula.DriverConfig;
import com.brinqa.nebula.impl.async.AsyncSessionImpl;
import com.brinqa.nebula.impl.rx.RxSessionImpl;
import com.google.common.base.Throwables;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
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

  private final DriverConfig driverConfig;
  private final ConnectionPool pool;

  public DriverImpl(final DriverConfig driverConfig) throws UnknownHostException {
    this.driverConfig = driverConfig;
    this.pool = new ConnectionPool(driverConfig);
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
    return new RxSessionImpl(newSession(sessionConfig));
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
    return new AsyncSessionImpl(driverConfig, newSession(sessionConfig));
  }

  /**
   * Close all the resources assigned to this driver, including open connections and IO threads.
   *
   * <p>This operation works the same way as {@link #closeAsync()} but blocks until all resources
   * are closed.
   */
  @Override
  public void close() {
    this.pool.close();
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
   * Returns the driver metrics if metrics reporting is enabled.
   *
   * @return the driver metrics if enabled.
   * @throws ClientException if the driver metrics reporting is not enabled.
   */
  @Override
  public Metrics metrics() {
    return null;
  }

  /**
   * Returns true if the driver metrics reporting is enabled.
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
    try (final var session = newSession(SessionConfig.defaultConfig())) {
      session.ping();
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
          verifyConnectivity();
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
      final var spaceName = config.database().orElse(driverConfig.getSpaceName());
      return new SessionImpl(this.pool, spaceName);
    } catch (Exception e) {
      throw new RuntimeException("Get session failed: " + e.getMessage());
    }
  }
}
