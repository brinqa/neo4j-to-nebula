/* Copyright (c) 2020 vesoft inc. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License.
 */

package com.brinqa.nebula.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.vesoft.nebula.Coordinate;
import com.vesoft.nebula.DataSet;
import com.vesoft.nebula.Date;
import com.vesoft.nebula.DateTime;
import com.vesoft.nebula.Duration;
import com.vesoft.nebula.Edge;
import com.vesoft.nebula.ErrorCode;
import com.vesoft.nebula.Geography;
import com.vesoft.nebula.LineString;
import com.vesoft.nebula.NList;
import com.vesoft.nebula.NMap;
import com.vesoft.nebula.NSet;
import com.vesoft.nebula.NullType;
import com.vesoft.nebula.Path;
import com.vesoft.nebula.Point;
import com.vesoft.nebula.Polygon;
import com.vesoft.nebula.Row;
import com.vesoft.nebula.Step;
import com.vesoft.nebula.Tag;
import com.vesoft.nebula.Time;
import com.vesoft.nebula.Value;
import com.vesoft.nebula.Vertex;
import com.vesoft.nebula.client.graph.data.DateTimeWrapper;
import com.vesoft.nebula.client.graph.data.DateWrapper;
import com.vesoft.nebula.client.graph.data.DurationWrapper;
import com.vesoft.nebula.client.graph.data.LineStringWrapper;
import com.vesoft.nebula.client.graph.data.Node;
import com.vesoft.nebula.client.graph.data.PathWrapper;
import com.vesoft.nebula.client.graph.data.PointWrapper;
import com.vesoft.nebula.client.graph.data.PolygonWrapper;
import com.vesoft.nebula.client.graph.data.Relationship;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.TimeWrapper;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import com.vesoft.nebula.graph.ExecutionResponse;
import com.vesoft.nebula.graph.PlanDescription;
import org.junit.Assert;
import org.junit.Test;

public class TestData {
  Vertex getVertex(String vid) {
    List<Tag> tags = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      Map<byte[], Value> props = new HashMap<>();
      for (int j = 0; j < 5; j++) {
        Value value = new Value();
        value.setIVal(j);
        props.put(String.format("prop%d", j).getBytes(), value);
      }
      Tag tag = new Tag(String.format("tag%d", i).getBytes(), props);
      tags.add(tag);
    }
    return new Vertex(new Value(Value.SVAL, vid.getBytes()), tags);
  }

  Edge getEdge(String srcId, String dstId) {
    Map<byte[], Value> props = new HashMap<>();
    for (int i = 0; i < 5; i++) {
      Value value = new Value();
      value.setIVal(i);
      props.put(String.format("prop%d", i).getBytes(), value);
    }
    return new Edge(
        new Value(Value.SVAL, srcId.getBytes()),
        new Value(Value.SVAL, dstId.getBytes()),
        1,
        "classmate".getBytes(),
        100,
        props);
  }

  Path getPath(String startId, int stepsNum) {
    List<Step> steps = new ArrayList<>();
    for (int i = 0; i < stepsNum; i++) {
      Map<byte[], Value> props = new HashMap<>();
      for (int j = 0; j < 5; j++) {
        Value value = new Value();
        value.setIVal(j);
        props.put(String.format("prop%d", j).getBytes(), value);
      }
      int type = 1;
      if (i % 2 != 0) {
        type = -1;
      }
      steps.add(
          new Step(
              getVertex(String.format("vertex%d", i)), type, ("classmate").getBytes(), 100, props));
    }
    return new Path(getVertex(startId), steps);
  }

  Vertex getSimpleVertex() {
    Map<byte[], Value> props1 = new HashMap<>();
    props1.put("tag1_prop".getBytes(), new Value(Value.IVAL, (long) 100));
    Map<byte[], Value> props2 = new HashMap<>();
    props2.put("tag2_prop".getBytes(), new Value(Value.IVAL, (long) 200));
    List<Tag> tags =
        Arrays.asList(new Tag("tag1".getBytes(), props1), new Tag("tag2".getBytes(), props2));
    return new Vertex(new Value(Value.SVAL, "vertex".getBytes()), tags);
  }

  Edge getSimpleEdge(boolean isReverse) {
    Map<byte[], Value> props = new HashMap<>();
    props.put("edge_prop".getBytes(), new Value(Value.IVAL, (long) 100));
    int type = 1;
    if (isReverse) {
      type = -1;
    }
    return new Edge(
        new Value(Value.SVAL, "Tom".getBytes()),
        new Value(Value.SVAL, "Lily".getBytes()),
        type,
        "classmate".getBytes(),
        10,
        props);
  }

  Path getSimplePath(boolean isReverse) {
    Map<byte[], Value> props1 = new HashMap<>();
    props1.put("tag1_prop".getBytes(), new Value(Value.IVAL, (long) 200));
    List<Tag> tags2 = Collections.singletonList(new Tag("tag1".getBytes(), props1));
    Vertex vertex1 = new Vertex(new Value(Value.SVAL, "vertex1".getBytes()), tags2);
    List<Step> steps = new ArrayList<>();
    Map<byte[], Value> props3 = new HashMap<>();
    props3.put("edge1_prop".getBytes(), new Value(Value.IVAL, (long) 100));
    steps.add(new Step(vertex1, 1, "classmate".getBytes(), 100, props3));
    Map<byte[], Value> props2 = new HashMap<>();
    props2.put("tag2_prop".getBytes(), new Value(Value.IVAL, (long) 300));
    List<Tag> tags3 = Collections.singletonList(new Tag("tag2".getBytes(), props2));
    Vertex vertex2 = new Vertex(new Value(Value.SVAL, "vertex2".getBytes()), tags3);
    Map<byte[], Value> props4 = new HashMap<>();
    props4.put("edge2_prop".getBytes(), new Value(Value.IVAL, (long) 200));
    steps.add(new Step(vertex2, isReverse ? -1 : 1, "classmate".getBytes(), 10, props4));
    Map<byte[], Value> props0 = new HashMap<>();
    props0.put("tag0_prop".getBytes(), new Value(Value.IVAL, (long) 100));
    List<Tag> tags1 = Collections.singletonList(new Tag("tag0".getBytes(), props0));
    Vertex vertex0 = new Vertex(new Value(Value.SVAL, "vertex0".getBytes()), tags1);
    return new Path(vertex0, steps);
  }

  DataSet getDateset() {
    final ArrayList<Value> list = new ArrayList<>();
    list.add(new Value(Value.IVAL, 1L));
    list.add(new Value(Value.IVAL, 2L));
    final HashSet<Value> set = new HashSet<>();
    set.add(new Value(Value.IVAL, 1L));
    set.add(new Value(Value.IVAL, 2L));
    final HashMap<byte[], Value> map = new HashMap();
    map.put("key1".getBytes(), new Value(Value.IVAL, 1L));
    map.put("key2".getBytes(), new Value(Value.IVAL, 2L));
    final Row row =
        new Row(
            Arrays.asList(
                new Value(),
                new Value(Value.NVAL, NullType.OUT_OF_RANGE),
                new Value(Value.BVAL, false),
                new Value(Value.IVAL, 1L),
                new Value(Value.FVAL, 10.01),
                new Value(Value.SVAL, "value1".getBytes()),
                new Value(Value.LVAL, new NList(list)),
                new Value(Value.UVAL, new NSet(set)),
                new Value(Value.MVAL, new NMap(map)),
                new Value(Value.TVAL, new Time((byte) 10, (byte) 30, (byte) 0, 100)),
                new Value(Value.DVAL, new Date((short) 2020, (byte) 10, (byte) 10)),
                new Value(
                    Value.DTVAL,
                    new DateTime(
                        (short) 2020, (byte) 10, (byte) 10, (byte) 10, (byte) 30, (byte) 0, 100)),
                new Value(Value.VVAL, getVertex("Tom")),
                new Value(Value.EVAL, getEdge("Tom", "Lily")),
                new Value(Value.PVAL, getPath("Tom", 3)),
                new Value(
                    Value.GGVAL,
                    new Geography(Geography.PTVAL, new Point(new Coordinate(1.0, 2.0)))),
                new Value(
                    Value.GGVAL,
                    new Geography(
                        Geography.PGVAL,
                        new Polygon(
                            Arrays.asList(
                                Arrays.asList(new Coordinate(1.0, 2.0), new Coordinate(2.0, 4.0)),
                                Arrays.asList(
                                    new Coordinate(3.0, 6.0), new Coordinate(4.0, 8.0)))))),
                new Value(
                    Value.GGVAL,
                    new Geography(
                        Geography.LSVAL,
                        new LineString(
                            Arrays.asList(new Coordinate(1.0, 2.0), new Coordinate(2.0, 4.0))))),
                new Value(Value.DUVAL, new Duration(100, 20, 1))));
    final List<byte[]> columnNames =
        Arrays.asList(
            "col0_empty".getBytes(),
            "col1_null".getBytes(),
            "col2_bool".getBytes(),
            "col3_int".getBytes(),
            "col4_double".getBytes(),
            "col5_string".getBytes(),
            "col6_list".getBytes(),
            "col7_set".getBytes(),
            "col8_map".getBytes(),
            "col9_time".getBytes(),
            "col10_date".getBytes(),
            "col11_datetime".getBytes(),
            "col12_vertex".getBytes(),
            "col13_edge".getBytes(),
            "col14_path".getBytes(),
            "col15_point".getBytes(),
            "col16_polygon".getBytes(),
            "col17_linestring".getBytes(),
            "col18_duration".getBytes());
    return new DataSet(columnNames, Collections.singletonList(row));
  }

  @Test
  public void testResult() {
    // build test data
    final ExecutionResponse resp = new ExecutionResponse();
    resp.error_code = ErrorCode.SUCCEEDED;
    resp.error_msg = "test".getBytes();
    resp.comment = "test_comment".getBytes();
    resp.latency_in_us = 1000;
    resp.plan_desc = new PlanDescription();
    resp.space_name = "test_space".getBytes();
    resp.data = getDateset();

    final var resultSet = new ResultSet(resp, 28800);

    // confirm that the data works in a neo4j based manner
    final var summary = new ResultSummaryImpl(0, null, "test_space", null);
    final var resultImpl = new ResultImpl(resultSet, summary);

    // assert the summary
    Assert.assertSame(summary, resultImpl.consume());

    // check a simple value
    final var records = resultImpl.list();
    Assert.assertEquals(1, records.size());

    // check the values within one record
    final var record = records.get(0);
    var col0 = record.get(0); // empty
    Assert.assertTrue(col0.isEmpty());
    Assert.assertFalse(col0.isNull());
    col0 = record.get("col0_empty"); // empty
    Assert.assertTrue(col0.isEmpty());
    Assert.assertFalse(col0.isNull());
    // TODO: implement equals
    //Assert.assertEquals(col0, record.get("col0_empty"));

    var col1 = record.get(1);
    Assert.assertFalse(col1.isEmpty());
    Assert.assertTrue(col1.isNull());
    col1 = record.get("col1_null");
    Assert.assertFalse(col1.isEmpty());
    Assert.assertTrue(col1.isNull());
    // TODO: implement equals
    // Assert.assertEquals(col1, record.get("col1_empty"));

    var col2 = record.get(2);
    Assert.assertFalse(col2.isTrue());
    Assert.assertTrue(col2.isFalse());
    Assert.assertFalse(col2.asBoolean());
  }
}
