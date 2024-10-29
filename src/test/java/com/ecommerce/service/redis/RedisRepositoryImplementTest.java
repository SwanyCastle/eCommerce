package com.ecommerce.service.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ecommerce.exception.DataBaseException;
import com.ecommerce.repository.redis.RedisRepositoryImplement;
import com.ecommerce.type.ResponseCode;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisRepositoryImplementTest {

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private ValueOperations<String, Object> valueOperations;

  @InjectMocks
  private RedisRepositoryImplement redisServiceImplement;

  @Test
  @DisplayName("Redis 데이터 저장 - 성공")
  void testSetData_Success() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    // when
    redisServiceImplement.setData(
        "testKey", "testValue", 1L, TimeUnit.MINUTES
    );

    // then
    verify(redisTemplate.opsForValue(), times(1))
        .set("testKey", "testValue", 1L, TimeUnit.MINUTES);
  }

  @Test
  @DisplayName("Redis 데이터 저장 - 실패 (Redis Server Error)")
  void testSetData_Fail_RedisServerError() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    doThrow(new RuntimeException("Redis 서버 오류")).when(valueOperations)
        .set("testKey", "testValue", 1L, TimeUnit.MINUTES);

    // when
    DataBaseException dataBaseException = assertThrows(DataBaseException.class,
        () -> redisServiceImplement.setData(
            "testKey", "testValue", 1L, TimeUnit.MINUTES
        ));

    // then
    verify(redisTemplate.opsForValue(), times(1))
        .set("testKey", "testValue", 1L, TimeUnit.MINUTES);

    assertThat(dataBaseException.getErrorCode()).isEqualTo(ResponseCode.DATABASE_ERROR);
  }

  @Test
  @DisplayName("Redis 데이터 조회 - 성공")
  void testGetData_Success() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(redisTemplate.opsForValue().get("testKey")).willReturn("testValue");

    // when
    Object data = redisServiceImplement.getData("testKey");

    // then
    verify(redisTemplate.opsForValue(), times(1)).get("testKey");

    assertThat(data).isEqualTo("testValue");
  }

  @Test
  @DisplayName("Redis 데이터 조회 - 실패 (Redis Server Error)")
  void testGetData_Fail_RedisServerError() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    doThrow(new RuntimeException("Redis 서버 오류")).when(valueOperations).get("testKey");

    // when
    DataBaseException dataBaseException = assertThrows(DataBaseException.class,
        () -> redisServiceImplement.getData("testKey"));

    // then
    verify(redisTemplate.opsForValue(), times(1)).get("testKey");

    assertThat(dataBaseException.getErrorCode()).isEqualTo(ResponseCode.DATABASE_ERROR);
  }

  @Test
  @DisplayName("Redis 데이터 삭제 - 성공")
  void testDeleteData_Success() {
    // given
    // when
    redisServiceImplement.deleteData("testKey");

    // then
    verify(redisTemplate, times(1)).delete("testKey");
  }

  @Test
  @DisplayName("Redis 데이터 삭제 - 실패 (Redis Server Error)")
  void testDeleteData_Fail_RedisServerError() {
    // given
    doThrow(new RuntimeException("Redis 서버 오류")).when(redisTemplate).delete("testKey");

    // when
    DataBaseException dataBaseException = assertThrows(DataBaseException.class,
        () -> redisServiceImplement.deleteData("testKey"));

    // then
    verify(redisTemplate, times(1)).delete("testKey");

    assertThat(dataBaseException.getErrorCode()).isEqualTo(ResponseCode.DATABASE_ERROR);
  }

}