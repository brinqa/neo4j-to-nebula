package com.brinqa.nebula;

import com.brinqa.nebula.impl.DriverImpl;
import com.vesoft.nebula.client.graph.SessionsManagerConfig;
import org.neo4j.driver.Driver;

/** This creates a driver instance for Nebula that looks like a Neo4j Driver. */
public class NebulaGraphService {

  public static Driver newDriver(final SessionsManagerConfig config) {
    return new DriverImpl(config);
  }
}
