package dev.hyunlab.gravity.cmmn.domain;

/**
 */
public enum GcWhereConditionEnum {
  EQUALS("EQUALS", " = "),
  NOT_EQUALS("NOT_EQUALS", " != "),
  GREATER_THAN("GREATER_THAN", " > "),
  GREATER_THAN_OR_EQUALS("GREATER_THAN_OR_EQUALS", " >= "),
  LESS_THAN("LESS_THAN", " < "),
  LESS_THAN_OR_EQUALS("LESS_THAN_OR_EQUALS", " <= "),
  NONE("NONE", "");
  // LIKE("LIKE", " LIKE "),
  // NOT_LIKE("NOT_LIKE", " NOT LIKE "),
  // IN("IN", " IN "),
  // NOT_IN("NOT_IN", " NOT IN "),
  // IS_NULL("IS_NULL", " IS NULL "),
  // IS_NOT_NULL("IS_NOT_NULL", " IS NOT NULL "),
  // BETWEEN("BETWEEN", " BETWEEN "),
  // NOT_BETWEEN("NOT_BETWEEN", " NOT BETWEEN ");
  ;

  private final String code;
  private final String operator;

  GcWhereConditionEnum(String code, String operator) {
    this.code = code;
    this.operator = operator;
  }

  public String getCode() {
    return code;
  }

  public String getOperator() {
    return operator;
  }

  public static GcWhereConditionEnum of(String code) {
    for (GcWhereConditionEnum e : values()) {
      if (e.getCode().equalsIgnoreCase(code)) {
        return e;
      }
    }
    return null;
  }
}
