package com.family.seckilldemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.seckilldemo.entity.Goods;
import com.family.seckilldemo.mapper.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/list")
    public List<Goods> list() {
        return goodsMapper.selectList(null);
    }

    @GetMapping("/{id}")
    public Goods detail(@PathVariable Long id) {
        // 先查Redis
        String key = "goods:" + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            // Redis有，直接返回（简化处理，实际要转成对象）
            System.out.println("从Redis读取");
        }

        // Redis没有，查数据库
        Goods goods = goodsMapper.selectById(id);

        // 存入Redis，过期时间10分钟
        stringRedisTemplate.opsForValue().set(key, goods.getName() + ":" + goods.getStock(), 10, TimeUnit.MINUTES);
        System.out.println("从数据库读取");

        return goods;
    }
}