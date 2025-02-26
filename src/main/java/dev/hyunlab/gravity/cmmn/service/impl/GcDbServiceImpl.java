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
  public boolean addColumn(Statement stmt, String tableName, String columnName, String comment) throws SQLException {
    return addColumn(stmt, tableName, columnName, "VARCHAR(255)", comment);
  }

  @Override
  public boolean addColumn(Statement stmt, String tableName, String columnName, String dataType, String comment)
      throws SQLException {
    GcDatabaseProductNameEnum dbProductName = GcDatabaseProductNameEnum.fromConnection(stmt.getConnection());
    switch (dbProductName) {
      case MYSQL:
      case MARIADB:
        try (ResultSet rs = stmt.executeQuery(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = '%s' AND column_name = '%s'"
                .formatted(tableName, columnName))) {
          rs.next();
          if (rs.getInt(1) > 0) {
            return false;
          }
        }

        stmt.executeUpdate(
            "ALTER TABLE %s ADD COLUMN %s %s NULL COMMENT '%s'".formatted(tableName, columnName, dataType, comment));
        return true;

      case ORACLE:
        try (ResultSet rs = stmt
            .executeQuery("SELECT COUNT(*) FROM all_tab_columns WHERE table_name = '%s' AND column_name = '%s'"
                .formatted(tableName, columnName))) {
          rs.next();
          if (rs.getInt(1) > 0) {
            return false;
          }
        }

        stmt.executeUpdate("ALTER TABLE %s ADD %s %s".formatted(tableName, columnName, dataType));
        stmt.executeUpdate("COMMENT ON COLUMN %s.%s IS '%s'".formatted(tableName, columnName, comment));
        break;

      default:
        throw new RuntimeException("Not supported db product name " + dbProductName);
    }

    return true;
  }

  @Override
  public boolean dropColumn(Statement stmt, String tableName, String columnName) throws SQLException {
    GcDatabaseProductNameEnum dbProductName = GcDatabaseProductNameEnum.fromConnection(stmt.getConnection());
    switch (dbProductName) {
      case MYSQL:
      case MARIADB:
        stmt.executeUpdate("ALTER TABLE %s DROP COLUMN IF EXISTS %s".formatted(tableName, columnName));
        return true;

      case ORACLE:
        try (ResultSet rs = stmt
            .executeQuery("SELECT COUNT(*) FROM all_tab_columns WHERE table_name = '%s' AND column_name = '%s'"
                .formatted(tableName, columnName))) {
          rs.next();
          if (rs.getInt(1) == 0) {
            return false;
          }

          stmt.executeUpdate("ALTER TABLE %s DROP COLUMN %s".formatted(tableName, columnName));
        }
        return true;

      default:
        throw new RuntimeException("Not supported db product name " + dbProductName);
    }

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
