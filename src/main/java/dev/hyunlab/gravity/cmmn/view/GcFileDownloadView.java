package dev.hyunlab.gravity.cmmn.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.view.AbstractView;

import dev.hyunlab.gravity.cmmn.misc.GcUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 다운로드
 */
@Component
@Slf4j
public class GcFileDownloadView extends AbstractView {
  /**
   * 파일 인스턴스. 선택
   */
  public static final String FILE = "file";

  /**
   * 파일 명. 선택
   */
  public static final String FILE_NAME = "fileName";

  /**
   * 바이트 배열. 선택
   */
  public static final String BYTES = "bytes";

  public void Download() {
    setContentType("application/octet-stream; utf-8");
  }

  @Override
  protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    /**
     * 
     */
    Runnable loggingModelEntries = () -> {
      for (Map.Entry<String, Object> entry : model.entrySet()) {
        // log.debug("{}\t{}", entry.getKey(), entry.getValue());
      }
    };

    /**
     * 
     */
    Supplier<Boolean> validateModel = () -> {
      if (!model.containsKey(BYTES) && !model.containsKey(FILE)) {
        log.error("BYTES AND FILE IS NULL");
        return true;
      }

      if (model.containsKey(FILE)) {
        File file = (File) model.get(FILE);
        if (!file.exists()) {
          log.error("{} NOT FOUND", file.toPath());
          return false;
        }
      }

      return true;
    };

    /**
     * 
     */
    Function<String, String> processFilename = (fileName) -> {

      boolean b = request.getHeader("User-Agent").indexOf("MSIE") > -1;

      if (b) {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8);
      }

      try {
        return new String(fileName.getBytes(StandardCharsets.UTF_8), "iso-8859-1");
      } catch (UnsupportedEncodingException e) {
        log.error("{}", e);
      }

      return "";

    };

    /**
     * 
     */
    Consumer<String> setResponseHeaderValue = (fileName) -> {
      response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\";");
      response.setHeader("Content-Transfer-Encoding", "binary");
    };

    /**
     * 
     */
    Runnable writeFile = () -> {
      try (OutputStream out = response.getOutputStream()) {
        File file = (File) model.get(FILE);
        try (FileInputStream fis = new FileInputStream(file)) {
          FileCopyUtils.copy(fis, out);
        }

        out.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    };

    Runnable writeBytes = () -> {
      byte[] bytes = (byte[]) model.get(BYTES);

      response.setContentLength(bytes.length);
      try (OutputStream out = response.getOutputStream()) {
        out.write(bytes);
        out.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };

    Supplier<String> getFileName = () -> {
      if (model.containsKey(FILE_NAME)) {
        return model.get(FILE_NAME).toString();
      }

      return GcUtils.uuid12();
    };
    ////

    setContentType("application/octet-stream; utf-8");

    loggingModelEntries.run();

    if (!validateModel.get()) {
      return;
    }

    String fileName = processFilename.apply(getFileName.get());
    response.setContentType(getContentType());

    setResponseHeaderValue.accept(fileName);

    //
    if (model.containsKey(FILE)) {
      File file = (File) model.get(FILE);
      response.setContentLength((int) file.length());

      writeFile.run();
      return;
    }

    //
    if (model.containsKey(BYTES)) {
      writeBytes.run();
      return;
    }

  }

}
