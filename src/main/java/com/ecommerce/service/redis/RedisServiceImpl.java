package com.ecommerce.service.redis;

import com.ecommerce.exception.DataBaseException;
import com.ecommerce.exception.RedisException;
import com.ecommerce.type.ResponseCode;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * Redis 에 key, value 데이터 저장시 유효 시간 설정해서 저장
   *
   * @param key
   * @param value
   * @param timeout
   * @param unit
   */
  @Override
  public void saveDataWithTTL(String key, Object value, long timeout, TimeUnit unit) {
    try {

      redisTemplate.opsForValue().set(key, value, timeout, unit);

    } catch (Exception e) {
      e.printStackTrace();
      throw new DataBaseException(ResponseCode.DATABASE_ERROR);
    }
  }

  /**
   * Redis 에 key 에 해당하는 데이터 조회
   *
   * @param key
   * @return Object
   */
  @Override
  public Object getData(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  /**
   * Redis 에 key 에 해당하는 데이터 삭제
   *
   * @param key
   */
  @Override
  public void deleteData(String key) {
    try {

      redisTemplate.delete(key);

    } catch (Exception e) {
      e.printStackTrace();
      throw new DataBaseException(ResponseCode.DATABASE_ERROR);
    }
  }

  /**
   * 인증번호 확인시 이메일 인증 상태 저장
   *
   * @param key
   * @param certificationNumber
   */
  @Override
  public boolean verifyCertificationNumber(String key, String certificationNumber) {
    Object storedCertificationNumber = getData(key);

    if (storedCertificationNumber == null) {
      throw new RedisException(ResponseCode.REDIS_DATA_NOT_FOUND);
    }

    if (!storedCertificationNumber.equals(certificationNumber)) {
      return false;
    }

    saveDataWithTTL(key + ":verified", true, 1, TimeUnit.MINUTES);
    deleteData(key);
    return true;
  }

  /**
   * 이메일 인증이 되어있는지 확인
   *
   * @param key
   * @return boolean
   */
  @Override
  public boolean checkVerified(String key) {
    Object storedVerifiedData = getData(key);

    if (storedVerifiedData == null) {
      throw new RedisException(ResponseCode.REDIS_DATA_NOT_FOUND);
    }

    return storedVerifiedData.equals(true);
  }
}
