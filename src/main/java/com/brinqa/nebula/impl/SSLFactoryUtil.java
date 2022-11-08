package com.brinqa.nebula.impl;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.vesoft.nebula.client.graph.data.CASignedSSLParam;
import com.vesoft.nebula.client.graph.data.SSLParam;
import com.vesoft.nebula.client.graph.data.SelfSignedSSLParam;
import com.vesoft.nebula.util.SslUtil;

public class SSLFactoryUtil {

  public static SocketFactory buildFactory(SSLParam sslParam) {
    if (null == sslParam) {
      return SSLSocketFactory.getDefault();
    }
    switch (sslParam.getSignMode()) {
      case CA_SIGNED:
        return SslUtil.getSSLSocketFactoryWithCA((CASignedSSLParam) sslParam);
      case SELF_SIGNED:
        return SslUtil.getSSLSocketFactoryWithoutCA((SelfSignedSSLParam) sslParam);
      case NONE:
        return SSLSocketFactory.getDefault();
    }
    throw new IllegalArgumentException("Unknown SSL Parameter: " + sslParam);
  }
}
