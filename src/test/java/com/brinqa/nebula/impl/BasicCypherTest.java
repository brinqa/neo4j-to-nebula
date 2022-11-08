package com.brinqa.nebula.impl;

import com.brinqa.nebula.DriverConfig;
import com.brinqa.nebula.NebulaGraphService;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.exception.NotValidConnectionException;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import java.io.File;
import java.net.UnknownHostException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.testcontainers.containers.DockerComposeContainer;

public class BasicCypherTest {
  private static final String SPACE_NAME = "test_space";

  @ClassRule
  public static DockerComposeContainer environment =
      new DockerComposeContainer(new File("docker/docker-compose.yml"))
          .withExposedService("graphd_1", 9669);

  /** Setup the space for testing. */
  @BeforeClass
  public static void setup()
      throws UnknownHostException, IOErrorException, AuthFailedException,
          ClientServerIncompatibleException, NotValidConnectionException {
    final var cfg = DriverConfig.defaultConfig(SPACE_NAME);
    final var CREATE_FORMAT =
        "CREATE SPACE IF NOT EXISTS %s(partition_num=10, replica_factor=1, vid_type=INT64);";
    final var createSpace = String.format(CREATE_FORMAT, cfg.getSpaceName());

    final var pool = new NebulaPool();
    final var poolCfg = new NebulaPoolConfig();
    pool.init(cfg.getAddresses(), poolCfg);
    final var session = pool.getSession(cfg.getUsername(), cfg.getPassword(), true);
    session.execute(createSpace);
    session.release();
  }

  @Test
  public void testUseSpaceReturnNone() {
    DriverConfig cfg = DriverConfig.defaultConfig(SPACE_NAME);
    try (Driver driver = NebulaGraphService.newDriver(cfg)) {
      try (Session session = driver.session()) {
        Result r = session.run("MATCH (n:Host) return n");
        Assert.assertTrue(r.list().isEmpty());
      }
    }
  }
}
