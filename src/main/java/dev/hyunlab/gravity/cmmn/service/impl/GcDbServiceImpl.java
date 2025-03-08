package dev.hyunlab.gravity.cmmn.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import dev.hyunlab.gravity.cmmn.domain.GcColumnMetaDto;
import dev.hyunlab.gravity.cmmn.domain.GcDatabaseProductNameEnum;
import dev.hyunlab.gravity.cmmn.misc.SqlHelper;
import dev.hyunlab.gravity.cmmn.service.GcDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GcDbServiceImpl implements GcDbService {

  @Override
  public Connection createConnection(String url, String username, String plainPassword) throws SQLException {
    return DriverManager.getConnection(url, username, plainPassword);
  }

  @Override
  public boolean canConnection(String url, String username, String plainPassword) throws SQLException {
    try (Connection conn = createConnection(url, username, plainPassword)) {
      return conn.isValid(1);
    }
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
  public Set<String> getColumnNames(ResultSetMetaData rsmd) throws SQLException {
    Set<String> list = new HashSet<>();
    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
      list.add(rsmd.getColumnName(i));
    }
    return list;

  }

  @Override
  public String createDatabaseUrl(GcDatabaseProductNameEnum databaseProductNameEnum, String ip, String port,
      String dbName) {
    switch (databaseProductNameEnum) {
      case MySQL:
        return "jdbc:mysql://%s:%s/%s".formatted(ip, port, dbName);
      case MariaDB:
        return "jdbc:mariadb://%s:%s/%s".formatted(ip, port, dbName);
      case PostgreSQL:
        return "jdbc:postgresql://%s:%s/%s".formatted(ip, port, dbName);
      case Oracle:
        return "jdbc:oracle:thin:@%s:%s:%s".formatted(ip, port, dbName);

      default:
        throw new RuntimeException("Not supported db type " + databaseProductNameEnum);
    }
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
  public boolean dropTable(Statement stmt, String tableName) throws SQLException {
    if (!existsTable(stmt, tableName)) {
      log.warn("Table {} is not exists", tableName);
      return false;
    }

    List<String> sqls = SqlHelper.createDropTableSqls(GcDatabaseProductNameEnum.of(stmt), tableName);

    executeBatch(stmt, sqls);

    return true;
  }

  @Override
  public boolean changeTableName(Statement stmt, String oldTableName, String newTableName) throws SQLException {
    if (!existsTable(stmt, oldTableName)) {
      log.warn("Table {} is not exists", oldTableName);
      return false;
    }

    if (existsTable(stmt, newTableName)) {
      log.warn("Table {} is already exists", newTableName);
      return false;
    }

    List<String> sqls = SqlHelper.createChangeTableNameSqls(GcDatabaseProductNameEnum.of(stmt), oldTableName,
        newTableName);

    executeBatch(stmt, sqls);

    return true;
  }

  @Override
  public boolean copyTableWithDatas(Statement stmt, String exstingTableName, String newTableName) throws SQLException {
    if (!existsTable(stmt, exstingTableName)) {
      log.warn("Table {} is not exists", exstingTableName);
      return false;
    }

    if (existsTable(stmt, newTableName)) {
      log.warn("Table {} is already exists", newTableName);
      return false;
    }

    List<String> sqls = SqlHelper.createCopyTableWithDatasSqls(GcDatabaseProductNameEnum.of(stmt), exstingTableName,
        newTableName);

    executeBatch(stmt, sqls);

    return true;
  }

  @Override
  public boolean addColumn(Statement stmt, String tableName, String columnName, String comment) throws SQLException {
    if (existsColumn(stmt, tableName, columnName)) {
      log.warn("Column {}.{} is already exists", tableName, columnName);
      return false;
    }

    List<String> sqls = SqlHelper.createAddColumnSqls(GcDatabaseProductNameEnum.of(stmt),
        tableName,
        GcColumnMetaDto
            .builder()
            .columnName(columnName)
            .columnComment(comment)
            .build());

    executeBatch(stmt, sqls);

    return true;
  }

  @Override
  public boolean addColumn(Statement stmt, String tableName, String columnName, String dataType, String comment)
      throws SQLException {
    if (existsColumn(stmt, tableName, columnName)) {
      log.warn("Column {}.{} is already exists", tableName, columnName);
      return false;
    }

    List<String> sqls = SqlHelper.createAddColumnSqls(GcDatabaseProductNameEnum.of(stmt),
        tableName,
        GcColumnMetaDto
            .builder()
            .columnName(columnName)
            .dataType(dataType)
            .columnComment(comment)
            .build());

    executeBatch(stmt, sqls);

    return true;
  }

  @Override
  public boolean dropColumn(Statement stmt, String tableName, String columnName) throws SQLException {
    if (!existsColumn(stmt, tableName, columnName)) {
      log.warn("Column {}.{} is not exists", tableName, columnName);
      return false;
    }

    List<String> sqls = SqlHelper.createDropColumnSqls(GcDatabaseProductNameEnum.of(stmt), tableName, columnName);
    executeBatch(stmt, sqls);

    return true;
  }

  @Override
  public boolean changeColumn(Statement stmt, String tableName, String oldColumnName, String newColumnName,
      String newDataType, String newComment) throws SQLException {
    if (!existsColumn(stmt, tableName, oldColumnName)) {
      log.warn("Column {}.{} is not exists", tableName, oldColumnName);
      return false;
    }

    if (existsColumn(stmt, tableName, newColumnName)) {
      log.warn("Column {}.{} is already exists", tableName, newColumnName);
      return false;
    }

    List<String> sqls = SqlHelper.createChangeColumnSqls(GcDatabaseProductNameEnum.of(stmt),
        tableName,
        oldColumnName,
        GcColumnMetaDto
            .builder()
            .columnName(newColumnName)
            .dataType(newDataType)
            .columnComment(newComment)
            .build());
    executeBatch(stmt, sqls);

    return true;
  }

  @Override
  public void executeUpdate(Connection conn, String sql) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(sql);
    }
  }

  @Override
  public void executeBatch(Statement stmt, List<String> sqls) throws SQLException {
    stmt.clearBatch();
    for (String sql : sqls) {
      stmt.addBatch(sql);
    }
    stmt.executeBatch();
  }

  @Override
  public boolean existsData(Statement stmt, String tableName) throws SQLException {
    if (!existsTable(stmt, tableName)) {
      log.warn("Table {} is not exists", tableName);
      return false;
    }

    try (ResultSet rs = stmt.executeQuery("SELECT * FROM %s".formatted(tableName))) {
      return rs.next();
    }
  }

  @Override
  public List<Map<String, Object>> getDatas(Connection conn, String sql) throws SQLException {
    return getDatas(conn.createStatement(), sql);
  }

  @Override
  public List<Map<String, Object>> getDatas(Statement stmt, String sql) throws SQLException {
    List<Map<String, Object>> datas = new ArrayList<>();

    try (ResultSet rs = stmt.executeQuery(sql)) {
      Set<String> columnNames = getColumnNames(rs.getMetaData());

      while (rs.next()) {
        datas.add(createDataMap(rs, columnNames));
      }
    } catch (Exception e) {
      log.error("{}", e);
      throw e;
    }

    return datas;
  }

  private Map<String, Object> createDataMap(ResultSet rs, Set<String> columnNames) throws SQLException {
    Map<String, Object> map = new LinkedHashMap<>();

    for (String columnName : columnNames) {
      map.put(columnName, rs.getObject(columnName));
    }

    return map;
  }

  @Override
  public boolean existsColumn(Statement stmt, String tableName, String columnName) throws SQLException {
    try (ResultSet rs = stmt.executeQuery(
        SqlHelper.createExistsColumnSql(GcDatabaseProductNameEnum.of(stmt), tableName, columnName))) {
      return rs.next();
    }
  }

  @Override
  public boolean dropTables(Statement stmt, List<String> tableNames) throws SQLException {

    List<String> sqls = new ArrayList<>();

    for (String tableName : tableNames) {
      if (!existsTable(stmt, tableName)) {
        log.warn("Table {} is not exists", tableName);
        continue;
      }

      sqls.addAll(SqlHelper.createDropTableSqls(GcDatabaseProductNameEnum.of(stmt), tableName));
    }

    executeBatch(stmt, sqls);

    return true;
  }

  @Override
  public boolean changeTablesNames(Statement stmt, List<String> oldTableNames, List<String> newTableNames)
      throws SQLException {

    List<String> sqls = new ArrayList<>();

    for (int i = 0; i < oldTableNames.size(); i++) {
      String oldTableName = oldTableNames.get(i);
      String newTableName = newTableNames.get(i);

      if (!existsTable(stmt, oldTableName)) {
        log.warn("Table {} is not exists", oldTableName);
        continue;
      }

      if (existsTable(stmt, newTableName)) {
        log.warn("Table {} is already exists", newTableName);
        continue;
      }

      sqls.addAll(SqlHelper.createChangeTableNameSqls(GcDatabaseProductNameEnum.of(stmt), oldTableName, newTableName));
    }

    executeBatch(stmt, sqls);

    return true;
  }

  @Override
  public boolean addColumns(Statement stmt, String tableName, List<GcColumnMetaDto> columnMetaDtos)
      throws SQLException {
    List<String> sqls = new ArrayList<>();

    for (int i = 0; i < columnMetaDtos.size(); i++) {
      GcColumnMetaDto columnMetaDto = columnMetaDtos.get(i);

      if (!existsTable(stmt, tableName)) {
        log.warn("Table {} is not exists", tableName);
        continue;
      }

      if (existsColumn(stmt, tableName, columnMetaDto.getColumnName())) {
        log.warn("Column {}.{} is already exists", tableName, columnMetaDto.getColumnName());
        continue;
      }

      sqls.addAll(SqlHelper.createAddColumnSqls(GcDatabaseProductNameEnum.of(stmt), tableName, columnMetaDto));
    }

    executeBatch(stmt, sqls);

    return true;
  }

  @Override
  public boolean dropColumns(Statement stmt, String tableName, List<String> columnNames) throws SQLException {
    List<String> sqls = new ArrayList<>();

    for (int i = 0; i < columnNames.size(); i++) {
      String columnName = columnNames.get(i);

      if (!existsTable(stmt, tableName)) {
        log.warn("Table {} is not exists", tableName);
        continue;
      }

      if (!existsColumn(stmt, tableName, columnName)) {
        log.warn("Column {}.{} is not exists", tableName, columnName);
        continue;
      }

      sqls.addAll(SqlHelper.createDropColumnSqls(GcDatabaseProductNameEnum.of(stmt), tableName, columnName));
    }

    executeBatch(stmt, sqls);

    return true;
  }

  @Override
  public boolean changeColumns(Statement stmt, String tableName, List<String> oldColumnNames,
      List<GcColumnMetaDto> columnMetaDtos) throws SQLException {
    List<String> sqls = new ArrayList<>();

    for (int i = 0; i < oldColumnNames.size(); i++) {
      String oldColumnName = oldColumnNames.get(i);
      GcColumnMetaDto columnMetaDto = columnMetaDtos.get(i);

      if (!existsTable(stmt, tableName)) {
        log.warn("Table {} is not exists", tableName);
        continue;
      }

      if (!existsColumn(stmt, tableName, oldColumnName)) {
        log.warn("Column {}.{} is not exists", tableName, oldColumnName);
        continue;
      }

      if (existsColumn(stmt, tableName, columnMetaDto.getColumnName())) {
        log.warn("Column {}.{} is already exists", tableName, columnMetaDto.getColumnName());
        continue;
      }

      sqls.addAll(SqlHelper.createChangeColumnSqls(GcDatabaseProductNameEnum.of(stmt), tableName, oldColumnName,
          columnMetaDto));
    }

    executeBatch(stmt, sqls);

    return true;
  }

  public boolean createTable(Statement stmt, String tableName, List<GcColumnMetaDto> columnMetaDtos)
      throws SQLException {

    if (existsTable(stmt, tableName)) {
      log.warn("Table {} is already exists", tableName);
      return false;
    }

    List<String> sqls = SqlHelper.createCreateTableSqls(GcDatabaseProductNameEnum.of(stmt), tableName, columnMetaDtos);
    executeBatch(stmt, sqls);

    return true;
  }

}
