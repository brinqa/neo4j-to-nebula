package com.brinqa.nebula.impl;

import java.util.Arrays;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.neo4j.driver.Query;

@RunWith(Parameterized.class)
public class QueryConverterTest {

  private final Query query;
  private final String expected;

  public QueryConverterTest(Query query, String expected) {
    this.query = query;
    this.expected = expected;
  }

  @Parameters(name = "{index}: {0}, expected: {1}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {new Query("MATCH (n:Host) RETURN n"), "MATCH (n:Host) RETURN n"},
          {
            new Query("MATCH (n:Host {id: $id}) RETURN n", Map.of("id", 123)),
            "MATCH (n:Host {id: 123}) RETURN n"
          },
        });
  }

  @Test
  public void testQueryConverter() {
    Assert.assertEquals(expected, QueryConverter.toText(query));
  }
}
