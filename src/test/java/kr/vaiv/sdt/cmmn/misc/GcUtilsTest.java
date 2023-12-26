package kr.vaiv.sdt.cmmn.misc;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;

import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Test;

import dev.hyunlab.gravity.cmmn.misc.GcUtils;
import jakarta.validation.constraints.AssertTrue;

public class GcUtilsTest {

  @Test
  void uuid12Test() {
    String uuid12 = GcUtils.uuid12();
    org.springframework.util.Assert.isTrue(uuid12.length() == 12, "성공");
  }

  @Test
  void getMemoryInfoMapTest() {
    System.out.println(GcUtils.getMemoryInfoMap());
  }

  @Test
  void getCpuUsageTest() {
    System.out.println(GcUtils.getAllDiskInfos());
    System.out.println(GcUtils.getMemoryInfoMap());
    System.out.println(GcUtils.getOsInfoMap());
  }

  public static void showDisk(File drive) {

    // 현재 가지고 있는 디스크의 크기 확인하는 코드

    try {
      System.out.println("Total  Space: " + (int) (drive.getTotalSpace() / 1024) + "kbytes");
      System.out.println("Usable Space: " + (int) (drive.getUsableSpace() / 1024) + "kbytes");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void showMemory() {

    // 자바 힙메모리 크기 확인하는 코드
    MemoryMXBean membean = (MemoryMXBean) ManagementFactory.getMemoryMXBean();
    System.out.println(membean.getHeapMemoryUsage());
    System.out.println(membean.getNonHeapMemoryUsage());
    System.out.println(GcUtils.getMemoryInfoMap());
    // MemoryUsage heap = membean.getHeapMemoryUsage();
    // System.out.println("Heap Memory: " + heap.getUsed() / 1024 / 1024 + "MB");
    // MemoryUsage nonheap = membean.getNonHeapMemoryUsage();
    // System.out.println("NonHeap Memory: " + nonheap.getUsed() / 1024 / 1024 +
    // "MB");
  }
}
