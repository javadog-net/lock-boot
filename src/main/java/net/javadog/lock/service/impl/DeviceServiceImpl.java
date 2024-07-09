package net.javadog.lock.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.javadog.lock.dao.DeviceMapper;
import net.javadog.lock.entity.Device;
import net.javadog.lock.service.DeviceService;
import org.springframework.stereotype.Service;

/**
 * 设备-实现类
 *
 * @author: hdx
 * @Date: 2024-07-05 17:03
 * @version: 1.0
 **/
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {
    @Override
    public void updateDevice(Long deviceId) {
        LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Device::getId, deviceId);
        updateWrapper.set(Device::getUseTimes, 5);
        // updateWrapper.setSql("use_times = use_times + 1"); // 将age字段的值加1
        final boolean update = this.update(updateWrapper);
        System.out.println("update" + update);
    }
}
