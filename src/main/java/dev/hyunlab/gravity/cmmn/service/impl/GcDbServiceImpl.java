package dev.hyunlab.gravity.cmmn.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import dev.hyunlab.gravity.cmmn.domain.DbType;
import dev.hyunlab.gravity.cmmn.service.GcDbService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GcDbServiceImpl implements GcDbService {

  @Override
  public Connection createConnection(String url, String username, String plainPassword) throws SQLException {
    return DriverManager.getConnection(url, username, plainPassword);
  }

  @Override
  public Set<String> getColumnNames(Statement stmt, String tableName) throws SQLException {
    try (ResultSet rs = stmt.executeQuery("SELECT * FROM %s".formatted(tableName))) {
      Set<String> list = new HashSet<>();
      for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
        list.add(rs.getMetaData().getColumnName(i));
      }
      return list;
    }
  }

  @Override
  public String createDatabaseUrl(DbType dbType, String ip, String port, String dbName) {
    if (DbType.MariaDB.getCode().equals(dbType.getCode())) {
      return "jdbc:mariadb://%s:%s/%s".formatted(ip, port, dbName);
    }

    throw new RuntimeException("Not supported db type " + dbType.getCode());
  };

  @Override
  public boolean existsTable(Statement stmt, String tableName) throws SQLException {
    try {
      stmt.executeQuery("SELECT * FROM %s".formatted(tableName));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void dropTable(Statement stmt, String tableName) throws SQLException {
    stmt.executeUpdate("DROP TABLE " + tableName);
  }

  @Override
  public void addColumn(Statement stmt, String tableName, String columnName, String comment) throws SQLException {
    stmt.executeUpdate(
        "ALTER TABLE %s ADD COLUMN %s VARCHAR(255) NULL COMMENT '%s'".formatted(tableName, columnName, comment));
  }

  @Override
  public void dropColumn(Statement stmt, String tableName, String columnName) throws SQLException {
    stmt.executeUpdate(
        "ALTER TABLE %s DROP COLUMN %s ".formatted(tableName, columnName));
  }

  @Override
  public void executeUpdate(Connection conn, String sql) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(sql);
    }
  }

}
