package dev.hyunlab.gravity.cmmn.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import dev.hyunlab.gravity.cmmn.domain.GcAtchmnflDto;

public interface GcAtchmnflService<DTO extends GcAtchmnflDto> {
  /**
   * 
   * @param mfile
   * @return {atchmnflGroupId:string, atchmnflId:string}
   */
  Map<String, String> regist(MultipartFile mfile);

  /**
   * 
   * @param mfiles
   * @return [{atchmnflGroupId:string, atchmnflId:string}]
   */
  List<Map<String, String>> regist(List<MultipartFile> mfiles);

  /**
   * 
   * @param atchmnflGroupId
   * @param mfile
   * @return {atchmnflGroupId:string, atchmnflId:string}
   */
  Map<String, String> regist(String atchmnflGroupId, MultipartFile mfile);

  /**
   * 
   * @param atchmnflGroupId
   * @param mfiles
   * @return [{atchmnflGroupId:string, atchmnflId:string}]
   */
  List<Map<String, String>> regist(String atchmnflGroupId, List<MultipartFile> mfiles);

  void deleteById(String atchmnflId);

  void deletesByAtchmnflGroupId(String atchmnflGroupId);

  Optional<DTO> getById(String atchmnflId);

  List<DTO> getsByAtchmnflGroupId(String atchmnflGroupId);

  File getFileById(String atchmnflId) throws Exception;

  File getFile(String saveSubPath, String saveFilename) throws Exception;

  List<File> getFilesByAtchmnflGroupId(String atchmnflGroupId) throws Exception;
}
