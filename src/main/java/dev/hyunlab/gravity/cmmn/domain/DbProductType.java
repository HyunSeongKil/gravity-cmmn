package dev.hyunlab.gravity.cmmn.domain;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Deprecated(since = "2021-02-01", forRemoval = true)
public enum DbProductType {
  Oracle("Oracle"), MySQL("MySQL"), MariaDB("MariaDB"), MSSQL("MSSQL"), PostgreSQL("PostgreSQL");

  private String value;

  DbProductType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static DbProductType of(String productName) {
    for (DbProductType dbProductType : DbProductType.values()) {
      if (productName.toUpperCase().contains(dbProductType.getValue().toUpperCase())) {
        return dbProductType;
      }
    }
    return null;
  }

  public static DbProductType of(Connection conn) throws SQLException {
    return of(conn.getMetaData().getDatabaseProductName());

  }

  public static DbProductType of(Statement stmt) throws SQLException {
    return of(stmt.getConnection());
  }

  public static boolean isMariaDB(String productName) {
    return productName.toUpperCase().contains(MariaDB.getValue().toUpperCase());
  }
}
