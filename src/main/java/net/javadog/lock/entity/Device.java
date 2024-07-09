package net.javadog.lock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 设备-实体
 *
 * @author hdx
 * @version 1.0
 * @since 2024.07
 */
@Data
@TableName("device")
public class Device {

    /**
     * 主键id。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 使用时长。
     */
    private Integer useTimes;
}
