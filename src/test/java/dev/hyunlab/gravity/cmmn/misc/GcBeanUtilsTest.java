package dev.hyunlab.gravity.cmmn.misc;

import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Test;

import dev.hyunlab.gravity.cmmn.domain.GcAtchmnflDto;
import dev.hyunlab.gravity.cmmn.domain.GcAtchmnflGroupDto;
import jakarta.validation.constraints.AssertTrue;

public class GcBeanUtilsTest {
  @Test
  void testCopyProperties() {
    GcAtchmnflDto sourceDto = new GcAtchmnflDto();
    sourceDto.setAtchmnflGroupId("아이디");

    GcAtchmnflGroupDto dto2 = GcBeanUtils.copyProperties(sourceDto, GcAtchmnflGroupDto.class);
    org.springframework.util.Assert.isTrue(dto2.getAtchmnflGroupId().equals(sourceDto.getAtchmnflGroupId()),
        "아이디가 일치하지 않습니다.");
  }
}
