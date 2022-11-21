/*
 * Copyright 2002 Brinqa, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
