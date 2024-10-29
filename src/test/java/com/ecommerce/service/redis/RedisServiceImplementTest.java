package com.ecommerce.service.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ecommerce.exception.DataBaseException;
import com.ecommerce.exception.NotFoundException;
import com.ecommerce.repository.redis.RedisRepository;
import com.ecommerce.type.ResponseCode;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RedisServiceImplementTest {

  @Mock
  private RedisRepository redisRepository;

  @InjectMocks
  private RedisServiceImplement redisServiceImplement;

  @Test
  @DisplayName("유효시간을 포함한 데이터 저장 - 성공")
  void testSaveDataWithTTL_Success() {
    // given
    String key = "testKey";
    String value = "1234";
    long timeout = 1L;
    TimeUnit unit = TimeUnit.MINUTES;

    willDoNothing().given(redisRepository)
        .setData(eq(key), eq(value), eq(timeout), eq(unit));

    // when
    redisServiceImplement.saveDataWithTTL(key, value, timeout, unit);

    // then
    verify(redisRepository, times(1))
        .setData(eq("testKey"), eq("1234"), eq(1L), eq(TimeUnit.MINUTES));
  }

  @Test
  @DisplayName("유효시간을 포함한 데이터 저장 - 실패")
  void testSaveDataWithTTL_Fail() {
    // given
    String key = "testKey";
    String value = "1234";
    long timeout = 1L;
    TimeUnit unit = TimeUnit.MINUTES;

    doThrow(new DataBaseException(ResponseCode.DATABASE_ERROR)).when(redisRepository)
        .setData(eq(key), eq(value), eq(timeout), eq(unit));

    // when
    DataBaseException dataBaseException = assertThrows(DataBaseException.class,
        () -> redisServiceImplement.saveDataWithTTL(key, value, timeout, unit));

    // then
    verify(redisRepository, times(1))
        .setData(eq("testKey"), eq("1234"), eq(1L), eq(TimeUnit.MINUTES));

    assertThat(dataBaseException.getErrorCode()).isEqualTo(ResponseCode.DATABASE_ERROR);
  }

  @Test
  @DisplayName("인증번호 확인 - 성공")
  void testVerifyCertificationNumber_Success() {
    // given
    String key = "testUser";
    String certificationNumber = "1234";

    given(redisRepository.getData(key)).willReturn(certificationNumber);

    willDoNothing().given(redisRepository)
        .setData(eq(key + ":verified"), eq(true), eq(1L), eq(TimeUnit.HOURS));
    willDoNothing().given(redisRepository)
        .deleteData(eq(key));

    // when
    boolean isVerified = redisServiceImplement.verifyCertificationNumber(key, certificationNumber);

    // then
    verify(redisRepository, times(1)).getData("testUser");
    verify(redisRepository, times(1))
        .setData(eq("testUser:verified"), eq(true), eq(1L), eq(TimeUnit.HOURS));
    verify(redisRepository, times(1))
        .deleteData("testUser");

    assertThat(isVerified).isEqualTo(true);
  }

  @Test
  @DisplayName("인증번호 확인 - 실패 (Redis 서버 오류)")
  void testVerifyCertificationNumber_Fail_RedisServerError() {
    // given
    String key = "testUser";
    String certificationNumber = "1234";

    doThrow(new DataBaseException(ResponseCode.DATABASE_ERROR))
        .when(redisRepository).getData(key);

    // when
    DataBaseException dataBaseException = assertThrows(DataBaseException.class,
        () -> redisServiceImplement.verifyCertificationNumber(key, certificationNumber));

    // then
    verify(redisRepository, times(1)).getData("testUser");

    assertThat(dataBaseException.getErrorCode()).isEqualTo(ResponseCode.DATABASE_ERROR);
  }

  @Test
  @DisplayName("인증번호 확인 - 실패 (Not Found Data)")
  void testVerifyCertificationNumber_Fail_NotFoundData() {
    // given
    String key = "testUser";
    String certificationNumber = "1234";

    given(redisRepository.getData(key)).willReturn(null);

    // when
    NotFoundException notFoundException = assertThrows(NotFoundException.class,
        () -> redisServiceImplement.verifyCertificationNumber(key, certificationNumber));

    // then
    verify(redisRepository, times(1)).getData("testUser");

    assertThat(notFoundException.getErrorCode()).isEqualTo(ResponseCode.REDIS_DATA_NOT_FOUND);
  }

  @Test
  @DisplayName("인증번호 확인 - 실패 (Not Matched Data)")
  void testVerifyCertificationNumber_Fail_NotMatchedData() {
    // given
    String key = "testUser";
    String certificationNumber = "1234";

    given(redisRepository.getData(key)).willReturn("0000");

    // when
    boolean isVerified = redisServiceImplement.verifyCertificationNumber(key, certificationNumber);

    // then
    verify(redisRepository, times(1)).getData("testUser");

    assertThat(isVerified).isEqualTo(false);
  }

  @Test
  @DisplayName("이메일 인증 여부 확인 - 성공")
  void testCheckVerified_Success() {
    // given
    String key = "testUser:verified";

    given(redisRepository.getData(key)).willReturn(true);

    // when
    boolean idCheckVerified = redisServiceImplement.checkVerified(key);

    // then
    verify(redisRepository, times(1)).getData("testUser:verified");

    assertThat(idCheckVerified).isEqualTo(true);
  }

  @Test
  @DisplayName("이메일 인증 여부 확인 - 실패 (Redis 서버 오류)")
  void testCheckVerified_Fail_RedisServerError() {
    // given
    String key = "testUser:verified";

    doThrow(new DataBaseException(ResponseCode.DATABASE_ERROR))
        .when(redisRepository).getData(key);

    // when
    DataBaseException dataBaseException = assertThrows(DataBaseException.class,
        () -> redisServiceImplement.checkVerified(key));

    // then
    verify(redisRepository, times(1)).getData("testUser:verified");

    assertThat(dataBaseException.getErrorCode()).isEqualTo(ResponseCode.DATABASE_ERROR);
  }

  @Test
  @DisplayName("이메일 인증 여부 확인 - 실패 (이메일 인증 X)")
  void testCheckVerified_Fail_DidNotEmailCertification() {
    // given
    String key = "testUser:verified";

    given(redisRepository.getData(key)).willReturn(null);

    // when
    boolean idCheckVerified = redisServiceImplement.checkVerified(key);


    // then
    verify(redisRepository, times(1)).getData("testUser:verified");

    assertThat(idCheckVerified).isEqualTo(false);
  }
}