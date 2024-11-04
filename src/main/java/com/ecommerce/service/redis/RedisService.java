package com.ecommerce.service.redis;

import java.util.concurrent.TimeUnit;

public interface RedisService {

  void saveDataWithTTL(String key, Object value, long timeout, TimeUnit unit);

  void deleteToken(String key);

  boolean verifyCertificationNumber(String key, String certificationNumber);

  boolean checkVerified(String key);

}
