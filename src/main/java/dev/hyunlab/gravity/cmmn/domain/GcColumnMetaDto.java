package dev.hyunlab.gravity.cmmn.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

/**
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcColumnMetaDto {
  private String columnName;

  @Default
  private String dataType = "varchar(100)";

  @Default
  private String columnComment = "";

  @Default
  private Integer dataLength = 10;

  @Default
  private Integer dataPrecision = 0;

  @Default
  private boolean primaryKey = false;

  @Default
  private boolean nullable = true;
}
