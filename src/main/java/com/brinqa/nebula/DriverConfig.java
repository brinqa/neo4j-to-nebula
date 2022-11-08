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
  public static DriverConfig defaultConfig(String spaceName) {
    return DriverConfig.builder()
        .address(new HostAddress("localhost", 9669))
        .spaceName(spaceName)
        .build();
  }

  @NonNull String spaceName;

  @Singular List<HostAddress> addresses;
  @Default String username = "root";
  @Default String password = "nebula";

  // The max connections in pool for all addresses
  @Default int maxSessions = 100;

  // Socket timeout and Socket connection timeout, unit: millisecond
  int timeout;

  // Set to true to turn on ssl encrypted traffic
  boolean enableSsl;

  // SSL param is required if ssl is turned on
  @NonNull @Default SSLParam sslParam = new NoneSSLParam();
}
