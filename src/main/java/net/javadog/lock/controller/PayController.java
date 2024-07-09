package net.javadog.lock.controller;

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
public class PayController {

    private DeviceService deviceService;

    @Resource
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/success")
    public void paySuccess(@RequestParam Long deviceId){
        // 模拟是个10线程
        for(int i=0; i<100; i++){
            // 创建线程
            new Thread(() -> {
                for(int j=0; j<10; j++){
                    deviceService.updateDevice(deviceId);
                }
            }).start();
        }
    }

    @GetMapping("/lock")
    public void payLock(@RequestParam Long deviceId){
        // 模拟是个10线程
        for(int i=0; i<10; i++){
            // 创建线程
            new Thread(() -> {
                for(int j=0; j<10; j++){
                    deviceService.updateDeviceByLock(deviceId);
                }
            }).start();
        }
    }
}
