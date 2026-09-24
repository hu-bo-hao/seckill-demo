package com.family.seckilldemo.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.family.seckilldemo.entity.Goods;
import com.family.seckilldemo.entity.OrderInfo;
import com.family.seckilldemo.mapper.GoodsMapper;
import com.family.seckilldemo.mapper.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 项目启动时执行，把库存预热到Redis
    @PostConstruct
    public void initStock() {
        Goods goods = goodsMapper.selectById(1L);
        stringRedisTemplate.opsForValue().set("seckill:stock:1", String.valueOf(goods.getStock()));
        System.out.println("库存预热完成，当前库存：" + goods.getStock());
    }

    @GetMapping("/buy")
    public String buy(@RequestParam Long goodsId, @RequestParam Long userId) {
        String stockKey = "seckill:stock:" + goodsId;

        // 1. Redis预减库存
        Long stock = stringRedisTemplate.opsForValue().decrement(stockKey);
        if (stock < 0) {
            stringRedisTemplate.opsForValue().increment(stockKey);
            return "商品已售罄";
        }

        // 2. Redis扣成功了，再扣数据库（乐观锁）
        Goods goods = goodsMapper.selectById(goodsId);
        int updated = goodsMapper.update(null,
                new LambdaUpdateWrapper<Goods>()
                        .eq(Goods::getId, goodsId)
                        .eq(Goods::getVersion, goods.getVersion())
                        .gt(Goods::getStock, 0)
                        .setSql("stock = stock - 1, version = version + 1")
        );

        if (updated == 0) {
            stringRedisTemplate.opsForValue().increment(stockKey);
            return "下单失败，请重试";
        }

        // 3. 创建订单
        OrderInfo order = new OrderInfo();
        order.setGoodsId(goodsId);
        order.setUserId(userId);
        order.setCreateTime(LocalDateTime.now());
        orderInfoMapper.insert(order);

        return "下单成功，Redis剩余库存：" + stock;
    }
}