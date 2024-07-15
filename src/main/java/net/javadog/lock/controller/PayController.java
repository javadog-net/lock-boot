package net.javadog.lock.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.javadog.lock.service.DeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * DeviceController
 *
 * @author hdx
 * @version 1.0
 * @since 2024.07
 */
@RestController
@RequestMapping("/pay")
@Tag(name = "支付-控制器")
public class PayController {

    private DeviceService deviceService;

    @Resource
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

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

    @GetMapping("/C")
    @Operation(summary = "方式C-更新设备-使用原子性更新(不会出现超卖)")
    public void payC(@RequestParam Long deviceId) throws InterruptedException {
        for(int i=0; i<100; i++){
            // 暂停20毫秒，模拟不同时间，不同人请求并发
            Thread.sleep(20);
            // 模拟是个100线程
            new Thread(() -> {
                // 更新-使用原子性更新
                deviceService.updateDeviceByAtomicity(deviceId);
            }).start();
        }
    }

    @GetMapping("/D")
    @Operation(summary = "方式D-更新设备-使用事务套锁(锁失效)")
    public void payD(@RequestParam Long deviceId) throws InterruptedException {
        for(int i=0; i<100; i++){
            // 暂停20毫秒，模拟不同时间，不同人请求并发
            Thread.sleep(20);
            // 模拟是个100线程
            new Thread(() -> {
                // 更新-使用原子性更新
                deviceService.updateDeviceByTansaction(deviceId);
            }).start();
        }
    }
}
