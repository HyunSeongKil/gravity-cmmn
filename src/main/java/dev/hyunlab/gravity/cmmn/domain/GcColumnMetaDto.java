package dev.hyunlab.gravity.cmmn.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ! 중요. gravity-cmmn으로 이동 예정
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcColumnMetaDto {
  private String columnName;
  private String dataType;
  private String columnComment;
  private Integer dataLength;
  private Integer dataPrecision;
  private boolean primaryKey;
  private boolean nullable;
}
