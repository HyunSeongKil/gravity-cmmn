package dev.hyunlab.gravity.cmmn.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcTableMetaDto {
  private String tableName;
  private String tableComment;
}
