package dev.hyunlab.gravity.cmmn.service;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.hyunlab.gravity.cmmn.domain.DbType;

public interface GcDbService {
  Connection createConnection(String url, String username, String plainPassword) throws SQLException;

  Set<String> getColumnNames(Statement stmt, String tableName) throws SQLException;

  Set<String> getColumnNames(ResultSetMetaData rsmd) throws SQLException;

  boolean existsTable(Statement stmt, String tableName) throws SQLException;

  boolean existsColumn(Statement stmt, String tableName, String columnName) throws SQLException;

  void dropTable(Statement stmt, String tableName) throws SQLException;

  /**
   * Create table. default dataType: varchar(255)
   * 
   * @param stmt
   * @param tableName
   * @param columns
   * @return 성공시 true
   * @throws SQLException
   */
  boolean addColumn(Statement stmt, String tableName, String columnName, String comment) throws SQLException;

  /**
   * Create table
   * 
   * @param stmt
   * @param tableName
   * @param columnName
   * @param dataType   varchar(255), int, date, ...
   * @param comment
   * @return 성공시 true
   * @throws SQLException
   */
  boolean addColumn(Statement stmt, String tableName, String columnName, String dataType, String comment)
      throws SQLException;

  /**
   * Add columns. batch 처리로 성능 향상
   * 
   * @param stmt
   * @param tableName
   * @param columnNames
   * @param dataTypes
   * @param comments
   * @return
   * @throws SQLException
   */
  int[] addColumns(Statement stmt, String tableName, List<String> columnNames, List<String> dataTypes,
      List<String> comments)
      throws SQLException;

  /**
   * Drop column
   * 
   * @param stmt
   * @param tableName
   * @param columnName
   * @return 성공시 true
   * @throws SQLException
   */
  boolean dropColumn(Statement stmt, String tableName, String columnName) throws SQLException;

  void executeUpdate(Connection conn, String sql) throws SQLException;

  String createDatabaseUrl(DbType dbType, String ip, String port, String dbName);

  List<Map<String, Object>> getDatas(Connection conn, String sql) throws SQLException;

  List<Map<String, Object>> getDatas(Statement stmt, String sql) throws SQLException;

}
