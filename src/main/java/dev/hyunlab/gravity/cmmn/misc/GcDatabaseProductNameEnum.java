package dev.hyunlab.gravity.cmmn.misc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public enum GcDatabaseProductNameEnum {
  MySQL("MySQL"),
  MariaDB("MariaDB"),
  Oracle("Oracle"),
  PostgreSQL("PostgreSQL"),
  H2("H2"),
  SQLite("SQLite"),
  MSSQL("Microsoft SQL Server"),
  OTHER("Other");

  private final String name;

  GcDatabaseProductNameEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static GcDatabaseProductNameEnum fromName(String name) {
    for (GcDatabaseProductNameEnum e : GcDatabaseProductNameEnum.values()) {
      if (e.getName().equals(name)) {
        return e;
      }
    }
    return OTHER;
  }

  public static GcDatabaseProductNameEnum fromConnection(Connection connection) throws SQLException {
    return fromMetadata(connection.getMetaData());
  }

  public static GcDatabaseProductNameEnum fromMetadata(DatabaseMetaData metaData) throws SQLException {
    return fromName(metaData.getDatabaseProductName());
  }
}
