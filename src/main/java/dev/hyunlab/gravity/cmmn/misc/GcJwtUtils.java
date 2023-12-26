package dev.hyunlab.gravity.cmmn.misc;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/**
 * jwt 관련 유틸리티
 */
public class GcJwtUtils {

  /**
   * default secret key
   */
  public static final String SECRET_KEY = "dev.hyunlab.gravity,aha1492@outlook.kr,aha1492@gmail.com";

  public static SecretKey createSecretKey(String key) {
    return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 토큰 생성
   * 만료시간 기본 10분으로 설정
   * 
   * @see createToken(String, Map, Date)
   * @param bodyMap
   * @return
   */
  public static String createToken(Map<String, Object> bodyMap) {
    Date exp = new Date();
    exp.setTime(exp.getTime() + GcConst.MIN_10); // 10min

    return createToken(bodyMap, exp);
  }

  /**
   * 토큰 생성
   * 
   * @see createToken(String, Map, Date)
   * @param bodyMap
   * @param expireDate
   * @return
   */
  public static String createToken(Map<String, Object> bodyMap, Date expireDate) {
    return createToken(SECRET_KEY, bodyMap, expireDate);
  }

  /**
   * 토큰 생성
   * ! 주의 : bodyMap은 immutation이 아니어야 함
   * 
   * @param secretKey 비밀키
   * @param bodyMap   body에 들어갈 값
   * @param exp       만료시간
   * @return
   */

  public static String createToken(String secretKey, Map<String, Object> bodyMap, Date exp) {
    return Jwts
        .builder()
        .claims(bodyMap)
        .expiration(exp)
        .signWith(createSecretKey(secretKey))
        .compact();
  }

  /**
   * 토큰 검사
   * 
   * @param secretKey
   * @param token
   * @return
   */
  public static boolean validateToken(String secretKey, String token) {
    try {
      Jwts
          .parser()
          .verifyWith(createSecretKey(secretKey))
          .build()
          .parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 토큰 검사
   * 
   * @see validateToken(String, String)
   * @param token
   * @return
   */
  public static boolean validateToken(String token) {
    return validateToken(SECRET_KEY, token);
  }

  /**
   * body값 구하기
   * 
   * @param token
   * @return
   */
  public static Map<String, Object> getBody(String token) {
    return getBody(SECRET_KEY, token);
  }

  /**
   * body값 구하기
   * 
   * @param secretKey
   * @param token
   * @return
   */
  public static Map<String, Object> getBody(String secretKey, String token) {
    Map<String, Object> map = new HashMap<>();

    if (!validateToken(secretKey, token)) {
      return map;
    }

    Jwts
        .parser()
        .verifyWith(createSecretKey(SECRET_KEY))
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .entrySet()
        .forEach(entry -> {
          map.put(entry.getKey(), entry.getValue());
        });

    return map;
  }
}
