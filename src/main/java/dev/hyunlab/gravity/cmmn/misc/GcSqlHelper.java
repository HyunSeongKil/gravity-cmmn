package dev.hyunlab.gravity.cmmn.misc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.hyunlab.gravity.cmmn.domain.GcColumnMetaDto;
import dev.hyunlab.gravity.cmmn.domain.GcDatabaseProductNameEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GcSqlHelper {

  public static String createExistsTableSql(GcDatabaseProductNameEnum dbNameEnum, String tableName) {
    switch (dbNameEnum) {
      case MySQL:
      case MariaDB:
      case PostgreSQL:
        return "SELECT * FROM information_schema.tables WHERE table_name = '%s'".formatted(tableName);

      case Oracle:
        return "SELECT * FROM user_tables WHERE table_name = '%s'".formatted(tableName);
      default:
        throw new RuntimeException("Not supported db type " + dbNameEnum);
    }
  }

  public static String createExistsColumnSql(GcDatabaseProductNameEnum dbNameEnum, String tableName,
      String columnName) {
    switch (dbNameEnum) {
      case MySQL:
      case MariaDB:
        return "SELECT * FROM information_schema.columns WHERE table_name = '%s' AND column_name = '%s'"
            .formatted(tableName, columnName);
      case PostgreSQL:
        return "SELECT * FROM information_schema.columns WHERE table_name = '%s' AND column_name = '%s'"
            .formatted(tableName, columnName);
      case Oracle:
        return "SELECT * FROM user_tab_columns WHERE table_name = '%s' AND column_name = '%s'"
            .formatted(tableName, columnName);
      default:
        throw new RuntimeException("Not supported db type " + dbNameEnum);

    }
  }

  public static List<String> createChangeColumnSqls(GcDatabaseProductNameEnum dbNameEnum, String tableName,
      String oldColumnName, GcColumnMetaDto newColumnMetaDto) {
    switch (dbNameEnum) {
      case MySQL:
      case MariaDB:
        return List.of(
            "ALTER TABLE %s CHANGE COLUMN %s %s %s NULL COMMENT '%s'"
                .formatted(tableName,
                    oldColumnName,
                    newColumnMetaDto.getColumnName(),
                    newColumnMetaDto.getDataType(),
                    newColumnMetaDto.getColumnComment()));
      case PostgreSQL:
        return List.of(
            "ALTER TABLE %s RENAME COLUMN %s TO %s"
                .formatted(tableName, oldColumnName, newColumnMetaDto.getColumnName()),
            "ALTER TABLE %s ALTER COLUMN %s TYPE %s"
                .formatted(tableName, newColumnMetaDto.getColumnName(), newColumnMetaDto.getDataType()),
            "COMMENT ON COLUMN %s.%s IS '%s'"
                .formatted(tableName, newColumnMetaDto.getColumnName(), newColumnMetaDto.getColumnComment()));

      case Oracle:
        return List.of(
            "ALTER TABLE %s RENAME COLUMN %s TO %s"
                .formatted(tableName, oldColumnName, newColumnMetaDto.getColumnName()),
            "ALTER TABLE %s MODIFY %s %s"
                .formatted(tableName, newColumnMetaDto.getColumnName(), newColumnMetaDto.getDataType()),
            "COMMENT ON COLUMN %s.%s IS '%s'"
                .formatted(tableName, newColumnMetaDto.getColumnName(), newColumnMetaDto.getColumnComment()));
      default:
        throw new RuntimeException("Not supported db type " + dbNameEnum);
    }

  }

  public static List<String> createDropColumnSqls(GcDatabaseProductNameEnum dbNameEnum, String tableName,
      String columnName) {
    switch (dbNameEnum) {
      case MySQL:
      case MariaDB:
      case PostgreSQL:
      case Oracle:
        return List.of("ALTER TABLE %s DROP COLUMN %s".formatted(tableName, columnName));
      default:
        throw new RuntimeException("Not supported db type " + dbNameEnum);
    }

  }

  public static List<String> createAddColumnSqls(GcDatabaseProductNameEnum dbNameEnum, String tableName,
      GcColumnMetaDto columnMetaDto) {
    switch (dbNameEnum) {
      case MySQL:
      case MariaDB:
        return List.of("ALTER TABLE %s ADD COLUMN %s %s NULL COMMENT '%s'"
            .formatted(tableName,
                columnMetaDto.getColumnName(),
                columnMetaDto.getDataType(),
                columnMetaDto.getColumnComment()));
      case PostgreSQL:
        return List.of(
            "ALTER TABLE %s ADD COLUMN %s %s NULL"
                .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getDataType()),
            "COMMENT ON COLUMN %s.%s IS '%s'"
                .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getColumnComment()));
      case Oracle:
        return List.of(
            "ALTER TABLE %s ADD %s %s NULL"
                .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getDataType()),
            "COMMENT ON COLUMN %s.%s IS '%s'"
                .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getColumnComment()));
      default:
        throw new RuntimeException("Not supported db type " + dbNameEnum);
    }
  }

  public static List<String> createCopyTableWithDatasSqls(GcDatabaseProductNameEnum dbNameEnum, String exstingTableName,
      String newTableName) {
    switch (dbNameEnum) {
      case MySQL:
      case MariaDB:
      case Oracle:
        return List.of("CREATE TABLE %s AS SELECT * FROM %s".formatted(newTableName, exstingTableName));
      case PostgreSQL:
        return List.of("CREATE TABLE %s AS TABLE %s".formatted(newTableName, exstingTableName));
      default:
        throw new RuntimeException("Not supported db type " + dbNameEnum);
    }

  }

  public static List<String> createChangeTableNameSqls(GcDatabaseProductNameEnum dbNameEnum, String oldTableName,
      String newTableName) {
    switch (dbNameEnum) {
      case MySQL:
      case MariaDB:
      case PostgreSQL:
      case Oracle:
        return List.of("ALTER TABLE %s RENAME TO %s".formatted(oldTableName, newTableName));

      default:
        throw new RuntimeException("Not supported db type " + dbNameEnum);
    }
  }

  public static List<String> createDropTableSqls(GcDatabaseProductNameEnum dbNameEnum, String tableName) {
    switch (dbNameEnum) {
      case MariaDB:
      case MySQL:
      case PostgreSQL:
        return List.of("DROP TABLE IF EXISTS %s".formatted(tableName));
      case Oracle:
        return List.of("DROP TABLE %s".formatted(tableName));
      default:
        throw new RuntimeException("Not supported db type " + dbNameEnum);
    }
  }

  public static List<String> createCreateTableSqls(GcDatabaseProductNameEnum dbNameEnum, String tableName,
      List<GcColumnMetaDto> columnMetaDtos) {
    // #region inner
    Supplier<List<String>> forMariadb = () -> {
      List<String> columns = columnMetaDtos
          .stream()
          .map(dto -> {
            return " `%s` %s %s COMMENT '%s'"
                .formatted(dto.getColumnName(),
                    dto.getDataType(),
                    dto.isPrimaryKey() ? "primary key" : "",
                    dto.getColumnComment());
          })
          .toList();

      return List.of(" CREATE TABLE %s ( %s ) COMMENT ''".formatted(tableName, String.join(",", columns)));
    };

    Supplier<List<String>> forPostgre = () -> {
      List<String> columns = columnMetaDtos
          .stream()
          .map(dto -> {
            return " %s %s %s"
                .formatted(dto.getColumnName(),
                    dto.getDataType(),
                    dto.isPrimaryKey() ? "primary key" : "");
          })
          .toList();

      List<String> sqls = new ArrayList<>();
      sqls.add(" CREATE TABLE %s ( %s ) ".formatted(tableName, String.join(",", columns)));
      sqls.add(" COMMENT ON TABLE %s IS '%s'".formatted(tableName));
      columnMetaDtos.forEach(columnMetaDto -> {
        sqls.add(" COMMENT ON COLUMN %s.%s IS '%s'"
            .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getColumnComment()));
      });

      return sqls;
    };

    Supplier<List<String>> forOracle = () -> {
      List<String> columns = columnMetaDtos
          .stream()
          .map(dto -> {
            return " %s %s %s"
                .formatted(dto.getColumnName(),
                    dto.getDataType(),
                    dto.isPrimaryKey() ? "as identify primary key" : "");
          })
          .toList();

      List<String> sqls = new ArrayList<>();
      sqls.add(" CREATE TABLE %s ( %s ) ".formatted(tableName, String.join(",", columns)));
      sqls.add(" COMMENT ON TABLE %s IS '%s'".formatted(tableName));
      columnMetaDtos.forEach(columnMetaDto -> {
        sqls.add(" COMMENT ON COLUMN %s.%s IS '%s'"
            .formatted(tableName, columnMetaDto.getColumnName(), columnMetaDto.getColumnComment()));
      });

      return sqls;
    };
    // #endregion inner

    switch (dbNameEnum) {
      case MariaDB:
      case MySQL:
        return forMariadb.get();

      case PostgreSQL:
        return forPostgre.get();

      case Oracle:
        return forOracle.get();

      default:
        throw new RuntimeException("Not supported db type " + dbNameEnum);
    }

  }

  public static List<String> createInsertSqls(GcDatabaseProductNameEnum dbNameEnum, String tableName,
      List<Map<String, Object>> listOfMap) {
    // #region inner
    Function<Map<String, Object>, String> columns = (map) -> {
      List<String> list = map.entrySet()
          .stream()
          .map(entry -> {
            return " %s".formatted(entry.getKey());
          })
          .toList();

      return String.join(",", list);

    };

    Function<Map<String, Object>, String> values = (map) -> {
      List<String> list = map.entrySet()
          .stream()
          .map(entry -> {
            if (entry.getValue().getClass() == String.class) {
              return " '%s'".formatted(entry.getValue().toString().replaceAll("'", ""));
            } else if (entry.getValue().getClass() == Date.class) {
              return " now()";
            } else {
              return " %s".formatted(entry.getValue());
            }
          })
          .toList();

      return String.join(",", list);
    };

    // #endregion inner

    return listOfMap
        .stream()
        .map(map -> {
          return " INSERT INTO %s (%s) VALUES (%s)"
              .formatted(tableName, columns.apply(map), values.apply(map));
        })
        .toList();
  }
}
