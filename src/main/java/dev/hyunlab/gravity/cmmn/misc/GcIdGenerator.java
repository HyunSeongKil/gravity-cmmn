package dev.hyunlab.gravity.cmmn.misc;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class GcIdGenerator implements IdentifierGenerator {

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
    // 문자로 시작하게 만들기
    return "x" + GcUtils.getYYMMDD() + GcUtils.uuid8();
  }

}
