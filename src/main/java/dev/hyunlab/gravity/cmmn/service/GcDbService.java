package dev.hyunlab.gravity.cmmn.service;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.hyunlab.gravity.cmmn.domain.DbType;
import dev.hyunlab.gravity.cmmn.domain.GcDatabaseProductNameEnum;

public interface GcDbService {
  Connection createConnection(String url, String username, String plainPassword) throws SQLException;

  boolean existsTable(Statement stmt, String tableName) throws SQLException;

  void dropTable(Statement stmt, String tableName) throws SQLException;

  boolean existsColumn(Statement stmt, String tableName, String columnName) throws SQLException;

  Set<String> getColumnNames(Statement stmt, String tableName) throws SQLException;

  Set<String> getColumnNames(ResultSetMetaData rsmd) throws SQLException;

  void addColumn(Statement stmt, String tableName, String columnName, String comment) throws SQLException;

  void addColumn(Statement stmt, String tableName, String columnName, String dataType, String comment)
      throws SQLException;

  void dropColumn(Statement stmt, String tableName, String columnName) throws SQLException;

  void changeColumn(Statement stmt, String tableName, String oldColumnName, String newColumnName, String newDataType,
      String newComment) throws SQLException;

  void executeUpdate(Connection conn, String sql) throws SQLException;

  String createDatabaseUrl(GcDatabaseProductNameEnum dbProductName, String ip, String port, String dbName);

  List<Map<String, Object>> getDatas(Connection conn, String sql) throws SQLException;

  List<Map<String, Object>> getDatas(Statement stmt, String sql) throws SQLException;

}
