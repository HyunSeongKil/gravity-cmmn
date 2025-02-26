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

import dev.hyunlab.gravity.cmmn.domain.DbType;
import dev.hyunlab.gravity.cmmn.misc.GcDatabaseProductNameEnum;
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
  public Set<String> getColumnNames(Statement stmt, String tableName) throws SQLException {
    GcDatabaseProductNameEnum dbProductName = GcDatabaseProductNameEnum.fromConnection(stmt.getConnection());
    switch (dbProductName) {
      case MySQL:
      case MariaDB:
      case PostgreSQL:
        try (ResultSet rs = stmt
            .executeQuery(
                "SELECT column_name FROM information_schema.columns WHERE table_name = '%s'".formatted(tableName))) {
          Set<String> list = new HashSet<>();
          while (rs.next()) {
            list.add(rs.getString(1));
          }
          return list;
        }

      case Oracle:
        try (ResultSet rs = stmt
            .executeQuery("SELECT column_name FROM all_tab_columns WHERE table_name = '%s'".formatted(tableName))) {
          Set<String> list = new HashSet<>();
          while (rs.next()) {
            list.add(rs.getString(1));
          }
          return list;
        }

      default:
        throw new RuntimeException("Not supported db product name " + dbProductName);
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
  public String createDatabaseUrl(DbType dbType, String ip, String port, String dbName) {
    if (DbType.MariaDB.getCode().equals(dbType.getCode())) {
      return "jdbc:mariadb://%s:%s/%s".formatted(ip, port, dbName);
    }

    throw new RuntimeException("Not supported db type " + dbType.getCode());
  };

  @Override
  public String createDatabaseUrl(GcDatabaseProductNameEnum dbProductName, String ip, String port, String dbName) {
    switch (dbProductName) {
      case MySQL:
        return "jdbc:mysql://%s:%s/%s".formatted(ip, port, dbName);

      case MariaDB:
        return "jdbc:mariadb://%s:%s/%s".formatted(ip, port, dbName);

      case PostgreSQL:
        return "jdbc:postgresql://%s:%s/%s".formatted(ip, port, dbName);

      case Oracle:
        return "jdbc:oracle:thin:@%s:%s:%s".formatted(ip, port, dbName);

      default:
        throw new RuntimeException("Not supported db product name " + dbProductName);
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
  public boolean existsColumn(Statement stmt, String tableName, String columnName) throws SQLException {
    GcDatabaseProductNameEnum dbProductName = GcDatabaseProductNameEnum.fromConnection(stmt.getConnection());
    switch (dbProductName) {
      case MySQL:
      case MariaDB:
      case PostgreSQL:
        try (ResultSet rs = stmt.executeQuery(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = '%s' AND column_name = '%s'"
                .formatted(tableName, columnName))) {
          rs.next();
          return rs.getInt(1) > 0;
        }

      case Oracle:
        try (ResultSet rs = stmt
            .executeQuery("SELECT COUNT(*) FROM all_tab_columns WHERE table_name = '%s' AND column_name = '%s'"
                .formatted(tableName, columnName))) {
          rs.next();
          return rs.getInt(1) > 0;
        }

      default:
        throw new RuntimeException("Not supported db product name " + dbProductName);
    }
  }

  @Override
  public void dropTable(Statement stmt, String tableName) throws SQLException {
    stmt.executeUpdate("DROP TABLE " + tableName);
  }

  @Override
  public int[] addColumns(Statement stmt, String tableName, List<String> columnNames, List<String> dataTypes,
      List<String> comments) throws SQLException {
    if (columnNames.size() != dataTypes.size() || columnNames.size() != comments.size()) {
      throw new RuntimeException("columnNames.size() != dataTypes.size() || columnNames.size() != comments.size()");
    }

    Set<String> columnNameSet = getColumnNames(stmt, tableName);

    for (int i = 0; i < columnNames.size(); i++) {
      if (columnNameSet.contains(columnNames.get(i))) {
        log.debug("Column already exists. tableName: {}, columnName: {}", tableName, columnNames.get(i));
        continue;
      }

      createAddColumnSqls(stmt, tableName, columnNames.get(i), dataTypes.get(i), comments.get(i)).forEach(sql -> {
        try {
          stmt.addBatch(sql);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      });
    }

    return stmt.executeBatch();
  }

  @Override
  public boolean addColumn(Statement stmt, String tableName, String columnName, String comment) throws SQLException {
    return addColumn(stmt, tableName, columnName, "VARCHAR(255)", comment);
  }

  @Override
  public boolean addColumn(Statement stmt, String tableName, String columnName, String dataType, String comment)
      throws SQLException {
    if (existsColumn(stmt, tableName, columnName)) {
      log.debug("Column already exists. tableName: {}, columnName: {}", tableName, columnName);
      return false;
    }

    createAddColumnSqls(stmt, tableName, columnName, dataType, comment).forEach(sql -> {
      try {
        stmt.executeUpdate(sql);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    return true;
  }

  private List<String> createAddColumnSqls(Statement stmt, String tableName, String columnName, String dataType,
      String comment) throws SQLException {
    GcDatabaseProductNameEnum dbProductName = GcDatabaseProductNameEnum.fromConnection(stmt.getConnection());
    switch (dbProductName) {
      case MySQL:
      case MariaDB:
        return List.of(
            "ALTER TABLE %s ADD COLUMN %s %s NULL COMMENT '%s'".formatted(tableName, columnName, dataType, comment));

      case PostgreSQL:
        return List.of("ALTER TABLE %s ADD COLUMN %s %s".formatted(tableName, columnName, dataType),
            "COMMENT ON COLUMN %s.%s IS '%s'".formatted(tableName, columnName, comment));

      case Oracle:
        return List.of("ALTER TABLE %s ADD %s %s".formatted(tableName, columnName, dataType),
            "COMMENT ON COLUMN %s.%s IS '%s'".formatted(tableName, columnName, comment));

      default:
        throw new RuntimeException("Not supported db product name " + dbProductName);
    }
  }

  @Override
  public int[] dropColumns(Statement stmt, String tableName, List<String> columnNames) throws SQLException {
    Set<String> columnNameSet = getColumnNames(stmt, tableName);

    for (String columnName : columnNames) {
      if (!columnNameSet.contains(columnName)) {
        log.debug("Column not exists. tableName: {}, columnName: {}", tableName, columnName);
        continue;
      }

      stmt.addBatch("ALTER TABLE %s DROP COLUMN %s".formatted(tableName, columnName));
    }

    return stmt.executeBatch();
  }

  @Override
  public boolean dropColumn(Statement stmt, String tableName, String columnName) throws SQLException {
    if (!existsColumn(stmt, tableName, columnName)) {
      log.debug("Column not exists. tableName: {}, columnName: {}", tableName, columnName);
      return false;
    }

    GcDatabaseProductNameEnum dbProductName = GcDatabaseProductNameEnum.fromConnection(stmt.getConnection());
    switch (dbProductName) {
      case MySQL:
      case MariaDB:
      case Oracle:
        stmt.executeUpdate("ALTER TABLE %s DROP COLUMN %s".formatted(tableName, columnName));
        break;

      default:
        throw new RuntimeException("Not supported db product name " + dbProductName);
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

}
