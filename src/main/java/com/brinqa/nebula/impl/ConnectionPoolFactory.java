package com.brinqa.nebula.impl;

import static com.brinqa.nebula.impl.SocketFactoryUtil.newFactory;

import com.brinqa.nebula.DriverConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.SocketFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * The Nebula Session ID is per connection because of auth and this connection is not thread-safe
 * because the client is not thread safe. For efficiency the connection/client must be pooled.
 */
public class ConnectionPoolFactory extends BasePooledObjectFactory<Connection> {

  private final DriverConfig driverConfig;
  private final SocketFactory socketFactory;
  private final AtomicInteger roundRobinIdx = new AtomicInteger();

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
    final var address = hostAddress();
    final var timeout = driverConfig.getTimeout();
    final var username = driverConfig.getUsername();
    final var password = driverConfig.getPassword();
    return new Connection(address, username, password, timeout, socketFactory);
  }

  /**
   * Close the connection.
   *
   * @param p a {@code PooledObject} wrapping the instance to be destroyed
   * @throws Exception
   */
  @Override
  public void destroyObject(PooledObject<Connection> p) throws Exception {
    p.getObject().close();
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
