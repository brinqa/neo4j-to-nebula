package com.brinqa.nebula.impl;

import com.vesoft.nebula.client.graph.data.HostAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.neo4j.driver.Query;
import org.neo4j.driver.summary.DatabaseInfo;
import org.neo4j.driver.summary.Notification;
import org.neo4j.driver.summary.Plan;
import org.neo4j.driver.summary.ProfiledPlan;
import org.neo4j.driver.summary.QueryType;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.ServerInfo;
import org.neo4j.driver.summary.SummaryCounters;

@AllArgsConstructor
public class ResultSummaryImpl implements ResultSummary {
  private final long time;
  private final Query query;
  private final String spaceName;
  private final HostAddress address;

  /**
   * @return query that has been executed
   */
  @Override
  public Query query() {
    return this.query;
  }

  /**
   * @return counters for operations the query triggered
   */
  @Override
  public SummaryCounters counters() {
    return SummaryCountersImpl.EMPTY;
  }

  /**
   * @return type of query that has been executed
   */
  @Override
  public QueryType queryType() {
    return QueryType.READ_WRITE;
  }

  /**
   * @return true if the result contained a query plan, i.e. is the summary of a Cypher "PROFILE" or
   *     "EXPLAIN" query
   */
  @Override
  public boolean hasPlan() {
    return false;
  }

  /**
   * @return true if the result contained profiling information, i.e. is the summary of a Cypher
   *     "PROFILE" query
   */
  @Override
  public boolean hasProfile() {
    return false;
  }

  /**
   * This describes how the database will execute your query.
   *
   * @return query plan for the executed query if available, otherwise null
   */
  @Override
  public Plan plan() {
    return null;
  }

  /**
   * This describes how the database did execute your query.
   *
   * <p>If the query you executed {@link #hasProfile() was profiled}, the query plan will contain
   * detailed information about what each step of the plan did. That more in-depth version of the
   * query plan becomes available here.
   *
   * @return profiled query plan for the executed query if available, otherwise null
   */
  @Override
  public ProfiledPlan profile() {
    return null;
  }

  /**
   * A list of notifications that might arise when executing the query. Notifications can be
   * warnings about problematic queries or other valuable information that can be presented in a
   * client.
   *
   * <p>Unlike failures or errors, notifications do not affect the execution of a query.
   *
   * @return a list of notifications produced while executing the query. The list will be empty if
   *     no notifications produced while executing the query.
   */
  @Override
  public List<Notification> notifications() {
    return List.of();
  }

  /**
   * The time it took the server to make the result available for consumption.
   *
   * @param unit The unit of the duration.
   * @return The time it took for the server to have the result available in the provided time unit.
   */
  @Override
  public long resultAvailableAfter(TimeUnit unit) {
    return TimeUnit.NANOSECONDS.convert(this.time, unit);
  }

  /**
   * The time it took the server to consume the result.
   *
   * @param unit The unit of the duration.
   * @return The time it took for the server to consume the result in the provided time unit.
   */
  @Override
  public long resultConsumedAfter(TimeUnit unit) {
    return TimeUnit.NANOSECONDS.convert(this.time, unit);
  }

  /**
   * The basic information of the server where the result is obtained from
   *
   * @return basic information of the server where the result is obtained from
   */
  @Override
  public ServerInfo server() {
    return new ServerInfoImpl(address);
  }

  /**
   * The basic information of the database where the result is obtained from
   *
   * @return the basic information of the database where the result is obtained from
   */
  @Override
  public DatabaseInfo database() {
    return () -> spaceName;
  }
}
