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

import static com.brinqa.nebula.impl.SocketFactoryUtil.newFactory;

import com.brinqa.nebula.DriverConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.SocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

/**
 * The Nebula Session ID is per connection because of auth and this connection is not thread-safe
 * because the client is not thread safe. For efficiency the connection/client must be pooled.
 */
@Slf4j
public class ConnectionPoolFactory extends BasePooledObjectFactory<Connection> {

  private final DriverConfig driverConfig;
  private final SocketFactory socketFactory;
  private final AtomicInteger roundRobinIdx = new AtomicInteger();

  /**
   * Session IDs are persistent, limited, and can survive for 8hrs or more so its important to
   * re-use them, make sure any connection to a Graph Service maintains the same session ID and
   * knows how to refresh it.
   */
  private final Map<SessionIdentifier, SessionData> identifier2Data = new HashMap<>();

  public ConnectionPoolFactory(DriverConfig driverConfig) {
    this.socketFactory =
        driverConfig.isEnableSsl()
            ? newFactory(driverConfig.getSslParam())
            : SocketFactory.getDefault();
    this.driverConfig = driverConfig;
  }

  /**
   * Creates an object instance, to be wrapped in a {@link PooledObject}.
   *
   * <p>This method <strong>must</strong> support concurrent, multi-threaded activation.
   *
   * @return an instance to be served by the pool
   * @throws Exception if there is a problem creating a new instance, this will be propagated to the
   *     code requesting an object.
   */
  @Override
  public Connection create() throws Exception {
    Exception lastException = null;
    // try everyone at least twice
    final var timeout = driverConfig.getTimeout();
    final int tries = driverConfig.getAddresses().size() * 2;
    for (int i = 0; i < tries; i++) {
      // rotate through addresses
      final var identifier =
          SessionIdentifier.builder()
              .hostAddress(hostAddress())
              .username(driverConfig.getUsername())
              .password(driverConfig.getPassword())
              .build();
      try {
        synchronized (this) {
          // check if there's existing data
          final var data = this.identifier2Data.get(identifier);
          final var c = new Connection(data, identifier, timeout, socketFactory);
          // save off the session data
          this.identifier2Data.put(identifier, c.getSessionData().incrementRef());
          return c;
        }
      } catch (Exception ex) {
        // TODO: check for any exceptions that would invalidate the session data
        log.warn("Unable to connect to host address {}", identifier.getHostAddress(), ex);
        lastException = ex;
      }
    }
    throw new ServiceUnavailableException("Unable to find a usable address.", lastException);
  }

  /**
   * Close the connection.
   *
   * @param p a {@code PooledObject} wrapping the instance to be destroyed
   * @throws Exception
   */
  @Override
  public void destroyObject(PooledObject<Connection> p) throws Exception {
    final var connection = p.getObject();
    final var sessionIdentifier = connection.getSessionIdentifier();
    synchronized (this) {
      final var sessionData = this.identifier2Data.get(sessionIdentifier).decrementRef();
      try {
        // replace session data
        if (0 != sessionData.getReferenceCount()) {
          // simply replace, there's more connections associated with this identifier
          this.identifier2Data.put(sessionIdentifier, sessionData);
        } else {
          // all connections are closed, expire the session
          try {
            connection.expireSession();
          } catch (Exception ex) {
            log.error("Unable to expire session.", ex);
          } finally {
            this.identifier2Data.remove(sessionIdentifier);
          }
        }
      } finally {
        try {
          connection.close();
        } catch (IOException ioe) {
          log.error("Failure during closure of a connection.", ioe);
        }
      }
    }
  }

  /**
   * Insure the {@link Connection} is still open.
   *
   * @param p a {@code PooledObject} wrapping the instance to be validated
   * @return true if still connected otherwise false.
   */
  @Override
  public boolean validateObject(PooledObject<Connection> p) {
    return p.getObject().isOpen();
  }

  /**
   * Wrap the provided instance with an implementation of {@link PooledObject}.
   *
   * @param obj the instance to wrap
   * @return The provided instance, wrapped by a {@link PooledObject}
   */
  @Override
  public PooledObject<Connection> wrap(Connection obj) {
    return new DefaultPooledObject<>(obj);
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
