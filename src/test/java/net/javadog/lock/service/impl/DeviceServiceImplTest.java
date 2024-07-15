package net.javadog.lock.service.impl;

import net.javadog.lock.Application;
import net.javadog.lock.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author: hdx
 * @Date: 2024-07-05 17:04
 * @version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
class DeviceServiceImplTest {

    private DeviceService deviceService;

    @Resource
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Test
    void updateDevice() {
        // 模拟是个10线程
        for(int i=0; i<10; i++){
            // 创建线程
            new Thread(() -> {
                try {
                    deviceService.updateDeviceNormal(1L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}