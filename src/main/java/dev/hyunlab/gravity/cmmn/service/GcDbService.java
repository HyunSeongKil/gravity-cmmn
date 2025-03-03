package dev.hyunlab.gravity.cmmn.service;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.hyunlab.gravity.cmmn.domain.GcDatabaseProductNameEnum;

public interface GcDbService {
  Connection createConnection(String url, String username, String plainPassword) throws SQLException;

  boolean canConnection(String url, String username, String plainPassword) throws SQLException;

  boolean existsTable(Statement stmt, String tableName) throws SQLException;

  /**
   * 
   * @param stmt
   * @param tableName
   * @return true: 성공, false: tableName 미 존재시
   * @throws SQLException
   */
  boolean dropTable(Statement stmt, String tableName) throws SQLException;

  /**
   * srcTableName을 destTableName으로 변경한다.
   * 
   * @param stmt
   * @param srcTableName
   * @param destTableName
   * @return true: 성공, false: srcTableName 미 존재시 or destTableName 존재시
   * @throws SQLException
   */
  boolean changeTableName(Statement stmt, String srcTableName, String destTableName) throws SQLException;

  /**
   * srcTableName을 destTableName으로 변경하고 데이터를 복사한다.
   * 
   * @param stmt
   * @param srcTableName
   * @param destTableName
   * @return true: 성공, false: srcTableName 미 존재시 or destTableName 존재시
   * @throws SQLException
   */
  boolean copyTableWithDatas(Statement stmt, String srcTableName, String destTableName) throws SQLException;

  boolean existsColumn(Statement stmt, String tableName, String columnName) throws SQLException;

  Set<String> getColumnNames(Statement stmt, String tableName) throws SQLException;

  Set<String> getColumnNames(ResultSetMetaData rsmd) throws SQLException;

  /**
   * 
   * @param stmt
   * @param tableName
   * @param columnName
   * @param comment
   * @return true: 성공, false: columnName 존재시
   * @throws SQLException
   */
  boolean addColumn(Statement stmt, String tableName, String columnName, String comment) throws SQLException;

  /**
   * 
   * @param stmt
   * @param tableName
   * @param columnName
   * @param dataType
   * @param comment
   * @return true: 성공, false: columnName 존재시
   * @throws SQLException
   */
  boolean addColumn(Statement stmt, String tableName, String columnName, String dataType, String comment)
      throws SQLException;

  /**
   * 
   * @param stmt
   * @param tableName
   * @param columnName
   * @return true: 성공, false: columnName 미 존재시
   * @throws SQLException
   */
  boolean dropColumn(Statement stmt, String tableName, String columnName) throws SQLException;

  /**
   * 
   * @param stmt
   * @param tableName
   * @param oldColumnName
   * @param newColumnName
   * @param newDataType
   * @param newComment
   * @return true: 성공, false: oldColumnName 미 존재시 or newColumnName 존재시
   * @throws SQLException
   */
  boolean changeColumn(Statement stmt, String tableName, String oldColumnName, String newColumnName, String newDataType,
      String newComment) throws SQLException;

  void executeUpdate(Connection conn, String sql) throws SQLException;

  String createDatabaseUrl(GcDatabaseProductNameEnum dbProductName, String ip, String port, String dbName);

  /**
   * 
   * @param stmt
   * @param tableName
   * @return true: 성공, false: tableName 미존재 or 데이터 미존재
   * @throws SQLException
   */
  boolean existsData(Statement stmt, String tableName) throws SQLException;

  /**
   * @see getDatas(Statement,String)
   * @param conn
   * @param sql
   * @return
   * @throws SQLException
   */
  List<Map<String, Object>> getDatas(Connection conn, String sql) throws SQLException;

  /**
   * 
   * @param stmt
   * @param sql
   * @return List<Map<String, Object>> key: columnName, value: data
   * @throws SQLException
   */
  List<Map<String, Object>> getDatas(Statement stmt, String sql) throws SQLException;

}
