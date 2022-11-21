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

import org.neo4j.driver.summary.SummaryCounters;

/** This information is not available from Nebula. */
public class SummaryCountersImpl implements SummaryCounters {
  public static final SummaryCounters EMPTY = new SummaryCountersImpl();

  /**
   * Whether there were any updates at all, eg. any of the counters are greater than 0.
   *
   * @return true if the query made any updates
   */
  @Override
  public boolean containsUpdates() {
    return false;
  }

  /**
   * @return number of nodes created.
   */
  @Override
  public int nodesCreated() {
    return 0;
  }

  /**
   * @return number of nodes deleted.
   */
  @Override
  public int nodesDeleted() {
    return 0;
  }

  /**
   * @return number of relationships created.
   */
  @Override
  public int relationshipsCreated() {
    return 0;
  }

  /**
   * @return number of relationships deleted.
   */
  @Override
  public int relationshipsDeleted() {
    return 0;
  }

  /**
   * @return number of properties (on both nodes and relationships) set.
   */
  @Override
  public int propertiesSet() {
    return 0;
  }

  /**
   * @return number of labels added to nodes.
   */
  @Override
  public int labelsAdded() {
    return 0;
  }

  /**
   * @return number of labels removed from nodes.
   */
  @Override
  public int labelsRemoved() {
    return 0;
  }

  /**
   * @return number of indexes added to the schema.
   */
  @Override
  public int indexesAdded() {
    return 0;
  }

  /**
   * @return number of indexes removed from the schema.
   */
  @Override
  public int indexesRemoved() {
    return 0;
  }

  /**
   * @return number of constraints added to the schema.
   */
  @Override
  public int constraintsAdded() {
    return 0;
  }

  /**
   * @return number of constraints removed from the schema.
   */
  @Override
  public int constraintsRemoved() {
    return 0;
  }

  /**
   * If the query updated the system graph in any way, this method will return true,
   *
   * @return true if the system graph has been updated.
   */
  @Override
  public boolean containsSystemUpdates() {
    return false;
  }

  /**
   * @return the number of system updates performed by this query.
   */
  @Override
  public int systemUpdates() {
    return 0;
  }
}
