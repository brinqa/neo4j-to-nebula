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

import com.brinqa.nebula.DriverConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class ConnectionPool extends GenericObjectPool<Connection> {

  /** */
  public ConnectionPool(DriverConfig driverConfig) {
    super(new ConnectionPoolFactory(driverConfig), toGenericPoolConfig(driverConfig));
  }

  static GenericObjectPoolConfig toGenericPoolConfig(DriverConfig driverConfig) {
    final var cfg = new GenericObjectPoolConfig();
    cfg.setMinIdle(cfg.getMinIdle());
    cfg.setMaxIdle(driverConfig.getIdleTime());
    cfg.setMaxTotal(driverConfig.getMaxSessions());
    cfg.setMaxWaitMillis(driverConfig.getWaitTime());
    return cfg;
  }
}
