package dev.hyunlab.gravity.cmmn.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import net.coobird.thumbnailator.Thumbnails;
import ua_parser.Client;
import ua_parser.Parser;

public class GcUtils {

  /**
   * 이미지 파일 확장자 목록
   */
  static List<String> IMAGE_EXTS = List.of(".GIF", ".PNG", ".JPG", ".JPEG", ".BMP");

  public static String encodeBase64(String str) {
    return new String(Base64.getEncoder().encode(str.getBytes(StandardCharsets.UTF_8)));
  }

  public static String decodeBase64(String str) throws UnsupportedEncodingException {
    return new String(Base64.getDecoder().decode(str), "UTF-8");
  }

  /**
   * uuid 문자열 생성. 하이픈은 제거됨
   * ! 1st char는 항상 문자임
   * 
   * @return
   */
  public static String uuid() {
    String str;
    while (true) {
      str = UUID.randomUUID().toString();

      // 첫글자가 문자이면
      if (!Character.isDigit(str.charAt(0))) {
        str = str.replaceAll("-", "");
        break;
      }
    }

    return str;
  }

  /**
   * 짧은(uuid의 앞 8자리) uuid 생성
   * ! 1st char는 항상 문자임
   * 
   * @return
   */
  @Deprecated(since = "20240100", forRemoval = true)
  public static String shortUuid() {
    return uuid().substring(0, 8);
  }

  /**
   * @see shortUuid()
   * @return
   */
  public static String uuid8() {
    return shortUuid();
  }

  /**
   * 짧은(uuid의 앞 12자리) uuid 생성
   * ! 1st char는 항상 문자임
   * 
   * @return
   */
  public static String uuid12() {
    return uuid().substring(0, 12);
  }

  /**
   * 
   * @param str camelToSnake
   * @return camel_to_snake
   */
  public static String camelToSnake(String str) {
    // Regular Expression
    String regex = "([a-z])([A-Z]+)";

    // Replacement string
    String replacement = "$1_$2";

    // Replace the given regex
    // with replacement string
    // and convert it to lower case.
    str = str
        .replaceAll(regex, replacement)
        .toLowerCase();

    // return string
    return str;
  }

  /**
   * 
   * @param str snake_to_camel
   * @return snakeToCamel
   */
  public static String snakeToCamel(String str) {
    return str.indexOf("_") != -1
        ? str.substring(0, str.indexOf("_")) +
            Arrays
                .stream(str.substring(str.indexOf("_") + 1).split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining())
        : str;
  }

  /**
   * 임시 디렉터리에 압축 파일 생성
   * 
   * @param srcPaths
   * @return
   * @throws IOException
   */
  public static Path zip(List<Path> srcPaths) throws IOException {
    return zip(srcPaths, getTempPath());
  }

  /**
   * 압축 파일 생성
   * 
   * @param srcPaths
   * @param destPath
   * @return
   * @throws IOException
   */
  public static Path zip(List<Path> srcPaths, Path destPath) throws IOException {
    String filename = srcPaths.size() +
        "ea_" +
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
        ".zip";

    return zip(srcPaths, destPath, filename);
  }

  /**
   * 압축 파일 생성
   * 
   * @param srcPaths
   * @param destPath
   * @param filename
   * @return
   * @throws IOException
   */
  public static Path zip(List<Path> srcPaths, Path destPath, String filename) throws IOException {
    destPath.toFile().mkdirs();

    try (final FileOutputStream fos = new FileOutputStream(destPath.resolve(filename).toString())) {
      try (ZipOutputStream zipOut = new ZipOutputStream(fos)) {

        for (Path p : srcPaths) {
          try (FileInputStream fis = new FileInputStream(p.toFile())) {
            ZipEntry zipEntry = new ZipEntry(p.getFileName().toString());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
              zipOut.write(bytes, 0, length);
            }

          }
        }
      }
    }

    return destPath.resolve(filename);
  }

  /**
   * 8자리의 랜덤 문자열 생성
   */
  @Deprecated(since = "20240100", forRemoval = true)
  public static String createRandom8String() {
    return GcUtils.createRandom8String("");
  }

  /*
   * 8자리의 랜덤 문자열 생성성
   * 
   * @param pre 접두어
   */
  public static String createRandom8String(String pre) {
    return pre + GcUtils.shortUuid();
  }

  /**
   * 오래된 디렉터리/파일 모두(하위 디렉터리/파일) 삭제
   * 
   * @param basePath 기준이 되는 경로
   * @param millis   기준이 되는 밀리초
   * @throws IOException
   */
  public static void deleteOldPath(Path basePath, long millis) throws IOException {
    Files
        .walk(basePath)
        .forEach(path -> {
          try {
            if (path.toFile().lastModified() < millis) {
              FileUtils.forceDelete(path.toFile());
            }
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  /**
   * java의 임시 경로 조회
   * 
   * @return
   */
  public static Path getTempPath() {
    return Paths.get(System.getProperty("java.io.tmpdir"));
  }

  /**
   * 파일의 확장자 추출(점(.) 포함)
   * 
   * @param file
   * @return
   */
  public static String getExtension(File file) {
    if (isNull(file)) {
      return "";
    }

    return getExtension(file.getName());
  }

  /**
   * 문자열의 확장자 추출(점(.) 포함)
   * 
   * @param filename
   * @return
   */
  public static String getExtension(String filename) {
    if (isEmpty(filename)) {
      return "";
    }

    if (!filename.contains(".")) {
      return "";
    }

    return filename.substring(filename.lastIndexOf("."));
  }

  /**
   * 전체 메모리 용량 조회. 단위:bytes
   * 
   * @return
   */
  public static Map<String, Long> getMemoryInfoMap() {
    return Map.of("total", Runtime.getRuntime().totalMemory(), "free", Runtime.getRuntime().freeMemory());
  }

  /**
   * 모든 드라이브의 정보(total,free) 목록 조회
   * 
   * @return
   */
  public static List<Map<String, Object>> getAllDiskInfos() {
    File[] drivers = File.listRoots();
    return getDiskInfos(Arrays.asList(drivers));
  }

  /**
   * 드라이브 목록의 정보(total,free) 조회
   * 
   * @param drives
   * @return
   */
  public static List<Map<String, Object>> getDiskInfos(List<File> drives) {
    return drives.stream().map(drive -> {
      return getDiskInfoMap(drive);
    }).collect(Collectors.toList());
  }

  /**
   * 드라이브의 정보(total,free) 조회. 단위:바이트
   * 
   * @param drive
   * @return
   */
  public static Map<String, Object> getDiskInfoMap(File drive) {
    return Map.of("drive", drive, "total", drive.getTotalSpace(), "free", drive.getUsableSpace());
  }

  /**
   * os 정보(명, 아키텍처, 프로세서 수) 조회
   * 
   * @return
   */
  public static Map<String, Object> getOsInfoMap() {
    return Map.of("name", ManagementFactory.getOperatingSystemMXBean().getName(), "arch",
        ManagementFactory.getOperatingSystemMXBean().getArch(), "processors",
        ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors());
  }

  /**
   * 클라이언트 ip 구하기
   * 
   * @param request
   * @return
   */
  public static String getIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (GcUtils.isNotEmpty(ip)) {
      return ip;
    }

    ip = request.getHeader("Proxy-Client-IP");
    if (GcUtils.isNotEmpty(ip)) {
      return ip;
    }

    ip = request.getHeader("WL-Proxy-Client-IP");
    if (GcUtils.isNotEmpty(ip)) {
      return ip;
    }

    ip = request.getHeader("HTTP_CLIENT_IP");
    if (GcUtils.isNotEmpty(ip)) {
      return ip;
    }

    ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    if (GcUtils.isNotEmpty(ip)) {
      return ip;
    }

    ip = request.getRemoteAddr();
    if (GcUtils.isNotEmpty(ip)) {
      return ip;
    }

    return ip;
  }

  /**
   * post방식으로 전달된 파라미터를 문자열로 추출해 리턴
   * 
   * @param request
   * @return
   * @throws IOException
   */
  public static String getBody(HttpServletRequest request) throws IOException {
    StringBuilder sb = new StringBuilder();

    try (InputStream inputStream = request.getInputStream()) {
      if (null == inputStream) {
        return sb.toString();
      }

      try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
        char[] charBuffer = new char[128];
        int bytesRead = -1;
        while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
          sb.append(charBuffer, 0, bytesRead);
        }
      }
    }

    return sb.toString();
  }

  /**
   * parse user-agent
   * 
   * @param request
   * @return
   * @throws IOException
   */
  public static Client parseUserAgent(HttpServletRequest request) throws IOException {
    Enumeration<String> userAgents = request.getHeaders("user-agent");

    while (userAgents.hasMoreElements()) {
      String agent = userAgents.nextElement();
      Parser parser = new Parser();

      return parser.parse(agent);
    }

    return null;
  }

  /**
   * 널여부 검사
   * 
   * @param obj 오브젝트
   * @return 널이면 true
   */
  public static boolean isNull(Object obj) {
    return (null == obj);
  }

  /**
   * 공백 여부
   * 
   * @param obj 오브젝트. String|Collection|Map|Set|List|배열
   * @return 공백이면 true
   * @since 20180322 배열, 리스트 처리 추가 20200221 Map관련 추가
   */
  @SuppressWarnings("rawtypes")
  public static boolean isEmpty(Object obj) {
    if (isNull(obj)) {
      return true;
    }

    // 문자열
    if (String.class == obj.getClass()) {
      return (0 == obj.toString().trim().length());
    }

    //
    if (obj instanceof Collection) {
      return (0 == ((Collection) obj).size());
    }

    //
    if (obj instanceof Map) {
      return (0 == ((Map) obj).size());
    }

    //
    if (Set.class == obj.getClass()) {
      return (0 == ((Set) obj).size());
    }

    // 리스트
    if (List.class == obj.getClass() || (ArrayList.class == obj.getClass())) {
      return (0 == ((List) obj).size());
    }

    // 배열
    if (obj.getClass().isArray()) {
      return (0 == Array.getLength(obj));
    }

    //
    return (0 == obj.toString().length());
  }

  /**
   * isEmpty의 반대
   * 
   * @param obj 문자열
   * @return true / false true 조건 문자열인 경우 공백이 아니면 collection(Set, List,...)인 경우 0
   *         &lt; size 배열인 경우 0 &lt; length Map인 경우 0 &lt; size
   */
  public static boolean isNotEmpty(Object obj) {
    return !isEmpty(obj);
  }

  /**
   * !널여부
   * 
   * @param obj 오브젝트
   * @return 널이 아니면 true
   */
  public static boolean isNotNull(Object obj) {
    return !isNull(obj);
  }

  /**
   * 이미지 파일인지 여부. 확장자로 검사
   * 
   * @param file 파일
   * @return 이미지 파일이면 true
   */
  public static boolean isImageFile(File file) {
    if (null == file || !file.exists()) {
      return false;
    }

    return isImageFile(file.getName());
  }

  /**
   * 이미지 파일인지 여부. 확장자로 검사
   * 
   * @param fileName 파일명
   * @return 이미지 파일이면 true
   */
  public static boolean isImageFile(String fileName) {
    if (isEmpty(fileName)) {
      return false;
    }

    String s = fileName.toUpperCase();

    for (int i = 0; i < IMAGE_EXTS.size(); i++) {
      if (s.endsWith(IMAGE_EXTS.get(i))) {
        return true;
      }
    }

    return false;
  }

  /**
   * 모든 필드 목록 추출 재귀호출. 부모의 필드 목록까지 몽땅 추출
   * 
   * @param currentClass 클랙스
   * @param fields       필드 목록. 리턴값
   * @since 20200821 init
   */
  public static void bindFieldsUpTo(Class<?> currentClass, List<Field> fields) {

    if (null == currentClass) {
      return;
    }

    List<Field> list = Arrays.asList(currentClass.getDeclaredFields());
    if (isEmpty(list)) {
      return;
    }

    //
    fields.addAll(list);

    //
    Class<?> parentClass = currentClass.getSuperclass();
    if (null != parentClass) {
      bindFieldsUpTo(parentClass, fields);
    }
  }

  /**
   * @see String.join(String, Object[])
   * 
   * @param deli    구분자
   * @param objects 값
   */
  @Deprecated(since = "1.22.0908")
  public static String join(String deli, Object... objects) {
    if (isEmpty(deli) || isEmpty(objects)) {
      return "";
    }

    String s = "";

    for (Object o : objects) {
      s += (isEmpty(s) ? "" : deli);

      if (String.class == o.getClass()) {
        s += o;
        continue;
      }

      if (Integer.class == o.getClass() || Long.class == o.getClass()) {
        s += ("" + o);
        continue;
      }

      // TODO map

      // TODO list

      throw new RuntimeException("not impl " + o.getClass());
    }

    return s;
  }

  /**
   * HttpRequest 인스턴스 생성하기
   * 
   * @param uri 요청 uri
   * @return
   * @throws UnsupportedEncodingException
   */
  @Deprecated(since = "20240100", forRemoval = true)
  public static HttpRequest createHttpRequest(String uri) throws UnsupportedEncodingException {
    return createGetHttpRequest(uri, null, null);
  }

  /**
   * HttpRequest 인스턴스 생성하기
   * 
   * @param uri   요청 uri
   * @param param 파라미터 맵
   * @return
   * @throws UnsupportedEncodingException
   */
  @Deprecated(since = "20240100", forRemoval = true)
  public static HttpRequest createHttpRequest(String uri, Map<String, Object> param)
      throws UnsupportedEncodingException {
    return createGetHttpRequest(uri, param, null);
  }

  /**
   * HttpRequest 인스턴스 생성하기
   * 
   * @param uri    uri
   * @param param  파라미터 맵. 옵션
   * @param header 헤더 맵. 옵션
   * @return
   * @throws UnsupportedEncodingException
   */
  @Deprecated(since = "20240100", forRemoval = true)
  public static HttpRequest createGetHttpRequest(String uri, Map<String, Object> param, Map<String, Object> header)
      throws UnsupportedEncodingException {
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().GET();

    String s = uri;

    // uri
    if (null != param && !param.isEmpty()) {
      if (!s.contains("?")) {
        s += "?_=" + System.currentTimeMillis();
      }

      for (String key : param.keySet()) {
        s += "&" + (key + "=" + URLEncoder.encode(param.get(key).toString(), "UTF-8"));
      }
    }
    requestBuilder.uri(URI.create(s));

    // header
    if (null != header && !header.isEmpty()) {
      for (String key : header.keySet()) {
        requestBuilder.header(key, header.get(key).toString());
      }
    }

    return requestBuilder.build();
  }

  /**
   * HttpRequest 인스턴스 생성하기
   * 
   * @param uri
   * @param param
   * @param header
   * @return
   * @throws JsonProcessingException
   */
  @Deprecated(since = "20240100", forRemoval = true)
  public static HttpRequest createPostHttpRequest(String uri, Object param, Map<String, Object> header)
      throws JsonProcessingException {
    //
    String requestBody = new ObjectMapper().writeValueAsString(param);
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().POST(BodyPublishers.ofString(requestBody));

    // uri
    requestBuilder.uri(URI.create(uri));

    // header
    if (null != header && !header.isEmpty()) {
      for (String key : header.keySet()) {
        requestBuilder.header(key, header.get(key).toString());
      }
    }

    return requestBuilder.build();

  }

  /**
   * HttpRequest 인스턴스 생성하기
   * 
   * @param uri
   * @param param
   * @param header
   * @return
   * @throws JsonProcessingException
   */
  @Deprecated(since = "20240100", forRemoval = true)
  public static HttpRequest createPutHttpRequest(String uri, Object param, Map<String, Object> header)
      throws JsonProcessingException {
    //
    String requestBody = new ObjectMapper().writeValueAsString(param);
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().PUT(BodyPublishers.ofString(requestBody));

    // uri
    requestBuilder.uri(URI.create(uri));

    // header
    if (null != header && !header.isEmpty()) {
      for (String key : header.keySet()) {
        requestBuilder.header(key, header.get(key).toString());
      }
    }

    return requestBuilder.build();

  }

  /**
   * 이미지 크기 변경. 가로/세로 비율로 이미지 크기 변경
   * 
   * @param srcFile  원본 파일
   * @param destFile 결과를 저장할 대상 파일
   * @param ratio    비율. 1보다 크면 확대, 1보다 작으면 축소
   */
  public static void resizeImage(File srcFile, File destFile, double ratio) throws IOException {
    Thumbnails.of(srcFile).scale(ratio).toFile(destFile);
  }

  /**
   * 이미지 크기 변경. 가로/세로 고정된 값으로 이미지 크기 변경
   * 
   * @param srcFile  원본 파일
   * @param destFile 결과를 저장할 대상 파일
   * @param width    넓이
   * @param height   높이
   */
  public static void resizeImage(File srcFile, File destFile, int width, int height) throws IOException {
    Thumbnails.of(srcFile).size(width, height).toFile(destFile);
  }

  /**
   * @see requestByGet(HttpClient, String)
   * 
   * @param uri uri
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "1.22.0908")
  public static String requestByGet(String uri) throws IOException, InterruptedException {
    return requestByGet(HttpClient.newHttpClient(), createGetHttpRequest(uri, null, null));
  }

  /**
   * @see requestByGet(HttpClient, String, Map)
   * 
   * @param uri   uri
   * @param param 파라미터 맵
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "1.22.0908")
  public static String requestByGet(String uri, Map<String, Object> param) throws IOException, InterruptedException {
    return requestByGet(HttpClient.newHttpClient(), createGetHttpRequest(uri, param, null));
  }

  /**
   * @see requestByGet(HttpClient, String, Map, Map)
   * 
   * @param uri    요청 uri
   * @param param  파라미터 맵. 옵션
   * @param header 헤더 맵. 옵션
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "1.22.0908")
  public static String requestByGet(String uri, Map<String, Object> param, Map<String, Object> header)
      throws IOException, InterruptedException {
    return requestByGet(HttpClient.newHttpClient(), createGetHttpRequest(uri, param, header));
  }

  /**
   * @see requestByGet(HttpClient, HttpRequest)
   * @deprecated 이 메소드에서 HttpClient를 직접 생성&사용하는 경우 os의 file open관련 이슈 발생 가능성 존재
   * @param request
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "1.22.0908")
  public static String requestByGet(HttpRequest request) throws IOException, InterruptedException {
    return requestByGet(HttpClient.newHttpClient(), request);
  }

  /**
   * get방식 요청
   * 
   * @param client
   * @param uri
   * @return
   * @throws UnsupportedEncodingException
   * @throws IOException
   * @throws InterruptedException
   */
  public static String requestByGet(HttpClient client, String uri)
      throws UnsupportedEncodingException, IOException, InterruptedException {
    return requestByGet(client, createGetHttpRequest(uri, Map.of(), Map.of()));
  }

  /**
   * get방식 요청
   * 
   * @param client
   * @param uri
   * @param param
   * @return
   * @throws UnsupportedEncodingException
   * @throws IOException
   * @throws InterruptedException
   */
  public static String requestByGet(HttpClient client, String uri, Map<String, Object> param)
      throws UnsupportedEncodingException, IOException, InterruptedException {
    return requestByGet(client, createGetHttpRequest(uri, param, Map.of()));
  }

  /**
   * get방식 요청
   * 
   * @param client
   * @param uri
   * @param param
   * @param header
   * @return
   * @throws UnsupportedEncodingException
   * @throws IOException
   * @throws InterruptedException
   */
  public static String requestByGet(HttpClient client, String uri, Map<String, Object> param,
      Map<String, Object> header)
      throws UnsupportedEncodingException, IOException, InterruptedException {
    return requestByGet(client, createGetHttpRequest(uri, param, header));
  }

  /**
   * get 방식 요청
   * 
   * @param client
   * @param request
   * @return
   * @throws UnsupportedEncodingException
   * @throws IOException
   * @throws InterruptedException
   */
  public static String requestByGet(HttpClient client, HttpRequest request)
      throws UnsupportedEncodingException, IOException, InterruptedException {
    return client.send(request, HttpResponse.BodyHandlers.ofString()).body().toString();
  }

  /**
   * @see requestByPost(HttpClient, String, Object)
   *      header에 기본값으로 Content-Type:application/json 추가됨
   * 
   * @param uri
   * @param param
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "1.22.0908")
  public static String requestByPost(String uri, Object param) throws IOException, InterruptedException {
    return requestByPost(HttpClient.newHttpClient(), uri, param, Map.of("Content-Type", "application/json"));
  }

  /**
   * @see requestByPost(HttpClient, String, Object, Map)
   * @param uri
   * @param param
   * @param header
   * @return
   * @throws JsonProcessingException
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "1.22.0908")
  public static String requestByPost(String uri, Object param, Map<String, Object> header)
      throws JsonProcessingException, IOException, InterruptedException {
    return requestByPost(HttpClient.newHttpClient(), createPostHttpRequest(uri, param, header));
  }

  /**
   * @see requestByPost(HttpClient, HttpRequest)
   * @deprecated 이 메소드에서 HttpClient를 직접 생성&사용하는 경우 os의 file open관련 이슈 발생 가능성 존재
   * @param request
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "1.22.0908")
  public static String requestByPost(HttpRequest request) throws IOException, InterruptedException {
    return requestByPost(HttpClient.newHttpClient(), request);
  }

  /**
   * post방식 요청
   * 
   * @param client
   * @param uri
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  public static String requestByPost(HttpClient client, String uri) throws IOException, InterruptedException {
    return requestByPost(client, createPostHttpRequest(uri, Map.of(), Map.of()));
  }

  /**
   * post방식 요청
   * 
   * @param client
   * @param uri
   * @param param
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  public static String requestByPost(HttpClient client, String uri, Object param)
      throws IOException, InterruptedException {
    return requestByPost(client, createPostHttpRequest(uri, param, Map.of()));
  }

  /**
   * post방식 요청
   * 
   * @param client
   * @param uri
   * @param param
   * @param header
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  public static String requestByPost(HttpClient client, String uri, Object param, Map<String, Object> header)
      throws IOException, InterruptedException {
    return requestByPost(client, createPostHttpRequest(uri, param, header));
  }

  /**
   * post 요청
   * 
   * @param client
   * @param request
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  public static String requestByPost(HttpClient client, HttpRequest request) throws IOException, InterruptedException {
    return client.send(request, HttpResponse.BodyHandlers.ofString()).body().toString();
  }

  /**
   * @see requestByPut(HttpClient, String)
   * 
   * @param uri uri
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "1.22.0908")
  public static String requestByPut(String uri) throws IOException, InterruptedException {
    return requestByPut(uri, Map.of("dummy", "dummy"), Map.of("Content-Type", "application/json"));
  }

  /**
   * @see requestByPut(HttpClient, String, Object)
   * 
   * @param uri   uri
   * @param param 파라미터
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "1.22.0908")
  public static String requestByPut(String uri, Object param) throws IOException, InterruptedException {
    return requestByPut(uri, param, Map.of("Content-Type", "application/json"));
  }

  /**
   * @see requestByPut(HttpClient, String, Object, Map)
   * 
   * @param uri    uri
   * @param param  파라미터
   * @param header 헤더 맵
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "1.22.0908")
  public static String requestByPut(String uri, Object param, Map<String, Object> header)
      throws IOException, InterruptedException {
    return requestByPut(createPutHttpRequest(uri, param, header));
  }

  /**
   * @see requestByPut(HttpClient, HttpRequest)
   * 
   * @param request
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "1.22.0908")
  public static String requestByPut(HttpRequest request) throws IOException, InterruptedException {
    HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    return response.body().toString();
  }

  /**
   * put방식 요청
   * 
   * @param client
   * @param uri
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "20240100", forRemoval = true)
  public static String requestByPut(HttpClient client, String uri) throws IOException, InterruptedException {
    return requestByPut(client, createPutHttpRequest(uri, Map.of(), Map.of()));
  }

  /**
   * put방식 요청
   * 
   * @param client
   * @param uri
   * @param param
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "20240100", forRemoval = true)
  public static String requestByPut(HttpClient client, String uri, Object param)
      throws IOException, InterruptedException {
    return requestByPut(client, createPutHttpRequest(uri, param, Map.of()));
  }

  /**
   * put방식 요청
   * 
   * @param client
   * @param uri
   * @param param
   * @param header
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "20240100", forRemoval = true)
  public static String requestByPut(HttpClient client, String uri, Object param, Map<String, Object> header)
      throws IOException, InterruptedException {
    return requestByPut(client, createPutHttpRequest(uri, param, header));
  }

  /**
   * put방식 요청
   * 
   * @param client
   * @param request
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @Deprecated(since = "20240100", forRemoval = true)
  public static String requestByPut(HttpClient client, HttpRequest request) throws IOException, InterruptedException {
    return client.send(request, HttpResponse.BodyHandlers.ofString()).body().toString();
  }

}
