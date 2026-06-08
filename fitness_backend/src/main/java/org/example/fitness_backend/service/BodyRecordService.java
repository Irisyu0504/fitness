package org.example.fitness_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.fitness_backend.entity.BodyRecord;

/**
 * 体测记录服务接口。
 * <p>管理用户的身体测量数据（体重、体脂率、腰围、BMI 等），
 * 并提供 BMI 自动计算能力。</p>
 */
public interface BodyRecordService extends IService<BodyRecord>{

    /**
     * 根据记录中的身高和体重自动计算并设置 BMI 值。
     * @param record 体测记录实体，需包含身高和体重数据
     */
    void calculateAndSetBmi(BodyRecord record);
}
