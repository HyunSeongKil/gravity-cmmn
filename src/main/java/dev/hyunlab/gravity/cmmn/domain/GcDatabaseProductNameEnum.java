package dev.hyunlab.gravity.cmmn.domain;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ! 중요. 추후 gravity-cmmn으로 이동 예정
 */
public enum GcDatabaseProductNameEnum {
  MySQL("MySQL"),
  MariaDB("MariaDB"),
  Oracle("Oracle"),
  PostgreSQL("PostgreSQL"),
  ;

  private final String name;

  GcDatabaseProductNameEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static GcDatabaseProductNameEnum of(Connection conn) throws SQLException {
    return of(conn.getMetaData().getDatabaseProductName());
  }

  public static GcDatabaseProductNameEnum of(Statement stmt) throws SQLException {
    return of(stmt.getConnection());
  }

  public static GcDatabaseProductNameEnum of(String name) {
    for (GcDatabaseProductNameEnum value : values()) {
      if (value.getName().equalsIgnoreCase(name)) {
        return value;
      }
    }

    return null;
  }

  public static GcDatabaseProductNameEnum of(DbType dbType) {
    return of(dbType.getName());
  }

}
