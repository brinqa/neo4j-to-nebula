package com.brinqa.nebula.impl;

import com.vesoft.nebula.client.graph.data.SSLParam;

public class NoneSSLParam extends SSLParam {
  public static final SSLParam INSTANCE = new NoneSSLParam();

  public NoneSSLParam() {
    super(SignMode.NONE);
  }
}
