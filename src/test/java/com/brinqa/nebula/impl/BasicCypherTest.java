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

  // @ClassRule
  // public static DockerComposeContainer environment =
  //    new DockerComposeContainer(new File("docker/docker-compose.yml"))
  //        .withExposedService("graphd_1", 9669);

  /** Setup the space for testing. */
  @BeforeClass
  public static void setup()
      throws UnknownHostException, IOErrorException, AuthFailedException,
          ClientServerIncompatibleException, NotValidConnectionException, InterruptedException {
    final var cfg = DriverConfig.defaultConfig(SPACE_NAME);
    log.info("DriverConfig: {}", cfg);
    final var CREATE_FORMAT =
        "CREATE SPACE %s(partition_num=10, replica_factor=1, vid_type=INT64);";
    final var CREATE_TAG_FORMAT = "CREATE TAG TEST_001;";
    final var createSpace = String.format(CREATE_FORMAT, cfg.getSpaceName());
    final var createTag = String.format(CREATE_TAG_FORMAT);
    final var dropSpace = String.format("DROP SPACE IF EXISTS %s", SPACE_NAME);

    final var pool = new NebulaPool();
    final var poolCfg = new NebulaPoolConfig();
    pool.init(cfg.getAddresses(), poolCfg);
    // final var session = pool.getSession(cfg.getUsername(), cfg.getPassword(), true);
    final var session = pool.getSession("root", "nebula", false);
    session.execute(dropSpace);
    Thread.sleep(10_000);
    session.execute(createSpace);
    session.execute(createTag);
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
            final var r = session.run("MATCH (n:Host) RETURN n LIMIT 1");
            // final var r = session.run("SHOW HOSTS;");
            System.out.println(r);
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
          final String CREATE_TAG = "CREATE TAG Host (`name` string, `ipAddress` string);";
          try (Session session = driver.session()) {
            final var r = session.run(CREATE_TAG);
            final var l = r.list();
            Assert.assertTrue(l.isEmpty());
            Assert.assertNotNull(r.consume());
          }
          return null;
        });

    // wait for it to make it
    Thread.sleep(10_000);

    withDriver(
        driver -> {
          // insert some records and read them back
          try (Session session = driver.session()) {
            final String INSERT_FMT = "INSERT VERTEX Host (%s) VALUES %d:(%s)";
            final var names = String.join(",", "name", "ipAddress");
            final var values = String.join(",", "\"bob\"", "\"192.168.1.1\"");
            final var query = String.format(INSERT_FMT, names, 10L, values);
            final var r = session.run(query);
            final var l = r.list();
            Assert.assertTrue(l.isEmpty());
            Assert.assertNotNull(r.consume());
          }

          return null;
        });
  }
}
