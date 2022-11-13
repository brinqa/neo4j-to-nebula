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
import java.util.concurrent.atomic.AtomicReference;
import javax.net.SocketFactory;
import lombok.Getter;
import org.neo4j.driver.exceptions.ClientException;

public class Connection implements Closeable {
  public static long NO_SESSION = -1L;

  private final Client client;
  private final TSocket transport;
  @Getter private final SessionData sessionData;
  @Getter private final SessionIdentifier sessionIdentifier;

  private final AtomicReference<String> currentSpace = new AtomicReference<>();

  public Connection(
      final SessionData data,
      final SessionIdentifier identifier,
      final int timeout,
      final SocketFactory socketFactory) {
    try {
      this.sessionIdentifier = identifier;
      final var address = identifier.getHostAddress();
      final int tm = timeout <= 0 ? Integer.MAX_VALUE : timeout;
      final var socket = socketFactory.createSocket(address.getHost(), address.getPort());
      this.transport = new TSocket(socket, tm, tm);
      this.client = new GraphService.Client(new TCompactProtocol(transport));

      // authenticate the connection to the server
      if (null != data) {
        this.sessionData = data;
      } else {
        // check if client version matches server version
        final var resp = client.verifyClientVersion(new VerifyClientVersionReq());
        if (resp.error_code != ErrorCode.SUCCEEDED) {
          client.getInputProtocol().getTransport().close();
          throw new ClientException(new String(resp.getError_msg(), UTF_8));
        }

        final var username = identifier.getUsername();
        final var password = identifier.getPassword();
        final var authResult = authenticate(username, password);
        final var sessionId = authResult.getSessionId();
        final var timezoneOffset = authResult.getTimezoneOffset();
        this.sessionData =
            SessionData.builder().sessionId(sessionId).timezoneOffset(timezoneOffset).build();
      }

    } catch (AuthFailedException | IOException e) {
      throw new ClientException("Unable to connect to Graph server.", e);
    }
  }

  public HostAddress getAddress() {
    return this.sessionIdentifier.getHostAddress();
  }

  public boolean isOpen() {
    return this.transport.isOpen();
  }

  /** Clients are not thread safe. */
  public synchronized ResultSet execute(String stmt, Map<byte[], Value> parameterMap) {
    final var sessionId = this.sessionData.getSessionId();
    final var resp = client.executeWithParameter(sessionId, stmt.getBytes(UTF_8), parameterMap);
    return new ResultSet(resp, this.sessionData.getTimezoneOffset());
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

  public void expireSession() {
    // attempt to sign out the session first
    this.client.signout(this.sessionData.getSessionId());
  }

  @Override
  public void close() throws IOException {
    this.transport.close();
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
    return this.sessionData.equals(that.sessionData);
  }

  @Override
  public int hashCode() {
    return this.sessionData.hashCode();
  }
}
