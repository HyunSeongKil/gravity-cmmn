package dev.hyunlab.gravity.cmmn.domain;

public enum DbProductType {
  Oracle("Oracle"), MySQL("MySQL"), MariaDB("MariaDB"), MSSQL("MSSQL"), PostgreSQL("PostgreSQL");

  private String value;

  DbProductType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static DbProductType of(String productName) {
    for (DbProductType dbProductType : DbProductType.values()) {
      if (productName.toUpperCase().contains(dbProductType.getValue().toUpperCase())) {
        return dbProductType;
      }
    }
    return null;
  }

  public static boolean isMariaDB(String productName) {
    return productName.toUpperCase().contains(MariaDB.getValue().toUpperCase());
  }
}
