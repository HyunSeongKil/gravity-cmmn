package dev.hyunlab.gravity.cmmn.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GcAtchmnflGroupDto {
  private String atchmnflGroupId;

  private String registerId;
  private LocalDateTime registDt;
  private String updaterId;
  private LocalDateTime updateDt;
}
