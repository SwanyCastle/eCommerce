package com.ecommerce.service.redis;

import java.util.concurrent.TimeUnit;

public interface RedisService {

  void saveDataWithTTL(String key, Object value, long timeout, TimeUnit unit);

  Object getData(String key);

  void deleteData(String key);

  boolean verifyCertificationNumber(String key, String certificationNumber);

  boolean checkVerified(String key);

}
