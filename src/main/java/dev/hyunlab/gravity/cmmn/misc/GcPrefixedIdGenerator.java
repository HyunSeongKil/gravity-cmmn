package dev.hyunlab.gravity.cmmn.misc;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import lombok.extern.slf4j.Slf4j;

/**
 * 랜덤 ID 생성기
 * 형식 : prefix + yymmdd + uuid8
 * prefix default value : x
 * 
 * @since 2025-03-13
 * @author hyun
 */
@Slf4j
public class GcPrefixedIdGenerator implements IdentifierGenerator {

  private static final ThreadLocal<String> PREFIX_HOLDER = new ThreadLocal<>();

  public GcPrefixedIdGenerator() {
    // default value
    PREFIX_HOLDER.set("x");

    log.info("<< GcPrefixedIdGenerator, prefix:{}", PREFIX_HOLDER.get());
  }

  public static void setPrefix(String prefix) {
    // FIXME prefix 유효성 검증

    GcPrefixedIdGenerator.PREFIX_HOLDER.set(prefix);
  }

  public static void removePrefix() {
    GcPrefixedIdGenerator.PREFIX_HOLDER.remove();
  }

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
    // 문자로 시작하게 만들기
    return PREFIX_HOLDER + GcUtils.getYYMMDD() + GcUtils.uuid8();
  }

}
