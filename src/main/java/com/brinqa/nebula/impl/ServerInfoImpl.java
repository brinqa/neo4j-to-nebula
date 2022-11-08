package com.brinqa.nebula.impl;

import com.vesoft.nebula.client.graph.data.HostAddress;
import lombok.AllArgsConstructor;
import org.neo4j.driver.summary.ServerInfo;

@AllArgsConstructor
public class ServerInfoImpl implements ServerInfo {

  private final HostAddress address;

  /**
   * Returns a string telling the address of the server the query was executed.
   *
   * @return The address of the server the query was executed.
   */
  @Override
  public String address() {
    return this.address.getHost();
  }

  /**
   * Returns a string telling which version of the server the query was executed. Supported since
   * neo4j 3.1.
   *
   * @return The server version.
   * @deprecated in 4.3, please use {@link ServerInfo#agent()}, {@link
   *     ServerInfo#protocolVersion()}, or call the <i>dbms.components</i> procedure instead.
   *     <b>Method might be removed in the next major release.</b>
   */
  @Override
  public String version() {
    return "";
  }

  /**
   * Returns Bolt protocol version with which the remote server communicates. This is returned as a
   * string in format X.Y where X is the major version and Y is the minor version.
   *
   * @return The Bolt protocol version.
   */
  @Override
  public String protocolVersion() {
    return "";
  }

  /**
   * Returns server agent string by which the remote server identifies itself.
   *
   * @return The agent string.
   */
  @Override
  public String agent() {
    return "";
  }
}
