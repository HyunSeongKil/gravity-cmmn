package dev.hyunlab.gravity.cmmn.misc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * bean 관련 유틸
 */
@Slf4j
public class GcBeanUtils {

  /**
   * 동적으로 인스턴스 생성
   * 
   * @param <T>
   * @param t
   * @return
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   * @throws SecurityException
   */
  public static <T> T newInstance(Class<T> t) throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    return (T) t.getConstructor().newInstance();
  }

  /**
   * @see copyProperties(Object, Class<T>, Map<String, Object>, List<String>)
   * @param <T>
   * @param srcObj
   * @param destClass
   * @return
   */
  public static <T> T copyProperties(Object srcObj, Class<T> destClass) {
    return copyProperties(srcObj, destClass, Map.of(), List.of());
  }

  /**
   * map의 값을 class로 복사
   * ! destClass의 fieldName과 srcMap의 key가 같아야 함
   * 
   * @param <T>
   * @param srcMap           소스 맵
   * @param destClass        신규로 생성할 클래스
   * @param defaultValueMap  필드의 값이 null일 경우 대체할 값 목록 key:필드명, value:대체할 값
   * @param exceptFieldNames 복사하지 않을 필드 목록
   * @return 생성된 클래스 인스턴스
   */
  public static <T> T copyProperties(Map<String, Object> srcMap, Class<T> destClass,
      Map<String, Object> defaultValueMap,
      List<String> exceptFieldNames) {
    Function<String, Boolean> isExceptField = (fieldName) -> {
      if (exceptFieldNames == null) {
        return false;
      }

      return exceptFieldNames
          .stream()
          .filter(x -> x.equals(fieldName))
          .count() > 0;
    };

    Function<String, Boolean> mapContainsKey = (fieldName) -> {
      return (srcMap.containsKey(GcUtils.camelToSnake(fieldName)) ||
          srcMap.containsKey(GcUtils.camelToKebab(fieldName)) ||
          srcMap.containsKey(fieldName));
    };

    Function<String, Object> getMapValue = (fieldName) -> {
      if (srcMap.containsKey(GcUtils.camelToSnake(fieldName))) {
        return srcMap.get(GcUtils.camelToSnake(fieldName));
      }

      if (srcMap.containsKey(GcUtils.camelToKebab(fieldName))) {
        return srcMap.get(GcUtils.camelToKebab(fieldName));
      }

      if (srcMap.containsKey(fieldName)) {
        return srcMap.get(fieldName);
      }

      return null;
    };
    ////

    try {
      T destObj = newInstance(destClass);

      if (srcMap == null || srcMap.isEmpty()) {
        return destObj;
      }

      if (destClass == null) {
        return destObj;
      }

      Field[] destFields = destObj.getClass().getDeclaredFields();

      for (int i = 0; i < destFields.length; i++) {
        Field f = destFields[i];

        if (f == null) {
          continue;
        }

        String fieldName = f.getName();

        if (isExceptField.apply(fieldName)) {
          continue;
        }

        // default value를 먼저 적용
        if (defaultValueMap.containsKey(fieldName)) {
          f.setAccessible(true);
          f.set(destObj, defaultValueMap.get(fieldName));
          continue;
        }

        if (mapContainsKey.apply(fieldName)) {
          Object value = getMapValue.apply(fieldName);

          if (value != null) {
            // TODO f의 타입과 value 타입이 다를 경우 변환하여 적용
            f.setAccessible(true);
            f.set(destObj, value);
            continue;
          }
        }

      }

      return destObj;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * source의 값을 targetClass로 복사
   * 
   * @param <T>
   * @param source           소스 오브젝트
   * @param targetClass      신규로 생성할 클래스
   * @param defaultValueMap  필드의 값이 null일 경우 대체할 값 목록 key:필드명, value:대체할 값
   * @param exceptFieldNames 처리에서 제외할 필드 명 목록
   * @return
   */
  public static <T> T copyProperties(Object source, Class<T> targetClass, Map<String, Object> defaultValueMap,
      List<String> exceptFieldNames) {
    BiFunction<Field[], String, Field> getField = (fields, fieldName) -> {
      for (int i = 0; i < fields.length; i++) {
        Field f = fields[i];
        f.setAccessible(true);

        if (f.getName().equals(fieldName)) {
          return f;
        }
      }

      return null;
    };

    Function<String, Boolean> isExceptField = (fieldName) -> {
      if (exceptFieldNames == null) {
        return false;
      }

      return exceptFieldNames.contains(fieldName);
    };
    ////

    try {
      T targetObj = newInstance(targetClass);

      Field[] sourceFields = source.getClass().getDeclaredFields();
      Field[] targetFields = targetObj.getClass().getDeclaredFields();

      for (int i = 0; i < targetFields.length; i++) {
        Field f = targetFields[i];

        if (f == null) {
          continue;
        }

        if (isExceptField.apply(f.getName())) {
          continue;
        }

        f.setAccessible(true);

        Field sourceField = getField.apply(sourceFields, f.getName());

        if (sourceField == null) {
          continue;
        }
        if (sourceField.get(source) == null && defaultValueMap.get(f.getName()) == null) {
          continue;
        }

        // default value를 먼저 적용
        if (defaultValueMap.get(f.getName()) != null) {
          f.set(targetObj, defaultValueMap.get(f.getName()));
          continue;
        }

        if (sourceField.get(source) != null) {
          f.set(targetObj, sourceField.get(source));
        }
      }

      return targetObj;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * entity 인스턴스 생성 & entity 필드의 값을 dto의 값으로 설정. 단, dto필드명과 entity필드명이 같아야지만 값 설정
   * 가능함
   * 
   * @see fromDtoToEntity(DTO, Class<ENTITY>, List<String>)
   * @param <DTO>
   * @param <ENTITY>
   * @param dtoObj      dto 인스턴스
   * @param entityClass entity 클래스
   * @return 생성된 entity 인스턴스
   * @throws Exception
   */
  @Deprecated(since = "2024-01-00", forRemoval = true)
  public static <DTO, ENTITY> ENTITY dtoToEntity(DTO dtoObj, Class<ENTITY> entityClass) throws Exception {
    return dtoToEntity(dtoObj, entityClass, List.of());
  }

  /**
   * entity 인스턴스 생성 & entity 필드의 값을 dto의 값으로 설정. 단, dto필드명과 entity필드명이 같아야지만 값 설정
   * 가능함
   * 
   * 예) CmmnBeanUtils.<UserDto, UserEntity>fromDtoToEntity(userDto,
   * UserEntity.class)
   * 
   * @param <DTO>
   * @param <ENTITY>
   * @param dtoObj           dto 인스턴스
   * @param entityClass      entity 클래스
   * @param exceptFieldNames 설정하지 않을 필드명 목록
   * @return 생성된 entity 인스턴스
   * @throws Exception
   */
  @Deprecated(since = "2024-01-00", forRemoval = true)
  public static <DTO, ENTITY> ENTITY dtoToEntity(DTO dtoObj, Class<ENTITY> entityClass, List<String> exceptFieldNames)
      throws Exception {
    if (null == dtoObj || null == entityClass || null == exceptFieldNames) {
      throw new RuntimeException("null paramter exists");
    }

    //
    ENTITY entityObj = newInstance(entityClass);

    Field[] dtoFields = dtoObj.getClass().getDeclaredFields();

    getFields(entityObj).forEach(f -> {
      String entityFieldName = f.getName();

      if (!existsField(dtoFields, entityFieldName)) {
        return;
      }

      if (exceptFieldNames.contains(entityFieldName)) {
        return;
      }

      try {
        // entity에 값 설정
        setFieldValue(entityObj, entityFieldName, getFieldValue(dtoObj, entityFieldName));
      } catch (IllegalArgumentException | IllegalAccessException e) {
        log.debug("{}", e);
      }
    });

    return entityObj;
  }

  /**
   * from의 필드 값을 to로 복사
   * 
   * @param froms
   * @param tos
   * @param fieldNames
   */
  public static void copyFieldValue(List<?> froms, List<?> tos, String... fieldNames) {
    if (GcUtils.isEmpty(froms) || GcUtils.isEmpty((tos)) || GcUtils.isEmpty(fieldNames)) {
      return;
    }

    try {
      for (int j = 0; j < fieldNames.length; j++) {
        String fieldName = fieldNames[j];

        for (int i = 0; i < froms.size(); i++) {
          Object v = GcBeanUtils.getFieldValue(froms.get(i), fieldName);
          GcBeanUtils.setFieldValue(tos.get(i), fieldName, v);
        }
      }
    } catch (Exception e) {
      log.error("{}", e);
    }
  }

  /**
   * 필드 리턴 바로 위 부모 필드까지 검색
   * 
   * @param obj       인스턴스
   * @param fieldName 필드명
   * @return
   */
  public static Field getField(Object obj, String fieldName) {
    // 현재 인스턴스
    Field[] fields = obj.getClass().getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      Field f = fields[i];

      if (f.getName().equals(fieldName)) {
        return f;
      }
    }

    // 부모
    fields = obj.getClass().getSuperclass().getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      Field f = fields[i];

      if (f.getName().equals(fieldName)) {
        return f;
      }
    }

    return null;
  }

  /**
   * fields에 targetFieldName이 존재하는지 여부
   * 
   * @param fields          필드 목록
   * @param targetFieldName 존재여부를 판단할 필드 명
   * @return
   */
  public static boolean existsField(Field[] fields, String targetFieldName) {
    for (int i = 0; i < fields.length; i++) {
      Field f = fields[i];

      if (f.getName().equals(targetFieldName)) {
        return true;
      }
    }

    return false;
  }

  /**
   * clz에 fieldName이 존재하는지 여부 바로 위 부모 클래스까지 검사
   * 
   * @param clz       클래스
   * @param fieldName 존재여부를 판단할 필드명
   * @return
   */
  public static boolean existsField(Class<?> clz, String fieldName) {
    Field[] fields = clz.getDeclaredFields();
    if (GcUtils.isEmpty(fields)) {
      return false;
    }

    boolean b = existsField(fields, fieldName);
    if (b) {
      return b;
    }

    // 부모
    b = existsField(clz.getSuperclass().getDeclaredFields(), fieldName);

    return b;
  }

  /**
   * 필드 목록 리턴. 1단계 위 부모 필드 목록도 포함
   * 
   * @param obj 엔티티
   * @return fieldName목록. 오류발생|field가 없으면 빈 목록 리턴
   * @since 20200811 init
   */
  public static Set<String> getFieldNames(Object obj) {
    Set<String> names = new HashSet<>();

    if (null == obj) {
      return names;
    }

    //
    Field[] fields = obj.getClass().getDeclaredFields();
    Arrays.asList(fields).forEach(f -> {
      names.add(f.getName());
    });

    // 부모
    fields = obj.getClass().getSuperclass().getDeclaredFields();
    Arrays.asList(fields).forEach(f -> {
      names.add(f.getName());
    });

    return names;
  }

  /**
   * reflection이용. domain의 fieldName의 값을 value로 설정 field가 없거나 오류 발생하면 아무런값도 set하지
   * 않음
   * 
   * @param obj       엔티티
   * @param fieldName 필드명
   * @param value     값
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @since 20200811 init
   */
  public static void setFieldValue(Object obj, String fieldName, Object value)
      throws IllegalArgumentException, IllegalAccessException {
    Field f = getField(obj, fieldName);
    if (null == f) {
      return;
    }

    f.setAccessible(true);
    f.set(obj, value);

  }

  /**
   * reflection이용. domain의 fieldName의 값 추출
   * 
   * @param obj       엔티티
   * @param fieldName 필드명
   * @return field의 값. 필드없거나 오류 발생하면 null 리턴
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @since 20200811 init
   */
  public static Object getFieldValue(Object obj, String fieldName)
      throws IllegalArgumentException, IllegalAccessException {
    boolean b = existsField(obj.getClass(), fieldName);
    if (!b) {
      return null;
    }

    Field f = getField(obj, fieldName);
    f.setAccessible(true);
    return f.get(obj);
  }

  /**
   * 필드 목록 리턴
   * 
   * @param clz 클래스
   * @return
   */
  public static List<Field> getFields(Class clz) {
    List<Field> list = new ArrayList<>();

    if (null == clz) {
      return list;
    }

    Field[] fields = clz.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      Field f = fields[i];

      list.add(f);
    }

    return list;
  }

  /**
   * 필드 목록 리턴. 바로 위 클래스의 필드까지 리턴
   * 
   * @param obj
   * @return
   */
  public static List<Field> getFields(Object obj) {
    List<Field> list = new ArrayList<>();

    if (null == obj) {
      return list;
    }

    list.addAll(getFields(obj.getClass()));
    // 부모
    list.addAll(getFields(obj.getClass().getSuperclass()));

    return list;
  }

  /**
   * fields에 f가 존재하는 여부. 필드명으로만 검사
   * 
   * @param f      찾으려는 필드
   * @param fields 대상 필드 목록
   * @return 존재하면 true
   */
  public static boolean existsField(Field f, List<Field> fields) {
    if (null == f || null == fields) {
      return false;
    }

    for (Field field : fields) {
      if (f.getName().equals(field.getName())) {
        return true;
      }
    }

    return false;
  }

  /**
   * basePackage하위의 전체 클래스 목록 조회
   * 
   * @param basePackage 시작 패키지명
   * @return 클래스 목록
   */
  public static List<Class<?>> getAllClasses(String basePackage) {
    List<Class<?>> classes = new ArrayList<>();

    new Reflections(basePackage, new SubTypesScanner(false)).getAllTypes().forEach(x -> {
      try {
        classes.add(Class.forName(x));
      } catch (ClassNotFoundException e) {
        log.error("{}", e);
      }
    });

    return classes;

  }

  /**
   * srcMap의 값을 destClass로 복사
   * map -> class key/value는 destClass의 field명을 기준으로 함. srcMap의 key와 destClass의
   * field명이 동일해야 함
   * 
   * @param <T>
   * @param srcMap
   * @param destClass
   * @return
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  @Deprecated(since = "2024-01-00", forRemoval = true)
  public static <T> T copyMapToObj(Map<String, Object> srcMap, Class<T> destClass)
      throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    Map<String, String> mappingMap = new HashMap<>();

    Field[] fields = destClass.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      mappingMap.put(fields[i].getName(), fields[i].getName());
    }

    return copyMapToObj(srcMap, destClass, mappingMap);
  }

  /**
   * sourceMap의 값을 targetClass로 복사
   * 
   * @param <T>
   * @param srcMap
   * @param destClass
   * @param mappingMap key:sourceMap의 키, value:targetClass의 필드명
   * @return
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  @Deprecated(since = "2024-01-00", forRemoval = true)
  public static <T> T copyMapToObj(Map<String, Object> srcMap, Class<T> destClass,
      Map<String, String> mappingMap)
      throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

    class InnerClass {
      boolean containsKey(Map<String, Object> fromMap, String key) {
        return fromMap.containsKey(key);
      }

      boolean containsField(T destObj, String fieldName) {
        Field f = null;
        try {
          f = destObj.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException | SecurityException e) {
          return false;
        }

        return null != f;
      }

      void setFieldValue(T destObj, String fieldName, Object value)
          throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = getField(destObj, fieldName);
        if (field == null) {
          return;
        }

        field.setAccessible(true);
        Class<?> fieldType = field.getType();
        Class<?> valueType = value.getClass();

        // 값 타입이 integer이고 필드 타입이 long이면 integer를 long으로 변환하여 저장
        if (fieldType == Long.class && valueType == Integer.class) {
          GcBeanUtils.setFieldValue(destObj, fieldName, Long.parseLong(value.toString()));
          return;
        }

        // System.out.println(fieldName + "\t" + fieldType + "\t" + valueType);
        GcBeanUtils.setFieldValue(destObj, fieldName, value);

      }
    }
    ;//

    if (null == srcMap || srcMap.isEmpty()) {
      return null;
    }

    T destObj = null;
    try {
      destObj = newInstance(destClass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    if (null == destObj) {
      return null;
    }

    InnerClass ic = new InnerClass();
    Iterator<Entry<String, String>> iter = mappingMap.entrySet().iterator();
    while (iter.hasNext()) {
      Entry<String, String> entry = iter.next();

      if (!ic.containsKey(srcMap, entry.getKey())) {
        continue;
      }

      if (!ic.containsField(destObj, entry.getValue())) {
        continue;
      }

      ic.setFieldValue(destObj, entry.getValue(), srcMap.get(entry.getKey()));
    }

    return destObj;
  }

  /**
   * object를 map으로 복사
   * 
   * @param obj
   * @return
   */
  @Deprecated(since = "2024-01-00", forRemoval = true)
  public static Map<String, Object> copyObjectToMap(Object obj) {
    Map<String, Object> map = new HashMap<>();

    Field[] fields = obj.getClass().getDeclaredFields();

    for (Field f : fields) {
      try {
        f.setAccessible(true);

        map.put(f.getName(), f.get(obj));

      } catch (IllegalArgumentException | IllegalAccessException e) {
        log.error("{}", e);
      }
    }

    return map;
  }

  public static String toJsonString(Object obj) throws JsonProcessingException {
    if (GcUtils.isNull(obj)) {
      return "";
    }

    return new ObjectMapper().writeValueAsString(obj);

  }
}
