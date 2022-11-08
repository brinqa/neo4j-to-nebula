package com.brinqa.nebula.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import javax.net.SocketFactory;

import com.google.common.base.Charsets;

import org.neo4j.driver.exceptions.ClientException;

import com.facebook.thrift.TException;
import com.facebook.thrift.protocol.TCompactProtocol;
import com.facebook.thrift.transport.TSocket;
import com.facebook.thrift.transport.TTransportException;
import com.vesoft.nebula.ErrorCode;
import com.vesoft.nebula.Value;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.net.AuthResult;
import com.vesoft.nebula.graph.ExecutionResponse;
import com.vesoft.nebula.graph.GraphService;
import com.vesoft.nebula.graph.GraphService.Client;
import com.vesoft.nebula.graph.VerifyClientVersionReq;

public class NebulaConnection implements Closeable {

  private final long sessionId;
  private final Client client;
  private final TSocket transport;
  private final int timezoneOffset;
  // created

  public NebulaConnection(
      final HostAddress address,
      final String username,
      final String password,
      final int timeout,
      final SocketFactory socketFactory) {
    try {
      final int tm = timeout <= 0 ? Integer.MAX_VALUE : timeout;
      final var socket = socketFactory.createSocket(address.getHost(), address.getPort());
      this.transport = new TSocket(socket, tm, tm);
      this.client = new GraphService.Client(new TCompactProtocol(transport));

      // check if client version matches server version
      final var resp = client.verifyClientVersion(new VerifyClientVersionReq());
      if (resp.error_code != ErrorCode.SUCCEEDED) {
        client.getInputProtocol().getTransport().close();
        throw new ClientException(new String(resp.getError_msg(), Charsets.UTF_8));
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

  public ExecutionResponse execute(String stmt, Map<byte[], Value> parameterMap)
      throws IOErrorException {
    try {
      return client.executeWithParameter(sessionId, stmt.getBytes(), parameterMap);
    } catch (TException e) {
      if (e instanceof TTransportException) {
        TTransportException te = (TTransportException) e;
        if (te.getType() == TTransportException.END_OF_FILE) {
          throw new IOErrorException(IOErrorException.E_CONNECT_BROKEN, te.getMessage());
        } else if (te.getType() == TTransportException.NOT_OPEN) {
          throw new IOErrorException(IOErrorException.E_NO_OPEN, te.getMessage());
        } else if (te.getType() == TTransportException.TIMED_OUT
            || te.getMessage().contains("Read timed out")) {
          throw new IOErrorException(IOErrorException.E_TIME_OUT, te.getMessage());
        }
      }
      throw new IOErrorException(IOErrorException.E_UNKNOWN, e.getMessage());
    }
  }

  public long getSessionId() {
    return sessionId;
  }

  public int getTimezoneOffset() {
    return timezoneOffset;
  }

  AuthResult authenticate(final String user, final String password) throws AuthFailedException {
    try {
      final var resp = client.authenticate(user.getBytes(), password.getBytes());
      if (resp.error_code != ErrorCode.SUCCEEDED) {
        if (resp.error_msg != null) {
          throw new AuthFailedException(new String(resp.error_msg));
        } else {
          throw new AuthFailedException(
              "The error_msg is null, " + "maybe the service not set or the response is disorder.");
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
}
