/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
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
package org.neo4j.driver.summary;

import org.neo4j.driver.util.Immutable;

/**
 * Contains counters for various operations that a query triggered.
 *
 * @since 1.0
 */
@Immutable
public interface SummaryCounters {
  /**
   * Whether there were any updates at all, eg. any of the counters are greater than 0.
   *
   * @return true if the query made any updates
   */
  boolean containsUpdates();

  /** @return number of nodes created. */
  int nodesCreated();

  /** @return number of nodes deleted. */
  int nodesDeleted();

  /** @return number of relationships created. */
  int relationshipsCreated();

  /** @return number of relationships deleted. */
  int relationshipsDeleted();

  /** @return number of properties (on both nodes and relationships) set. */
  int propertiesSet();

  /** @return number of labels added to nodes. */
  int labelsAdded();

  /** @return number of labels removed from nodes. */
  int labelsRemoved();

  /** @return number of indexes added to the schema. */
  int indexesAdded();

  /** @return number of indexes removed from the schema. */
  int indexesRemoved();

  /** @return number of constraints added to the schema. */
  int constraintsAdded();

  /** @return number of constraints removed from the schema. */
  int constraintsRemoved();

  /**
   * If the query updated the system graph in any way, this method will return true,
   *
   * @return true if the system graph has been updated.
   */
  boolean containsSystemUpdates();

  /** @return the number of system updates performed by this query. */
  int systemUpdates();
}
