package org.example.fitness_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.fitness_backend.entity.BodyRecord;

public interface BodyRecordService extends IService<BodyRecord>{
    void calculateAndSetBmi(BodyRecord record);
}
