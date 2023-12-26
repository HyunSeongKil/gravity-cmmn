package kr.vaiv.sdt.cmmn.misc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import dev.hyunlab.gravity.cmmn.misc.GcConst;
import dev.hyunlab.gravity.cmmn.misc.GcResultMap;

public class GcResultMapTest {

  @Test
  void ofTest() {
    GcResultMap resultMap = GcResultMap.of(List.of(), PageRequest.of(0, 10), 0L);
    System.out.println(resultMap);

    assertTrue(0 == ((List) resultMap.get(GcConst.DATA)).size());
    assertTrue(0 == (Integer) resultMap.get(GcConst.PAGE));
    assertTrue(10 == (Integer) resultMap.get(GcConst.SIZE));
    assertTrue(0L == (Long) resultMap.get(GcConst.TOTAL_COUNT));
  }
}
