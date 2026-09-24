package com.family.seckilldemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/test")
    public String test() {
        stringRedisTemplate.opsForValue().set("name", "hello redis");
        String value = stringRedisTemplate.opsForValue().get("name");
        return "Redis测试成功，读取到：" + value;
    }
}