package net.javadog.lock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.javadog.lock.entity.Device;

/**
 * 设备-接口
 *
 * @author: hdx
 * @Date: 2024-07-05 17:02
 * @version: 1.0
 **/
public interface DeviceService extends IService<Device> {
    void updateDevice(Long deviceId);


    void updateDeviceByLock(Long deviceId);
}
