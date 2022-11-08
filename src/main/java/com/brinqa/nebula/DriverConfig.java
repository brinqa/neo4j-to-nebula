package com.brinqa.nebula;

import com.brinqa.nebula.impl.NoneSSLParam;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.SSLParam;
import java.util.List;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class DriverConfig {

  /**
   * Simple default configuration.
   *
   * @param spaceName require space name for usage.
   * @return default driver configuration.
   */
  public static DriverConfig defaultConfig(String spaceName) {
    return DriverConfig.builder()
        .address(new HostAddress("localhost", 9669))
        .spaceName(spaceName)
        .build();
  }

  /**
   * @return name of the space to use during driver interactions.
   */
  @NonNull String spaceName;

  /**
   * @return list of host addresses to Graph Service instances.
   */
  @Singular List<HostAddress> addresses;
  /**
   * @return username to authenticate with.
   */
  @Default String username = "root";
  /**
   * @return password to authenticate with.
   */
  @Default String password = "nebula";

  /**
   * @return the max number of sessions for all addresses.
   */
  @Default int maxSessions = 100;

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

  /**
   * @return Set to true to turn on ssl encrypted traffic
   */
  boolean enableSsl;

  // SSL param is required if ssl is turned on
  @NonNull @Default SSLParam sslParam = new NoneSSLParam();
}
