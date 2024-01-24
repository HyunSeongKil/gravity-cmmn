package dev.hyunlab.gravity.cmmn.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import dev.hyunlab.gravity.cmmn.domain.DbType;

public interface GcDbService {
  Connection createConnection(String url, String username, String plainPassword) throws SQLException;

  Set<String> getColumnNames(Statement stmt, String tableName) throws SQLException;

  boolean existsTable(Statement stmt, String tableName) throws SQLException;

  void dropTable(Statement stmt, String tableName) throws SQLException;

  void addColumn(Statement stmt, String tableName, String columnName, String comment) throws SQLException;

  void dropColumn(Statement stmt, String tableName, String columnName) throws SQLException;

  void executeUpdate(Connection conn, String sql) throws SQLException;

  String createDatabaseUrl(DbType dbType, String ip, String port, String dbName);

}
