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
import com.brinqa.nebula.NebulaGraphService;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.exception.NotValidConnectionException;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import java.net.UnknownHostException;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

@Slf4j
public class BasicCypherTest {
  private static final String SPACE_NAME = "test_space";

  //@ClassRule
  //public static DockerComposeContainer environment =
   //   new DockerComposeContainer(new File("docker/docker-compose.yml"))
     //     .withExposedService("graphd_1", 9669);

  /** Setup the space for testing. */
  @BeforeClass
  public static void setup()
      throws UnknownHostException, IOErrorException, AuthFailedException,
          ClientServerIncompatibleException, NotValidConnectionException, InterruptedException {
    final var cfg = DriverConfig.defaultConfig(SPACE_NAME);
    log.info("DriverConfig: {}", cfg);
    final var CREATE_FORMAT =
        "CREATE SPACE %s(partition_num=10, replica_factor=1, vid_type=INT64);";
    // final var CREATE_TAG_FORMAT = "CREATE TAG Host;";
    final String CREATE_TAG_FORMAT =
        "USE %s; CREATE TAG IF NOT EXISTS Host(name string, ipAddress string);";
    final var createSpace = String.format(CREATE_FORMAT, cfg.getSpaceName());
    final var createTag = String.format(CREATE_TAG_FORMAT, SPACE_NAME);
    final var dropSpace = String.format("DROP SPACE IF EXISTS %s;", SPACE_NAME);

    final var pool = new NebulaPool();
    final var poolCfg = new NebulaPoolConfig();
    pool.init(cfg.getAddresses(), poolCfg);
    // final var session = pool.getSession(cfg.getUsername(), cfg.getPassword(), true);
    final var session = pool.getSession("root", "nebula", false);
    session.execute(dropSpace);
    Thread.sleep(10_000);
    session.execute(createSpace);
    session.execute(createTag);
    Thread.sleep(10_000);
    session.release();
  }

  <T> T withDriver(Function<Driver, T> fx) {
    final DriverConfig cfg = DriverConfig.defaultConfig(SPACE_NAME);
    try (Driver driver = NebulaGraphService.newDriver(cfg)) {
      System.out.println(driver);
      return fx.apply(driver);
    }
  }

  @Test
  public void testUseSpaceReturnNone() {
    withDriver(
        driver -> {
          try (Session session = driver.session()) {
            // final String CREATE_TAG = "CREATE TAG Host (`name` string, `ipAddress` string);";
            final var r = session.run("USE test_space; MATCH (n:Host) RETURN n LIMIT 1;");
            //final var r = session.run("SHOW HOSTS;");
            // System.out.println(r);
            Assert.assertTrue(r.list().isEmpty());
            return null;
          }
        });
  }

  /** Simple insert with simple read. */
  @Test
  public void testReadObjects() throws InterruptedException {
    withDriver(
        driver -> {
          // create a schema tag
          final String CREATE_TAG =
              "CREATE TAG IF NOT EXISTS Host (`name` string, `ipAddress` string);";
          try (Session session = driver.session()) {
            final var r = session.run(CREATE_TAG);
            final var l = r.list();
            Assert.assertTrue(l.isEmpty());
            Assert.assertNotNull(r.consume());
            long originalSystemTime1 = System.currentTimeMillis();
            while (true) {
              if (System.currentTimeMillis() - originalSystemTime1 >= 10000) {
                break;
              }
            }
            //Thread.sleep(10_000);
            // create a schema tag
            final String CREATE_EDGE =
                    "CREATE EDGE like (likeness double);";
            final var createEdgeRun = session.run(CREATE_EDGE);
            final var createEdgeList = createEdgeRun.list();
            Assert.assertTrue(createEdgeList.isEmpty());
            Assert.assertNotNull(createEdgeRun.consume());
            // wait for it to make the edge
            //Thread.sleep(10_000);
            long originalSystemTime2 = System.currentTimeMillis();
            while (true) {
              if (System.currentTimeMillis() - originalSystemTime2 >= 10000) {
                break;
              }
            }
            final String INSERT_FMT = "INSERT VERTEX Host (%s) VALUES %d:(%s)";
            final var sampleNames = String.join(",", "name", "ipAddress");
            final var sampleValues = String.join(",", "\"bob\"", "\"192.168.1.1\"");
            final var insertSampleQuery = String.format(INSERT_FMT, sampleNames, 10L, sampleValues);
            final var insertSampleResult = session.run(insertSampleQuery);
            final var insertSampleList = insertSampleResult.list();
            Assert.assertTrue(insertSampleList.isEmpty());
            Assert.assertNotNull(insertSampleResult.consume());
            final String INSERT_NODE_FMT = "INSERT VERTEX Host (%s) VALUES %d:(%s), %d:(%s), %d:(%s)";
            final var names = String.join(",", "name", "ipAddress");
            final var value1 = String.join(",", "\"nodeX\"", "\"192.168.1.1\"");
            final var value2 = String.join(",", "\"nodeY\"", "\"192.169.1.1\"");
            final var value3 = String.join(",", "\"nodeZ\"", "\"192.170.1.1\"");
            final var addNodesQuery = String.format(INSERT_NODE_FMT, names, 1L, value1, 2L, value2, 3L, value3);
            final var addNodesResult = session.run(addNodesQuery);
            Assert.assertNotNull(addNodesResult.consume());
            final var seekNodesQuery = "FETCH PROP ON Host 3 YIELD vertex as node;";
            final var seekNodeResult = session.run(seekNodesQuery);
            final var seekNodeList = seekNodeResult.list();
            //Assert.assertFalse(seekNodeList.isEmpty());
            // check single record
            Assert.assertTrue(seekNodeList.size()==1);
            Assert.assertTrue(seekNodeList.get(0).size()==1);
            // check record is rendered as "node"
            Assert.assertTrue(seekNodeList.get(0).containsKey("node"));
            //printResult(seekNodeResult);
            //log.info("Record size: {}", seekNodeList.get(0).size());
            //log.info("Value of key: {}", seekNodeList.get(0).get("node"));
            // for(Record record : seekNodeList) {
            //    log.info("Record: {}", record.get(0));
            //}
            Assert.assertNotNull(seekNodeResult.consume());
            final String INSERT_EDGE_FMT = "INSERT EDGE like(likeness) VALUES %d->%d:(%.1f), %d->%d:(%.1f);";
            final var addEdgesQuery = String.format(INSERT_EDGE_FMT, 1L, 2L, 70.0, 1L, 3L, 90.0);
            final var addEdgesResult = session.run(addEdgesQuery);
            final var seekEdgeQuery = "FETCH PROP ON like 1->2 YIELD edge as e;";
            final var seekEdgeResult = session.run(seekEdgeQuery);
            final var seekEdgeList = seekEdgeResult.list();
            //Assert.assertFalse(seekEdgeList.isEmpty());
            Assert.assertTrue(seekEdgeList.size()==1);
            Assert.assertTrue(seekEdgeList.get(0).containsKey("e"));
            Assert.assertNotNull(seekEdgeResult.consume());
            //update a vertex
            final String UPDATE_NODE_FMT = "UPDATE VERTEX %d SET %s.%s=\"%s\";";
            final var updateAttrQuery = String.format(UPDATE_NODE_FMT, 3L, "host", "name", "nodeW");
            final var updateAttrResult = session.run(updateAttrQuery);
            Assert.assertNotNull(updateAttrResult.consume());
            // delete a vertex, fetch and confirm null
            final String DELETE_NODE_FMT = "DELETE VERTEX %d;";
            final var deleteNodeQuery = String.format(DELETE_NODE_FMT, 3L);
            final var deleteNodeResult = session.run(deleteNodeQuery);
            Assert.assertNotNull(deleteNodeResult.consume());
            final var seekDeletedNodeResult = session.run(seekNodesQuery);
            final var seekDeletedNodeList = seekDeletedNodeResult.list();
            Assert.assertTrue(seekDeletedNodeList.size()==0);
          }
          return null;
        });
  }
}
