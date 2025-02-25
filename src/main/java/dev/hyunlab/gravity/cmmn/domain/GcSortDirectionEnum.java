package dev.hyunlab.gravity.cmmn.domain;

/**
 */
public enum GcSortDirectionEnum {
  ASC("ASC"),
  DESC("DESC"),
  NONE("NONE");

  private final String value;

  GcSortDirectionEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static GcSortDirectionEnum of(String value) {
    for (GcSortDirectionEnum sortConditionEnum : GcSortDirectionEnum.values()) {
      if (sortConditionEnum.getValue().equalsIgnoreCase(value)) {
        return sortConditionEnum;
      }
    }
    return null;
  }
}
