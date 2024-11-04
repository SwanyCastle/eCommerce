package com.ecommerce.service.redis;

import com.ecommerce.exception.NotFoundException;
import com.ecommerce.repository.redis.RedisRepository;
import com.ecommerce.type.ResponseCode;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisServiceImplement implements RedisService {

  private final RedisRepository redisRepository;

  /**
   * 유효시간을 포함한 key, value 데이터 저장
   *
   * @param key
   * @param value
   * @param timeout
   * @param unit
   */
  @Override
  public void saveDataWithTTL(String key, Object value, long timeout, TimeUnit unit) {
    redisRepository.setData(key, value, timeout, unit);
  }

  /**
   * 로그아웃시 Redis 에 저장된 Token 정보 삭제
   *
   * @param key
   */
  @Override
  public void deleteToken(String key) {
    redisRepository.deleteData(key);
  }

  /**
   * 이메일로 전송받은 인증 번호 확인
   *
   * @param key
   * @param certificationNumber
   * @return boolean
   */
  @Override
  public boolean verifyCertificationNumber(String key, String certificationNumber) {
    Object storedCertificationNumber = redisRepository.getData(key);

    if (storedCertificationNumber == null) {
      throw new NotFoundException(ResponseCode.REDIS_DATA_NOT_FOUND);
    }

    if (!storedCertificationNumber.equals(certificationNumber)) {
      return false;
    }

    // Redis 에 UserId 를 키값으로 인증 완료 여부를 저장. (유효시간 1시간으로 설정)
    redisRepository.setData(key + ":verified", true, 1, TimeUnit.HOURS);
    // 기존에 Redis 에 UserId 를 키값으로 가지는 인증번호 데이터는 유효시간에 관계없이 삭제
    redisRepository.deleteData(key);
    return true;
  }

  /**
   * 이메일 인증을 완료했는지 여부 확인
   *
   * @param key
   * @return boolean
   */
  @Override
  public boolean checkVerified(String key) {
    Object storedVerifiedData = redisRepository.getData(key);
    return storedVerifiedData != null;
  }

}
