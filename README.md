## 前言

### 🍊缘由

####  Java事务套着锁，就像女色缠着我

![](https://img.javadog.net/blog/java-lock/94847721425249c5b0a715d0708f7782.png)

🏀事情起因：

**大家好，我是JavaDog程序狗**

在一个阳光明媚的中午，我的师傅突然找到我，问了我如下一个问题：

**Java中已经加了锁，为什么结果却还是超卖了！**

先解释下上述的几个关键词

- 我的师傅：前**阿里大牛**，目前屈居**青岛大厂管理岗**......此处省略一万字夸赞

- 加锁：模拟使用**ReentrantLock加锁**，多线程下建议使用**Redisson分布式锁**实现

- 超卖：是指系统允许多个用户购买或预订**超过实际可用数量的资源**

各位小伙伴先有个印象，后续本狗会详细讲解关键词

******

### 🎯主要目标

#### 实现4大重点

##### 1. 什么是超卖

##### 2. 超卖如何解决

##### 3. 事务套锁失效问题

##### 4. 解决锁失效问题

******

### 🎁如何获取源码

本狗将测试的所有代码均已上传，包含多个示例，小伙伴们可亲测

公众号：【JavaDog程序狗】

关注公众号，**发送  “lock”**，无任何套路即可获得！

![](https://img.javadog.net/blog/java-lock/27170c4df74c41e58a701796d91a1331.png)

![](https://img.javadog.net/blog/java-lock/84c694ce68604041be956c944bf5fbe3.png)

##  正文

### 🍅情景前置

#### 空调租赁充值时长超卖问题

因我师傅遇到的问题代码涉及隐私，我们就模拟一个场景来分析我们的问题

举例🌰

炎炎夏日，狗哥宿舍因忍受不了酷热，**租赁了一台空调**，大家在清爽空调的吹拂下渐渐迷失自我

随着租赁时长到期，空调暂停工作，需要我们**充值空调使用时长**

我们宿舍**100个赤膊大汉**，分分掏出自己手机**同时进行空调使用时长充值**......并发超卖问题由此而来

![](https://img.javadog.net/blog/java-lock/085b50f8cc5b474d86dfbdc96867bd66.png)

为此设计了一个设备表，用于下方演示调试

```sql
CREATE TABLE `device` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `use_times` int(10) DEFAULT '0' COMMENT '使用时长',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

![](https://img.javadog.net/blog/java-lock/ad753bc8194d4499bca5e81f7cfb876a.png)


### 🥦目标分析

#### 一. 什么是超卖？

> 系统允许多个用户购买或预订超过实际可用数量的资源

 👽人话解释

某件商品库存数量1件，结果卖给2个人；更形象的就是双胞胎，一个爱情结晶却喜提两个宝贝

结合上述**情景前置**，就是宿舍这100个人同时并发操作，按照正常逻辑每个人充值都会在基数+1小时，则总使用时长应为100。

但因为**多线程并发**问题，可能会导致A和B同时处理逻辑时，获取基数都是同一个，+1后同时更新入库，这样最终的总使用时长就会小于100，出现超卖问题。

##### 出现超卖的代码

```java
@GetMapping("/A")
@Operation(summary = "方式A-更新设备-普通方法(会出现超卖)")
public void payA(@RequestParam Long deviceId) throws InterruptedException {
    for(int i=0; i<100; i++){
        // 暂停20毫秒，模拟不同时间，不同人请求并发
        Thread.sleep(20);
        // 模拟是个100线程
        new Thread(() -> {
            // 更新设备-普通方法
            deviceService.updateDeviceNormal(deviceId);
        }).start();
    }
}
```

```java
@Override
public void updateDeviceNormal(Long deviceId) {
    Device device = this.getById(deviceId);
    LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(Device::getId, deviceId);
    updateWrapper.set(Device::getUseTimes, device.getUseTimes()+1);
    this.update(updateWrapper);
}
```

##### 出现超卖的代码截图

![](https://img.javadog.net/blog/java-lock/9546db044fa3453caac697a678e3e732.png)

![](https://img.javadog.net/blog/java-lock/7761366a67bb4474b4096b0d834c451a.png)

##### 出现超卖的代码调试

可以启动本狗代码，访问http://localhost:1026/lock  ，查看swagger文档，或者使用postman都是一样的

![](https://img.javadog.net/blog/java-lock/2bd62488738a451381795ac02022823b.png)

![](https://img.javadog.net/blog/java-lock/4492cb6e893f431dbabeccd65a9d754a.png)

##### 出现超卖测试结果

![](https://img.javadog.net/blog/java-lock/1138d14920004ce496d022b55d08681e.png)

******

#### 二. 超卖如何解决

- 乐观锁
通常通过在**数据库表中增加一个版本号（version）字段来实现**。在更新时，系统会检查当前版本号是否与请求时的版本号相匹配，如果匹配则更新，否则认为数据已被其他事务修改，当前事务失败。

- 悲观锁
在**数据被读取时就锁定数据**，直到事务结束。在数据库中，可以通过SELECT ... FOR UPDATE语句来实现，这会阻止其他事务对锁定的数据进行修改，直到当前事务完成。

- 分布式锁
在分布式系统中，**单个节点**的锁机制**不足以保证数据的一致性**，因此需要使用分布式锁，如**Redisson**

- 代码锁
使用 synchronized 关键字或者使用 ReentrantLock等

解决方式有很多种，我们举例以ReentrantLock为例，但首先我们要了解什么是ReentrantLock?

>ReentrantLock 是 Java 平台上的一个**可重入的互斥锁**，它属于 java.util.concurrent.locks 包的一部分。与传统的 synchronized 关键字相比，ReentrantLock 提供了更多高级功能和更大的灵活性。

再啰嗦解释一下，什么是可重入的互斥锁？

>可重入的互斥锁是一种特殊的锁机制，**它允许在同一个线程内多次获取而不造成死锁**。通常，互斥锁（mutex）在一个线程获取后，会阻止其他线程获取该锁，直到锁被释放。然而，**可重入的互斥锁允许一个线程在已经获取了锁的情况下再次获取锁，而不会引起死锁，而是增加锁的持有计数**。当这个线程最终释放锁时，它必须释放相同的次数才能完全释放锁，让其他线程有机会获取。

##### 加入ReentrantLock代码

```java
@GetMapping("/B")
@Operation(summary = "方式B-更新设备-使用ReentrantLock(不会出现超卖)")
public void payB(@RequestParam Long deviceId) throws InterruptedException {
    // 模拟是个10线程
    for(int i=0; i<100; i++){
        // 暂停20毫秒，模拟不同时间，不同人请求并发
        Thread.sleep(20);
        // 创建线程
        new Thread(() -> {
            // 更新设备-使用ReentrantLock。
            deviceService.updateDeviceByLock(deviceId);
        }).start();
    }
}
```

```java
Lock lock = new ReentrantLock();
@Override
public void updateDeviceByLock(Long deviceId) {
    lock.lock();
    Device device = this.getById(deviceId);
    LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(Device::getId, deviceId);
    updateWrapper.set(Device::getUseTimes, device.getUseTimes()+1);
    try {
        this.update(updateWrapper);
    } catch (Exception e) {
        throw new RuntimeException(e);
    } finally {
        lock.unlock();
    }
}
```

##### 加入ReentrantLock代码截图

![](https://img.javadog.net/blog/java-lock/ca70480cc3f44ad19b3560e4b0894525.png)

![](https://img.javadog.net/blog/java-lock/8dc59a8d2b56413081e4f95d58ec18ad.png)

##### ReentrantLock代码调试

![](https://img.javadog.net/blog/java-lock/7a17b2e5c1234dabb851287acb64505e.png)

##### ReentrantLock测试结果

![](https://img.javadog.net/blog/java-lock/af21f490fb2545efb0c8ae5c9cc9fe92.png)

******
#### 三. 事务套锁失效问题

上面第二步已经通过加入ReentrantLock成功解决超卖问题

BUT，我师傅的代码中却还存在超卖问题，排查一下，原来在**锁外面加入了事务@Transactional**

##### 事务套锁失效代码

```java
@GetMapping("/C")
@Operation(summary = "方式C-更新设备-使用事务套锁(锁失效)")
public void payC(@RequestParam Long deviceId) throws InterruptedException {
    for(int i=0; i<100; i++){
        // 暂停20毫秒，模拟不同时间，不同人请求并发
        Thread.sleep(20);
        // 模拟是个100线程
        new Thread(() -> {
            // 更新-使用事务套锁
            deviceService.updateDeviceByTansaction(deviceId);
        }).start();
    }
}
```

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void updateDeviceByTansaction(Long deviceId) {
    // 加锁
    lock.lock();
    Device device = this.getById(deviceId);
    LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(Device::getId, deviceId);
    updateWrapper.set(Device::getUseTimes, device.getUseTimes()+1);
    try {
        this.update(updateWrapper);
    } catch (Exception e) {
        throw new RuntimeException(e);
    } finally {
        // 解锁
        lock.unlock();
    }
}
```

##### 事务套锁失效代码截图

![](https://img.javadog.net/blog/java-lock/1be8bd834213429f8b50fc79cd7ca474.png)

![](https://img.javadog.net/blog/java-lock/19321415c03e402498bd5b872516d27d.png)

##### 事务套锁失效代码调试

![](https://img.javadog.net/blog/java-lock/857b6a98105a4963aac186eb5b16d8bf.png)

##### 事务套锁失效测试结果

![](https://img.javadog.net/blog/java-lock/e3993f0f32f34981ad2967983eadf67f.png)

 ❓为什么加入事务就导致锁失效了呢？

 ✅答案是因为**事务边界问题**
 
> 使用@Transactional 注解来管理事务，但**锁的获取和释放并没有放在事务边界之内**。这意味着**如果在事务提交之前锁就被释放了，其他线程可能在当前事务结束之前修改相同的数据，这会导致数据不一致**。

![](https://img.javadog.net/blog/java-lock/568005cc2d834e24a2e092f95c7bc221.png)

![](https://img.javadog.net/blog/java-lock/d7b6c8f76c8248d98ff490fdd126af1f.png)

#### 四. 解决锁失效问题

通过上面分析事务套锁失效问题，我们可以**采取事务边界缩小**，尽量不要让事务边界过大，从而导致包裹着锁导致并发数据问题

##### 事务边界缩小代码

```java
@GetMapping("/D")
@Operation(summary = "方式D-更新设备-缩小事务便捷方法(锁正常-不会出现超卖)")
public void payD(@RequestParam Long deviceId) throws InterruptedException {
    for(int i=0; i<100; i++){
        // 暂停20毫秒，模拟不同时间，不同人请求并发
        Thread.sleep(20);
        // 模拟是个100线程
        new Thread(() -> {
            // 更新-使用原子性更新
            deviceService.updateDeviceByReduce(deviceId);
        }).start();
    }
}
```

```java
@Override
public void updateDeviceByReduce(Long deviceId) {
     try {
         // 加锁
         lock.lock();
         this.updateDevice(deviceId);
     } finally {
         // 解锁
         lock.unlock();
     }
 }

 @Transactional(rollbackFor = Exception.class)
 public void updateDevice(Long deviceId){
     Device device = this.getById(deviceId);
     LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
     updateWrapper.eq(Device::getId, deviceId);
     updateWrapper.set(Device::getUseTimes, device.getUseTimes()+1);
     this.update(updateWrapper);
 }
```

##### 事务边界缩小代码截图

![](https://img.javadog.net/blog/java-lock/f92950cde00f4f5ab7e54b3debd03286.png)

![](https://img.javadog.net/blog/java-lock/5f3ddc73115748398beff887c67c3ec3.png)

##### 事务边界缩小代码调试

![](https://img.javadog.net/blog/java-lock/60573ac5d65845cfa43b4f58cda5f1ee.png)

##### 事务边界缩小测试结果

![](https://img.javadog.net/blog/java-lock/8f4d840956c3481189b4c944250fb381.png)

## 总结

处理并发和超卖问题时，理解并**合理运用锁机制和事务管理**至关重要。

**通过将锁操作置于事务边界内，可以有效防止数据不一致，确保系统的稳定性和可靠性**。

在实际应用中，根据业务特性和性能要求选择最合适的解决方案是关键

解决方案概述

- 乐观锁：通过版本号或时间戳检查数据是否已被其他事务修改，适用于读多写少的场景。

- 悲观锁：预先锁定数据直至事务完成，适合写操作频繁或数据竞争激烈的场景。

- 分布式锁：如Redisson，确保分布式系统中数据的一致性，适用于跨节点的数据同步。

- 代码级锁：利用synchronized或ReentrantLock等机制，控制线程间的访问顺序，防止并发冲突。

事务边界的重要性

- 关键点：确保锁的获取和释放严格位于事务边界内，避免数据在事务未完成前被其他线程修改。

- 实践：使用try-finally结构包裹锁的获取和释放，确保即使发生异常，锁也能正确释放，维护数据完整性。

### 🍈猜你想问

####  如何与狗哥联系进行探讨

##### 关注公众号【JavaDog程序狗】

公众号回复【入群】或者【加入】，便可成为【程序员学习交流摸鱼群】的一员，问题随便问，牛逼随便吹，目前**群内已有超过290+个小伙伴啦**！！！

![](https://img.javadog.net/blog/java-lock/2998b2a49f7dc8c8ddd3a345e5694422.png)

##### 2.踩踩狗哥博客

[javadog.net](https://www.javadog.net/)

**里面有狗哥的私密联系方式呦 😘**

>大家可以在里面留言，随意发挥，有问必答

![](https://img.javadog.net/blog/java-lock/7e380641de0de84f8d07a0d85dabe389.png)

******

###  🍯猜你喜欢

####  文章推荐

[【规范】Git分支管理，看看我司是咋整的](https://mp.weixin.qq.com/s/8LRB9k-4EsgSN1lCy5az8A)

[【工具】珍藏免费宝藏工具，不好用你来捶我](https://mp.weixin.qq.com/s/Mj5--CQVaafs_bInMCgUaQ)

[【插件】IDEA这款插件，爱到无法自拔](https://mp.weixin.qq.com/s/IePixEWV5JMG1X2R4mwt6g)

[【规范】看看人家Git提交描述，那叫一个规矩](https://mp.weixin.qq.com/s/EbNWRpSYMdWFv5aUQ2ockw)

[【工具】用nvm管理nodejs版本切换，真香！](https://mp.weixin.qq.com/s/N6qwQpH-oIgFGSWIVDJ-2g)

[【项目实战】SpringBoot+uniapp+uview2打造H5+小程序+APP入门学习的聊天小项目](https://mp.weixin.qq.com/s/g7AZOWLgW5vcCahyJDEPKA)

[【项目实战】SpringBoot+uniapp+uview2打造一个企业黑红名单吐槽小程序](https://mp.weixin.qq.com/s/t_qwF_HvkdW-6TI3sYUHrA)

[【模块分层】还不会SpringBoot项目模块分层？来这手把手教你！](https://mp.weixin.qq.com/s/fpkiNR2tj832a6VxZozwDg)

 
![](https://img.javadog.net/blog/java-lock/5ee9ad70af6c2ec5d3730bd0c15565e6.jpg)