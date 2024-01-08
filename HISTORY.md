## v1.24.0108

- uuid(), uuid4(), uuid8(), uuid12() 첫 char가 항상 문자가 되도록 수정

## v1.24.0104

- copyProperties(Object, Class, Map, List) 결함 수정

## v1.24.0103

- zip(Path, Path) 추가
- zip(Path, Path, String) 추가

## v3.23.1107

- JDK 17 지원
- Spring Boot 3.x이상 지원
- JJWT 버전 업

## v1.23.0814

- CmmnConst.MIN_1, MIN_10, MIN_30 결함 수정
- CmmnConst.HOUR_1 => MIN_60

## v1.23.0707

- CmmnFileDownloadView의 package 변경
  - misc.CmmnFileDownloadView => view.CmmnFileDownloadView
- refactoring CmmnFileDownloadView

## v1.23.0529

- CmmnUtils.deleteOldPath(Path, long) 추가

## v1.23.0430

- CmmnUtils.camelToSnake(String) 추가
- CmmnUtils.snakeToCamel(String) 추가

## v1.23.0416

- AtchmnflHandler.createMultipartEntityBuilder()에 chartset 설정 추가

## v1.23.0414

- CmmnResultMap.empty() 추가

## v1.23.0310

- MIN_1, MIN_10, MIN_30, HOUR_1 추가

## 2023-03-07 v1.23.0307

- CmmnResultMap.toJsonString() 메소드 추가

## 2023-03-03 v1.23.0303

- 파일 추가
  - AtchmnflGroupDto

## 2023-02-27 v1.23.0227

- 결함 수정
  - AtchmnflHandler.getsByAtchmnflGroupId()

## 2023-01-28 v1.23.0128

- 추가
  - AtchmnflHandler.getFilesByAtchmnflGroupId(String)

## 2023-01-20 v1.23.0120

- 추가
  - CmmnBeanUtils.copyMapToObj(Map, Class) Map의 값을 Object로 복사
  - CmmnUtils.resizeImage(File, File, long) 이미지 크기 변경
  - CmmnUtils.resizeImage(File, File, int, int) 이미지 크기 변경
  - uuid8()

## 2023-01-12 v1.23.0112

- 추가
  - CmmnUtils.createRandom8String() 8자리 랜덤 문자열 생성
  - CmmnUtils.createRandom8String(String) 8자리 랜덤 문자열 생성
  - CmmnUtils.getExtension(File) 파일의 확장자 추출(점(.) 불포함)
  - CmmnUtils.getExtension(String) 파일의 확장자 추출(점(.) 불포함)
  - CmmnBeanUtils.entityToDto()의 Exception을 RuntimeException()으로 변경

## 2023-01-03 v1.23.0103

- 추가
  - CmmnDurationInterceptor 소요시간 측정용 인터셉터
  - CmmnJwtUtils jwt관련 처리 유틸리티
  - CmmnJwtUtilsTest 테스트 파일

## 2022-09-08 v1.22.0908

- 주요 내용 : rest 요청시 os의 file open 이슈 해결을 위한 수정
- 추가
  - CmmnUtils.requestByGet(HttpClient, String)
  - CmmnUtils.requestByGet(HttpClient, String, Map)
  - CmmnUtils.requestByGet(HttpClient, String, Map, Map)
  - CmmnUtils.requestByGet(HttpClient, HttpRequest)
  - CmmnUtils.requestByPost(HttpClient, String)
  - CmmnUtils.requestByPost(HttpClient, String, Object)
  - CmmnUtils.requestByPost(HttpClient, String, Object, Map)
  - CmmnUtils.requestByPost(HttpClient, HttpRequest)
  - CmmnUtils.requestByPut(HttpClient, String)
  - CmmnUtils.requestByPut(HttpClient, String, Object)
  - CmmnUtils.requestByPut(HttpClient, String, Object, Map)
  - CmmnUtils.requestByPut(HttpClient, HttpRequest)
- deprecated 이유 아래 메소드들은 실행할때마다 HttpClient를 신규로 생성하여 File Open 갯수관련 이슈 발생함
  - CmmnUtils.requestByGet(String)
  - CmmnUtils.requestByGet(String, Map)
  - CmmnUtils.requestByGet(String, Map, Map)
  - CmmnUtils.requestByGet(HttpRequest)
  - CmmnUtils.requestByPost(String)
  - CmmnUtils.requestByPost(String, Object)
  - CmmnUtils.requestByPost(String, Object, Map)
  - CmmnUtils.requestByPost(HttpRequest)
  - CmmnUtils.requestByPut(String)
  - CmmnUtils.requestByPut(String, Object)
  - CmmnUtils.requestByPut(String, Object, Map)
  - CmmnUtils.requestByPut(HttpRequest)

## 2022-07-30 v1.22.0730

- 추가 (put방식 요청 메소드 추가)
  - CmmnUtils.requestByPut(String)
  - CmmnUtils.requestByPut(String, Object)
  - CmmnUtils.requestByPut(String, Object, Map)
  - CmmnUtils.requestByPut(HttpClient)

## 2022-07-29 v1.22.0729

- 변경 전 : CmmnUtils.requestByPost(String, Map)
- 변경 후 : CmmnUtils.requestByPost(String, Object)

## 2022-07-28 v1.22.0728

- 추가
  CmmnResultMap.of(Object, Pageable, Long)

## 2022-07-16 v1.22.0716

- 추가
  - createPostHttpRequest(String, Map, Map)
  - requestByPost(String, Map)
  - requestByPost(String, Map, Map)
  - requestByPost(HttpRequest)

## 2022-07-03 v1.22.0703

- 추가
  - createHttpRequest(String)
  - createHttpRequest(String, Map)
  - createHttpRequest(String, Map, Map)
  - requestByGet(String)
  - requestByGet(String, Map)
  - requestByGet(String, Map, Map)
  - requestByGet(HttpRequest)

## 2022-02-08

CmmnBeanUtils.<ENTITY, DTO>fromEntityToDto() 추가
CmmnBeanUtils.<DTO, ENTITY>fromDtoToEntity() 추가

## 2022-02-03

CmmnResultMap.withData() 추가

## 2021-04-22

- 공통 라이브러리 버전 올림
  - sdt-cmmn-1.21.0422-SNAPSHOT.jar
- 비동기 처리 메소드 추가
  - CmmnService.registAsync() : 비동기로 등록 처리
  - CmmnService.updateAsync() : 비동기로 수정 처리
  - CmmnService.deleteAsync() : 비동기로 삭제 처리
- 암호화 처리 클래스 추가
  - CmmnCrypto
  - 주요 메소드
    - sha512(String)
    - encrypt(String)
    - decrypt(String)
- user-agent 파싱 메소드 추가
  - CmmnUtils.parseUserAgent()
- 자잘한 결함 수정
