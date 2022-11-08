package com.brinqa.nebula;

import java.util.List;

import com.brinqa.nebula.impl.NonSSLParam;

import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.SSLParam;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class DriverConfig {
  public static DriverConfig defaultConfig(String spaceName) {
    return DriverConfig.builder()
        .address(new HostAddress("localhost", 9669))
        .spaceName(spaceName)
        .build();
  }

  @Singular List<HostAddress> addresses;
  @Default String username = "root";
  @Default String password = "nebula";
  @NonNull String spaceName;
  @Default boolean reconnect = true;

  // The min connections in pool for all addresses
  @Default int minConnections = 0;

  // The max connections in pool for all addresses
  @Default int maxConnections = 10;

  // Socket timeout and Socket connection timeout, unit: millisecond
  int timeout;

  // The idleTime of the connection, unit: millisecond
  // The connection's idle time more than idleTime, it will be delete
  // 0 means never delete
  int idleTime;

  // The interval time to check idle connection, unit ms, -1 means no check
  @Default int intervalIdle = -1;

  // The wait time to get idle connection, unit ms
  int waitTime;

  // The minimum rate of healthy servers to all servers. if 1 it means all servers should be
  // available on init.
  @Default double minClusterHealthRate = 1;

  // Set to true to turn on ssl encrypted traffic
  boolean enableSsl;

  // SSL param is required if ssl is turned on
  @NonNull @Default SSLParam sslParam = new NonSSLParam();
}
