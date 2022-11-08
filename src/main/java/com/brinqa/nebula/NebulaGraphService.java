package com.brinqa.nebula;

import java.net.UnknownHostException;

import com.brinqa.nebula.impl.DriverImpl;
import org.neo4j.driver.Driver;
import org.neo4j.driver.exceptions.ClientException;

/** This creates a driver instance for Nebula that looks like a Neo4j Driver. */
public class NebulaGraphService {

  /**
   * Based on the configuration provided create a driver to manage a connection to the Nebula
   * cluster.
   *
   * @param config contains all the configuration required to connect and manage space within the
   *     Nebula cluster.
   * @return driver instance of the configuration is valid.
   */
  public static Driver newDriver(final DriverConfig config) {
    try {
      return new DriverImpl(config);
    } catch (UnknownHostException e) {
      throw new ClientException("Unknown host error.", e);
    }
  }
}
