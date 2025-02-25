package dev.hyunlab.gravity.cmmn.domain;

import dev.hyunlab.gravity.cmmn.misc.GcUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcSortDto {
  private String sortColumnNm;
  private GcSortDirectionEnum sortDirectionEnum;

  public String getSortClause() {
    if (GcUtils.isNotEmpty(sortColumnNm)) {
      return " ORDER BY %s %s".formatted(sortColumnNm, sortDirectionEnum == null ? "" : sortDirectionEnum);
    }

    return "";
  }
}
