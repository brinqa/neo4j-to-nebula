package com.brinqa.nebula.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.facebook.thrift.TException;
import com.facebook.thrift.protocol.TCompactProtocol;
import com.facebook.thrift.transport.TSocket;
import com.vesoft.nebula.ErrorCode;
import com.vesoft.nebula.Value;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.net.AuthResult;
import com.vesoft.nebula.graph.GraphService;
import com.vesoft.nebula.graph.GraphService.Client;
import com.vesoft.nebula.graph.VerifyClientVersionReq;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.SocketFactory;
import lombok.Getter;
import org.neo4j.driver.exceptions.ClientException;

public class Connection implements Closeable {

  private final Client client;
  private final TSocket transport;
  @Getter private final long sessionId;
  @Getter private final int timezoneOffset;
  @Getter private final HostAddress address;

  private final AtomicReference<String> currentSpace = new AtomicReference<>();

  public Connection(
      final HostAddress address,
      final String username,
      final String password,
      final int timeout,
      final SocketFactory socketFactory) {
    try {
      this.address = address;

      final int tm = timeout <= 0 ? Integer.MAX_VALUE : timeout;
      final var socket = socketFactory.createSocket(address.getHost(), address.getPort());
      this.transport = new TSocket(socket, tm, tm);
      this.client = new GraphService.Client(new TCompactProtocol(transport));

      // check if client version matches server version
      final var resp = client.verifyClientVersion(new VerifyClientVersionReq());
      if (resp.error_code != ErrorCode.SUCCEEDED) {
        client.getInputProtocol().getTransport().close();
        throw new ClientException(new String(resp.getError_msg(), UTF_8));
      }

      // authenticate the connection to the server
      final var authResult = authenticate(username, password);
      this.sessionId = authResult.getSessionId();
      this.timezoneOffset = authResult.getTimezoneOffset();

    } catch (AuthFailedException | IOException e) {
      throw new ClientException("Unable to connect to Graph server.", e);
    }
  }

  public boolean isOpen() {
    return this.transport.isOpen();
  }

  /** Clients are not thread safe. */
  public synchronized ResultSet execute(String stmt, Map<byte[], Value> parameterMap) {
    final var resp = client.executeWithParameter(sessionId, stmt.getBytes(UTF_8), parameterMap);
    return new ResultSet(resp, getTimezoneOffset());
  }

  private AuthResult authenticate(String user, final String password) throws AuthFailedException {
    try {
      final var usr = user.getBytes(UTF_8);
      final var pwd = password.getBytes(UTF_8);
      final var resp = client.authenticate(usr, pwd);
      if (resp.error_code != ErrorCode.SUCCEEDED) {
        if (resp.error_msg != null) {
          throw new AuthFailedException(new String(resp.error_msg, UTF_8));
        } else {
          throw new AuthFailedException(
              "The error_msg is null, maybe the service not set or the response is disorder.");
        }
      }
      return new AuthResult(resp.getSession_id(), resp.getTime_zone_offset_seconds());
    } catch (TException e) {
      throw new AuthFailedException(String.format("Authenticate failed: %s", e.getMessage()));
    }
  }

  /**
   * Closes this stream and releases any system resources associated with it. If the stream is
   * already closed then invoking this method has no effect.
   *
   * <p>As noted in {@link AutoCloseable#close()}, cases where the close may fail require careful
   * attention. It is strongly advised to relinquish the underlying resources and to internally
   * <em>mark</em> the {@code Closeable} as closed, prior to throwing the {@code IOException}.
   *
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    try {
      // attempt to sign out the session first
      this.client.signout(this.sessionId);
    } finally {
      this.transport.close();
    }
  }

  public synchronized boolean updateCurrentSpace(String space) {
    return !this.currentSpace.compareAndSet(space, space);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Connection)) {
      return false;
    }
    Connection that = (Connection) o;
    return getSessionId() == that.getSessionId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSessionId());
  }
}
