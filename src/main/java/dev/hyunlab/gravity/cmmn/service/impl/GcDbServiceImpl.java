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

    stmt.executeUpdate("DROP TABLE " + tableName);

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

    switch (GcDatabaseProductNameEnum.of(stmt)) {
      case MySQL:
      case MariaDB:
      case PostgreSQL:
      case Oracle:
        stmt.executeUpdate("ALTER TABLE %s RENAME TO %s".formatted(oldTableName, newTableName));
        return true;

      default:
        throw new RuntimeException("Not supported db type " + GcDatabaseProductNameEnum.of(stmt));
    }

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

    switch (GcDatabaseProductNameEnum.of(stmt)) {
      case MySQL:
      case MariaDB:
      case Oracle:
        stmt.executeUpdate("CREATE TABLE %s AS SELECT * FROM %s".formatted(newTableName, exstingTableName));
        break;
      case PostgreSQL:
        stmt.executeUpdate("CREATE TABLE %s AS TABLE %s".formatted(newTableName, exstingTableName));
        break;
      default:
        throw new RuntimeException("Not supported db type " + GcDatabaseProductNameEnum.of(stmt));
    }

    return true;
  }

  @Override
  public boolean addColumn(Statement stmt, String tableName, String columnName, String comment) throws SQLException {
    if (existsColumn(stmt, tableName, columnName)) {
      log.warn("Column {}.{} is already exists", tableName, columnName);
      return false;
    }

    stmt.executeUpdate(
        "ALTER TABLE %s ADD COLUMN %s VARCHAR(255) NULL COMMENT '%s'".formatted(tableName, columnName, comment));
    return true;
  }

  @Override
  public boolean addColumn(Statement stmt, String tableName, String columnName, String dataType, String comment)
      throws SQLException {
    if (existsColumn(stmt, tableName, columnName)) {
      log.warn("Column {}.{} is already exists", tableName, columnName);
      return false;
    }

    switch (GcDatabaseProductNameEnum.of(stmt)) {
      case MySQL:
      case MariaDB:
        stmt.executeUpdate(
            "ALTER TABLE %s ADD COLUMN %s %s NULL COMMENT '%s'".formatted(tableName, columnName, dataType, comment));
        break;
      case PostgreSQL:
        stmt.executeUpdate("ALTER TABLE %s ADD COLUMN %s %s NULL".formatted(tableName, columnName, dataType));
        stmt.executeUpdate("COMMENT ON COLUMN %s.%s IS '%s'".formatted(tableName, columnName, comment));
        break;
      case Oracle:
        stmt.executeUpdate("ALTER TABLE %s ADD %s %s NULL".formatted(tableName, columnName, dataType));
        stmt.executeUpdate("COMMENT ON COLUMN %s.%s IS '%s'".formatted(tableName, columnName, comment));
        break;
      default:
        throw new RuntimeException("Not supported db type " + GcDatabaseProductNameEnum.of(stmt));
    }

    return true;
  }

  @Override
  public boolean dropColumn(Statement stmt, String tableName, String columnName) throws SQLException {
    if (!existsColumn(stmt, tableName, columnName)) {
      log.warn("Column {}.{} is not exists", tableName, columnName);
      return false;
    }

    stmt.executeUpdate("ALTER TABLE %s DROP COLUMN %s ".formatted(tableName, columnName));
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

    switch (GcDatabaseProductNameEnum.of(stmt)) {
      case MySQL:
      case MariaDB:
        stmt.executeUpdate(
            "ALTER TABLE %s CHANGE COLUMN %s %s %s NULL COMMENT '%s'"
                .formatted(tableName, oldColumnName, newColumnName, newDataType, newComment));
        break;
      case PostgreSQL:
        stmt.executeUpdate(
            "ALTER TABLE %s RENAME COLUMN %s TO %s".formatted(tableName, oldColumnName, newColumnName));
        stmt.executeUpdate(
            "ALTER TABLE %s ALTER COLUMN %s TYPE %s".formatted(tableName, newColumnName, newDataType));
        stmt.executeUpdate(
            "COMMENT ON COLUMN %s.%s IS '%s'".formatted(tableName, newColumnName, newComment));
        break;
      case Oracle:
        stmt.executeUpdate(
            "ALTER TABLE %s RENAME COLUMN %s TO %s".formatted(tableName, oldColumnName, newColumnName));
        stmt.executeUpdate(
            "ALTER TABLE %s MODIFY %s %s".formatted(tableName, newColumnName, newDataType));
        stmt.executeUpdate(
            "COMMENT ON COLUMN %s.%s IS '%s'".formatted(tableName, newColumnName, newComment));
        break;
      default:
        throw new RuntimeException("Not supported db type " + GcDatabaseProductNameEnum.of(stmt));
    }

    return true;
  }

  @Override
  public void executeUpdate(Connection conn, String sql) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(sql);
    }
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
    switch (GcDatabaseProductNameEnum.of(stmt)) {
      case MySQL:
      case MariaDB:
        try (ResultSet rs = stmt.executeQuery(
            "SELECT * FROM information_schema.columns WHERE table_name = '%s' AND column_name = '%s'"
                .formatted(tableName, columnName))) {
          return rs.next();
        }
      case PostgreSQL:
        try (ResultSet rs = stmt.executeQuery(
            "SELECT * FROM information_schema.columns WHERE table_name = '%s' AND column_name = '%s'"
                .formatted(tableName, columnName))) {
          return rs.next();
        }
      case Oracle:
        try (ResultSet rs = stmt.executeQuery(
            "SELECT * FROM user_tab_columns WHERE table_name = '%s' AND column_name = '%s'"
                .formatted(tableName, columnName))) {
          return rs.next();
        }
      default:
        throw new RuntimeException("Not supported db type " + GcDatabaseProductNameEnum.of(stmt));

    }
  }

  @Override
  public boolean dropTables(Statement stmt, List<String> tableNames) throws SQLException {
    stmt.clearBatch();

    for (String tableName : tableNames) {
      if (!existsTable(stmt, tableName)) {
        log.warn("Table {} is not exists", tableName);
        continue;
      }

      stmt.addBatch("DROP TABLE " + tableName);
    }

    stmt.executeBatch();

    return true;
  }

  @Override
  public boolean changeTablesNames(Statement stmt, List<String> oldTableNames, List<String> newTableNames)
      throws SQLException {
    stmt.clearBatch();

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

      stmt.addBatch("ALTER TABLE %s RENAME TO %s".formatted(oldTableName, newTableName));
    }

    stmt.executeBatch();

    return true;
  }

  @Override
  public boolean addColumns(Statement stmt, List<String> tableNames, List<GcColumnMetaDto> columnMetaDtos)
      throws SQLException {
    stmt.clearBatch();

    for (int i = 0; i < tableNames.size(); i++) {
      String tableName = tableNames.get(i);
      GcColumnMetaDto columnMetaDto = columnMetaDtos.get(i);

      if (!existsTable(stmt, tableName)) {
        log.warn("Table {} is not exists", tableName);
        continue;
      }

      if (existsColumn(stmt, tableName, columnMetaDto.getColumnName())) {
        log.warn("Column {}.{} is already exists", tableName, columnMetaDto.getColumnName());
        continue;
      }

      switch (GcDatabaseProductNameEnum.of(stmt)) {
        case MySQL:
        case MariaDB:
          stmt.addBatch(
              "ALTER TABLE %s ADD COLUMN %s %s COMMENT '%s'"
                  .formatted(tableName,
                      columnMetaDto.getColumnName(),
                      columnMetaDto.getDataType(),
                      columnMetaDto.getColumnComment()));
          break;
        case PostgreSQL:
          stmt.addBatch(
              "ALTER TABLE %s ADD COLUMN %s %s NULL"
                  .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getDataType()));
          stmt.addBatch(
              "COMMENT ON COLUMN %s.%s IS '%s'"
                  .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getColumnComment()));
          break;
        case Oracle:
          stmt.addBatch(
              "ALTER TABLE %s ADD %s %s NULL"
                  .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getDataType()));
          stmt.addBatch(
              "COMMENT ON COLUMN %s.%s IS '%s'"
                  .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getColumnComment()));
          break;
        default:
          throw new RuntimeException("Not supported db type " + GcDatabaseProductNameEnum.of(stmt));
      }
    }

    stmt.executeBatch();

    return true;
  }

  @Override
  public boolean dropColumns(Statement stmt, List<String> tableNames, List<String> columnNames) throws SQLException {
    stmt.clearBatch();

    for (int i = 0; i < tableNames.size(); i++) {
      String tableName = tableNames.get(i);
      String columnName = columnNames.get(i);

      if (!existsTable(stmt, tableName)) {
        log.warn("Table {} is not exists", tableName);
        continue;
      }

      if (!existsColumn(stmt, tableName, columnName)) {
        log.warn("Column {}.{} is not exists", tableName, columnName);
        continue;
      }

      stmt.addBatch("ALTER TABLE %s DROP COLUMN %s".formatted(tableName, columnName));
    }

    stmt.executeBatch();

    return true;
  }

  @Override
  public boolean changeColumns(Statement stmt, List<String> tableNames, List<String> oldColumnNames,
      List<GcColumnMetaDto> columnMetaDtos) throws SQLException {
    stmt.clearBatch();

    for (int i = 0; i < tableNames.size(); i++) {
      String tableName = tableNames.get(i);
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

      switch (GcDatabaseProductNameEnum.of(stmt)) {
        case MySQL:
        case MariaDB:
          stmt.addBatch(
              "ALTER TABLE %s CHANGE COLUMN %s %s %s NULL COMMENT '%s'"
                  .formatted(tableName,
                      oldColumnName,
                      columnMetaDto.getColumnName(),
                      columnMetaDto.getDataType(),
                      columnMetaDto.getColumnComment()));
          break;
        case PostgreSQL:
          stmt.addBatch(
              "ALTER TABLE %s RENAME COLUMN %s TO %s"
                  .formatted(tableName, oldColumnName, columnMetaDto.getColumnName()));
          stmt.addBatch(
              "ALTER TABLE %s ALTER COLUMN %s TYPE %s"
                  .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getDataType()));
          stmt.addBatch(
              "COMMENT ON COLUMN %s.%s IS '%s'"
                  .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getColumnComment()));
          break;
        case Oracle:
          stmt.addBatch(
              "ALTER TABLE %s RENAME COLUMN %s TO %s"
                  .formatted(tableName, oldColumnName, columnMetaDto.getColumnName()));
          stmt.addBatch(
              "ALTER TABLE %s MODIFY %s %s"
                  .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getDataType()));
          stmt.addBatch(
              "COMMENT ON COLUMN %s.%s IS '%s'"
                  .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getColumnComment()));
          break;
        default:
          throw new RuntimeException("Not supported db type " + GcDatabaseProductNameEnum.of(stmt));
      }
    }

    stmt.executeBatch();

    return true;
  }

  public boolean createTable(Statement stmt, String tableName, List<GcColumnMetaDto> columnMetaDtos)
      throws SQLException {
    String sql = "";

    if (existsTable(stmt, tableName)) {
      log.warn("Table {} is already exists", tableName);
      return false;
    }

    switch (GcDatabaseProductNameEnum.of(stmt)) {
      case MariaDB:
      case MySQL:
        List<String> columns = columnMetaDtos
            .stream()
            .map(dto -> {
              return " `%s` %s %s COMMENT '%s'"
                  .formatted(dto.getColumnName(), dto.getDataType(), dto.isPrimaryKey() ? "primary key" : "",
                      dto.getColumnComment());
            })
            .toList();

        sql = " CREATE TABLE %s ( %s ) COMMENT ''".formatted(tableName, String.join(",", columns));
        break;

      default:
        throw new RuntimeException("Not supported db type " + GcDatabaseProductNameEnum.of(stmt));
    }

    stmt.executeUpdate(sql);

    return true;
  }

}
