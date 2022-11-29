/*
 * Copyright 2022 Brinqa, Inc. All rights reserved.
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
package com.brinqa.nebula;

import com.brinqa.nebula.impl.DriverImpl;
import java.net.UnknownHostException;
import org.neo4j.driver.Driver;
import org.neo4j.driver.exceptions.ClientException;

/** This creates a driver instance for Nebula that looks like a Neo4j Driver. */
public class NebulaGraphService {

  /**
   * Based on the configuration provided create a driver to manage a connection pool to the Nebula
   * cluster.
   *
   * @param config contains all the configuration required to connect and manage a space within the
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
