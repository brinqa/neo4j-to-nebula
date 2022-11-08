package com.brinqa.nebula.impl;

import java.util.Map;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class NebulaQuery {
  /** Query string that supports named parameters. */
  String query;
  /** Named parameters to substitute into the query. */
  @Singular Map<String, Object> parameters;
}
