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

    /**
     * 更新设备-普通方法。
     *
     * @param deviceId 设备的唯一标识符
     */
    void updateDeviceNormal(Long deviceId);

    /**
     * 更新设备-使用ReentrantLock。
     *
     * @param deviceId 设备的唯一标识符
     */
    void updateDeviceByLock(Long deviceId);

    /**
     * 更新设备-使用原子性更新。
     *
     * @param deviceId 设备的唯一标识符
     */
    void updateDeviceByAtomicity(Long deviceId);

    /**
     * 更新设备-使用事务套锁。
     *
     * @param deviceId 设备的唯一标识符
     */
    void updateDeviceByTansaction(Long deviceId);
}
