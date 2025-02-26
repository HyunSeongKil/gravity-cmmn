package dev.hyunlab.gravity.cmmn.domain;

/**
 * @deprecated Use
 *             {@link dev.hyunlab.gravity.cmmn.domain.GcDatabaseProductNameEnum}
 *             instead
 * @since 2025-02-26
 */
public enum DbType {
  MariaDB("MariaDB", "마리아디비"),
  Oracle("Oracle", "오라클"),
  MSSQL("MSSQL", "마이크로소프트SQL"),
  MySQL("MySQL", "마이에스큐엘"),
  PostgreSQL("PostgreSQL", "포스트그레스큐엘"),
  SQLite("SQLite", "SQL라이트");

  private String code;
  private String name;

  DbType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public String getCode() {
    return this.code;
  }

  public String getName() {
    return this.name;
  }

  public static DbType of(String code) {
    for (DbType type : DbType.values()) {
      if (type.getCode().equals(code)) {
        return type;
      }
    }
    return null;
  }
}
