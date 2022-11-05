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
 * Representation for notifications found when executing a query.
 *
 * <p>A notification can be visualized in a client pinpointing problems or other information about
 * the query.
 *
 * @since 1.0
 */
@Immutable
public interface Notification {
  /**
   * Returns a notification code for the discovered issue.
   *
   * @return the notification code
   */
  String code();

  /**
   * Returns a short summary of the notification.
   *
   * @return the title of the notification.
   */
  String title();

  /**
   * Returns a longer description of the notification.
   *
   * @return the description of the notification.
   */
  String description();

  /**
   * The position in the query where this notification points to. Not all notifications have a
   * unique position to point to and in that case the position would be set to null.
   *
   * @return the position in the query where the issue was found, or null if no position is
   *     associated with this notification.
   */
  InputPosition position();

  /**
   * The severity level of the notification.
   *
   * @return the severity level of the notification
   */
  String severity();
}
