package dev.hyunlab.gravity.cmmn.domain;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ! 중요. 추후 gravity-cmmn으로 이동 예정
 */
public enum GcDatabaseProductNmEnum {
  MySQL("MySQL"),
  MariaDB("MariaDB"),
  Oracle("Oracle"),
  PostgreSQL("PostgreSQL"),
  ;

  private final String name;

  GcDatabaseProductNmEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static GcDatabaseProductNmEnum of(Connection conn) throws SQLException {
    return of(conn.getMetaData().getDatabaseProductName());
  }

  public static GcDatabaseProductNmEnum of(Statement stmt) throws SQLException {
    return of(stmt.getConnection());
  }

  public static GcDatabaseProductNmEnum of(String name) {
    for (GcDatabaseProductNmEnum value : values()) {
      if (value.getName().equalsIgnoreCase(name)) {
        return value;
      }
    }

    return null;
  }

  public static GcDatabaseProductNmEnum of(DbType dbType) {
    return of(dbType.getName());
  }

}
