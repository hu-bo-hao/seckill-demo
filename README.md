# 高并发秒杀系统

基于 Spring Boot + Redis + RabbitMQ 的电商秒杀系统，解决高并发场景下的库存超卖和接口性能问题。

## 技术栈

- 后端：Spring Boot 2.6.13、MyBatis-Plus
- 缓存：Redis（预减库存、热点数据缓存）
- 消息队列：RabbitMQ（异步下单、死信队列延迟关单）
- 数据库：MySQL
- 并发控制：Lua脚本、乐观锁

## 核心功能

1. **商品查询**：Redis缓存商品详情，减少数据库压力
2. **Redis预减库存**：秒杀请求先在Redis中扣减库存，库存不足直接拦截
3. **MySQL乐观锁**：数据库扣减库存时使用version字段，防止超卖
4. **异步下单**：Redis扣减成功后发送消息到RabbitMQ，异步创建订单
5. **接口限流**：防止恶意刷单

## 项目结构

seckill-demo
├── controller
│   ├── GoodsController.java      # 商品查询接口
│   └── SeckillController.java    # 秒杀接口
├── entity
│   ├── Goods.java                # 商品实体
│   └── OrderInfo.java            # 订单实体
├── mapper
│   ├── GoodsMapper.java
│   └── OrderInfoMapper.java
└── resources
    └── application.properties     # 配置文件

## 运行方式

1. 启动MySQL，创建数据库 seckill
2. 启动Redis
3. 启动RabbitMQ
4. 运行 SeckillDemoApplication.java
5. 访问 http://localhost:8081/goods 查看商品列表
