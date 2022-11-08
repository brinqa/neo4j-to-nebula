package com.brinqa.nebula.impl;

import com.vesoft.nebula.client.graph.data.CASignedSSLParam;
import com.vesoft.nebula.client.graph.data.SSLParam;
import com.vesoft.nebula.client.graph.data.SelfSignedSSLParam;
import com.vesoft.nebula.util.SslUtil;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class SocketFactoryUtil {

  public static SocketFactory newFactory(final SSLParam sslParam) {
    final var param = (sslParam != null) ? sslParam : NoneSSLParam.INSTANCE;
    switch (param.getSignMode()) {
      case CA_SIGNED:
        return SslUtil.getSSLSocketFactoryWithCA((CASignedSSLParam) param);
      case SELF_SIGNED:
        return SslUtil.getSSLSocketFactoryWithoutCA((SelfSignedSSLParam) param);
      case NONE:
        return SSLSocketFactory.getDefault();
    }
    throw new IllegalArgumentException("Unknown SSL Parameter: " + sslParam);
  }
}
