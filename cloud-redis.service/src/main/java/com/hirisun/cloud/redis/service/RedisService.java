package com.hirisun.cloud.redis.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoufeng
 * @version 1.0
 * @className RedisService
 * @data 2020/7/6 10:14
 * @description RedisService 接口
 */
public interface RedisService {

    /**
     * set操作不过期
     *
     * @param key 键
     * @param value 值
     */
    void setForPerpetual(String key, Object value);

    /**
     * set操作给定过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     */
    void setForTerminable(String key, Object value, long timeout, TimeUnit timeUnit);

    /**
     * get操作
     *
     * @param key 键
     * @return 值
     */
    Object get(String key);

    /**
     * keys操作
     *
     * @param pattern 匹配模式
     * @return
     */
    Set<String> keys(String pattern);

    /**
     * range操作
     *
     * @param key 键
     * @param start 起始位置
     * @param end 结束位置
     * @return 值
     */
    List<Object> range(String key, long start, long end);

    /**
     * expire操作
     *
     * @param key 键
     * @param timeout 时间
     * @param timeUnit 时间单位
     * @return 是否成功
     */
    Boolean expire(String key, long timeout, TimeUnit timeUnit);

    /**
     * remove操作
     *
     * @param key 键
     * @param count 个数
     * @param value 值
     * @return
     */
    Long remove(String key, long count, Object value);
    
    /**
     * delete操作
     *
     * @param key 键
     * @return
     */
    Boolean delete(String key);

    /**
     * hasKey操作
     *
     * @param key 键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * leftPush操作
     *
     * @param key 键
     * @param value 值
     * @return
     */
    Long leftPush(String key, Object value);

    /**
     * getExpire操作
     *
     * @param key 键
     * @param timeUnit 时间单位
     * @return
     */
    Long getExpire(String key, TimeUnit timeUnit);
}
