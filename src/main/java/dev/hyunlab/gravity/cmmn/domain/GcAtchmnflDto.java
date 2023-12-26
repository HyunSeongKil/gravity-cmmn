package dev.hyunlab.gravity.cmmn.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GcAtchmnflDto {
  private String atchmnflId;

  private String atchmnflGroupId;

  private String originalFilename;

  private String saveFilename;

  private Long fileSize;

  private String saveSubPath;

  private String contentType;

  private String registerId;
  private LocalDateTime registDt;
  private String updaterId;
  private LocalDateTime updateDt;

}
