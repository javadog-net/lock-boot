package net.javadog.lock.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.javadog.lock.dao.DeviceMapper;
import net.javadog.lock.entity.Device;
import net.javadog.lock.service.DeviceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 设备-实现类
 *
 * @author: hdx
 * @Date: 2024-07-05 17:03
 * @version: 1.0
 **/
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    Lock lock = new ReentrantLock();
    @Override
    public void updateDeviceNormal(Long deviceId) {
        Device device = this.getById(deviceId);
        LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Device::getId, deviceId);
        updateWrapper.set(Device::getUseTimes, device.getUseTimes()+1);
        this.update(updateWrapper);
    }

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

    @Override
    public void updateDeviceByAtomicity(Long deviceId) {
        LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Device::getId, deviceId);
        updateWrapper.setSql("use_times = use_times + 1");
        this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceByTansaction(Long deviceId) {
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
}
