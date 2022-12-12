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

import java.io.File;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import com.brinqa.nebula.DriverConfig;
import com.brinqa.nebula.NebulaGraphService;

import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.exception.NotValidConnectionException;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
public class BasicCypherTest {
  private static final String SPACE_NAME = "test_space";

  @Container
  public static DockerComposeContainer<?> environment =
      new DockerComposeContainer(new File("docker/docker-compose.yml"))
          .withExposedService("graphd_1", 9669);

  /** Setup the space for testing. */
  @BeforeAll
  public static void setup()
      throws UnknownHostException, IOErrorException, AuthFailedException,
          ClientServerIncompatibleException, NotValidConnectionException {
    final var cfg = DriverConfig.defaultConfig(SPACE_NAME);
    log.info("DriverConfig: {}", cfg);

    String[] queries =
        new String[] {
          // clear out
          "DROP SPACE IF EXISTS test_space;",
          // create new space
          "CREATE SPACE test_space(partition_num=10, replica_factor=1, vid_type=INT64);",
          // create tag
          "USE test_space;",
          "CREATE TAG Host(name string, ipAddress string);",
          "CREATE EDGE like (likeness double);"
        };

    final var pool = new NebulaPool();
    final var poolCfg = new NebulaPoolConfig();
    try {
      pool.init(cfg.getAddresses(), poolCfg);
      var session = pool.getSession("root", "nebula", false);
      try {
        session.execute(String.join(" ", queries));
        waitForSpaceToExist(session);
      } finally {
        session.release();
      }
    } finally {
      pool.close();
    }

    // create driver
    DriverConfig driverConfig = DriverConfig.defaultConfig(SPACE_NAME);
    driver = NebulaGraphService.newDriver(driverConfig);
  }

  static void waitForSpaceToExist(com.vesoft.nebula.client.graph.net.Session session) {
    // wait for it to setup and test
    int i = 0;
    for (; i < 20; i++) {
      log.info("Waiting for SPACE to exist.");
      try {
        final ResultSet rs = session.execute("USE test_space; MATCH (n:Host) RETURN n LIMIT 1;");
        if (rs.isSucceeded()) {
          break;
        }
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      } catch (IOErrorException e) {
        log.warn("I/O Error encountered retrying.", e);
      }
    }
    if (20 == i) {
      throw new IllegalStateException("Unable to setup nebula.");
    }
  }

  @AfterAll
  public static void destroy() {
    driver.close();
  }

  static Driver driver;

  @Test
  @Order(1)
  public void testUseSpaceReturnNone() {
    try (Session session = driver.session()) {
      final var r = session.run("MATCH (n:Host) RETURN n LIMIT 1;");
      assertTrue(r.list().isEmpty());
    }
  }

  /** Simple insert with simple read. */
  @Test
  @Order(2)
  public void testReadObjects() {
    // create a schema tag
    try (Session session = driver.session()) {
      final String INSERT_FMT = "INSERT VERTEX Host (%s) VALUES %d:(%s)";
      final var sampleNames = String.join(",", "name", "ipAddress");
      final var sampleValues = String.join(",", "\"bob\"", "\"192.168.1.1\"");
      final var insertSampleQuery = String.format(INSERT_FMT, sampleNames, 10L, sampleValues);
      final var insertSampleResult = session.run(insertSampleQuery);
      final var insertSampleList = insertSampleResult.list();
      assertTrue(insertSampleList.isEmpty());
      assertNotNull(insertSampleResult.consume());
      final String INSERT_NODE_FMT = "INSERT VERTEX Host (%s) VALUES %d:(%s), %d:(%s), %d:(%s)";
      final var names = String.join(",", "name", "ipAddress");
      final var value1 = String.join(",", "\"nodeX\"", "\"192.168.1.1\"");
      final var value2 = String.join(",", "\"nodeY\"", "\"192.169.1.1\"");
      final var value3 = String.join(",", "\"nodeZ\"", "\"192.170.1.1\"");
      final var addNodesQuery =
          String.format(INSERT_NODE_FMT, names, 1L, value1, 2L, value2, 3L, value3);
      final var addNodesResult = session.run(addNodesQuery);
      assertNotNull(addNodesResult.consume());
      final var seekNodesQuery = "FETCH PROP ON Host 3 YIELD vertex as node;";
      final var seekNodeResult = session.run(seekNodesQuery);
      final var seekNodeList = seekNodeResult.list();
      // assertFalse(seekNodeList.isEmpty());
      // check single record
      assertEquals(1, seekNodeList.size());
      assertEquals(1, seekNodeList.get(0).size());
      // check record is rendered as "node"
      assertTrue(seekNodeList.get(0).containsKey("node"));
      // printResult(seekNodeResult);
      // log.info("Record size: {}", seekNodeList.get(0).size());
      // log.info("Value of key: {}", seekNodeList.get(0).get("node"));
      // for(Record record : seekNodeList) {
      //    log.info("Record: {}", record.get(0));
      // }
      assertNotNull(seekNodeResult.consume());
      final String INSERT_EDGE_FMT =
          "INSERT EDGE like(likeness) VALUES %d->%d:(%.1f), %d->%d:(%.1f);";
      final var addEdgesQuery = String.format(INSERT_EDGE_FMT, 1L, 2L, 70.0, 1L, 3L, 90.0);
      final var addEdgesResult = session.run(addEdgesQuery);
      final var seekEdgeQuery = "FETCH PROP ON like 1->2 YIELD edge as e;";
      final var seekEdgeResult = session.run(seekEdgeQuery);
      final var seekEdgeList = seekEdgeResult.list();
      // assertFalse(seekEdgeList.isEmpty());
      assertEquals(1, seekEdgeList.size());
      assertTrue(seekEdgeList.get(0).containsKey("e"));
      assertNotNull(seekEdgeResult.consume());
      // update a vertex
      final String UPDATE_NODE_FMT = "UPDATE VERTEX %d SET %s.%s=\"%s\";";
      final var updateAttrQuery = String.format(UPDATE_NODE_FMT, 3L, "host", "name", "nodeW");
      final var updateAttrResult = session.run(updateAttrQuery);
      assertNotNull(updateAttrResult.consume());
      // delete a vertex, fetch and confirm null
      final String DELETE_NODE_FMT = "DELETE VERTEX %d;";
      final var deleteNodeQuery = String.format(DELETE_NODE_FMT, 3L);
      final var deleteNodeResult = session.run(deleteNodeQuery);
      assertNotNull(deleteNodeResult.consume());
      final var seekDeletedNodeResult = session.run(seekNodesQuery);
      final var seekDeletedNodeList = seekDeletedNodeResult.list();
      assertTrue(seekDeletedNodeList.isEmpty());
    }
  }
}
