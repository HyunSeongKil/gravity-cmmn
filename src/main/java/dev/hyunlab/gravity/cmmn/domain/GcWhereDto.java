package dev.hyunlab.gravity.cmmn.domain;

import dev.hyunlab.gravity.cmmn.misc.GcUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * whereCn XOR (whereColumnNm, whereConditionEnum, whereValue)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GcWhereDto {
  private String whereColumnNm;
  private GcWhereConditionEnum whereConditionEnum;
  private String whereValue;
  private String whereCn;

  public String getWhereClause() {

    if (GcUtils.isNotEmpty(whereCn)) {
      return " WHERE %s".formatted(whereCn);
    }

    if (GcUtils.isNotEmpty(whereColumnNm)) {
      return " WHERE %s %s '%s'".formatted(whereColumnNm, whereConditionEnum.getOperator(), whereValue);
    }

    return " WHERE 1=1";
  }
}
