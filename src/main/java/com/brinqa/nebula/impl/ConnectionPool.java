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
