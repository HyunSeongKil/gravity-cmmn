# gravity-java

## 요약

- 목적 : 모든 자바단에 공통으로 사용될 기능을 라이브러리 형태로 제공
- (최초)작성자 : gravity@vaiv.kr
- (최초)작성일자 : 2021-04-06
- 라이선스 : MIT

## 사용기술

- spring boot

## 사용 방법

- 업무 Entity, Dto가 없는 경우

  - 공통에서 제공하는 CmmnEntity, CmmnDto를 이용하여 Service단 구현

  ```
  public interface HelloService extends CmmnService<CmmnEntity, CmmnDto, Long>{
  }
  ```

- 업무 Entity, Dto가 없는 경우 Repository, Mapper파일도 없을 것임. ServiceImpl단은 아래처럼 사용
  ```
  public class HelloServiceImpl extends CmmnServiceImpl<JpaResitory, CmmnMapper, CmmnEntity, CmmnDto, Long> implements HelloService{
  }
  ```
