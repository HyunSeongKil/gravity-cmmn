package dev.hyunlab.gravity.cmmn.misc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * result map
 * 
 * @since 20210721
 */
public class GcResultMap extends HashMap<String, Object> {

  /**
   * 빈값 리턴
   * 
   * @return
   */
  public static GcResultMap empty() {
    return withData(Map.of());
  }

  /**
   * 생성 with 데이터
   * 
   * @see 클린코드 p32
   * @param data 데이터
   * @return
   * @since 20220203
   */
  public static GcResultMap withData(Object data) {
    return of(data);
  }

  /**
   * 생성 with code, message
   * 보통, 데이터없이 오류코드와 메시지만 리턴할 때 사용
   * 
   * @param code
   * @param message
   * @return
   */
  public static GcResultMap withCode(String code, String message) {
    return of(Map.of(), code, message);
  }

  /**
   * 생성
   * 
   * @param data 데이터
   * @return 인스턴스
   */
  public static GcResultMap of(Object data) {
    GcResultMap map = new GcResultMap();
    map.put(GcConst.DATA, data);
    return map;
  }

  /**
   * 생성
   * 
   * @param data       데이터
   * @param resultCode 결과 코드
   * @return 인스턴스
   */
  public static GcResultMap of(Object data, String resultCode) {
    GcResultMap map = new GcResultMap();
    map.put(GcConst.DATA, data);
    return map;
  }

  /**
   * 생성
   * 
   * @param data    데이터
   * @param code    결과 코드
   * @param message 결과 메시지
   * @return 인스턴스
   */
  public static GcResultMap of(Object data, String code, Object message) {
    GcResultMap map = new GcResultMap();
    map.put(GcConst.DATA, data);

    map.put(GcConst.CODE, code);
    map.put(GcConst.MESSAGE, message);

    return map;
  }

  /**
   * 생성
   * 
   * @param data       데이터
   * @param page       페이지 인덱스. ※주의:0부터 시작
   * @param size       페이지 크기
   * @param totalCount 전체 건수
   * @return
   */
  public static GcResultMap of(Object data, Integer page, Integer size, Long totalCount) {
    GcResultMap map = new GcResultMap();
    map.put(GcConst.DATA, data);
    map.put(GcConst.PAGE, page);
    map.put(GcConst.SIZE, size);
    map.put(GcConst.TOTAL_COUNT, totalCount);

    return map;
  }

  /**
   * 생성
   * 
   * @param data       데이터
   * @param pageable   페이지 정보
   * @param totalCount 전체 건수
   * @return
   */
  public static GcResultMap of(Object data, Pageable pageable, Long totalCount) {
    return of(data, pageable.getPageNumber(), pageable.getPageSize(), totalCount);
  }

  /**
   * 데이터 설정
   * 
   * @param data 데이터
   */
  public void putData(Object data) {
    this.put(GcConst.DATA, data);
  }

  public void putCode(String code) {
    this.put(GcConst.CODE, code);
  }

  public void putMessage(Object message) {
    this.put(GcConst.MESSAGE, message);
  }

  /**
   * 페이지 인덱스 설정
   * 
   * @param page 페이지 인덱스. 주의 0부터 시작
   */
  public void putPage(Integer page) {
    this.put(GcConst.PAGE, page);
  }

  /**
   * 페이지 크기 설정
   * 
   * @param size 페이지 크기
   */
  public void putSize(Integer size) {
    this.put(GcConst.SIZE, size);
  }

  public void putTotalCount(Long totalCount) {
    this.put(GcConst.TOTAL_COUNT, totalCount);
  }

  /**
   * @deprecated from 1.23.0308
   * @see CmmnBeanUtils.toJsonString(Object)
   * @return
   * @throws JsonProcessingException
   */
  public String toJsonString() throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(this);
  }
}
