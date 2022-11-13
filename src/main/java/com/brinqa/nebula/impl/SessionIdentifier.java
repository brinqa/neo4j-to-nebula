package com.brinqa.nebula.impl;

import com.vesoft.nebula.client.graph.data.HostAddress;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class SessionIdentifier {
  HostAddress hostAddress;
  @NonNull String username;
  @NonNull String password;
}
