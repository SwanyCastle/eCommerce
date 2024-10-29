package com.ecommerce.repository.redis;

import java.util.concurrent.TimeUnit;

public interface RedisRepository {

  void setData(String key, Object value, long timeout, TimeUnit unit);

  Object getData(String key);

  void deleteData(String key);

}
