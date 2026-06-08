package org.example.fitness_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.fitness_backend.entity.BodyRecord;
import org.example.fitness_backend.entity.User;
import org.example.fitness_backend.mapper.BodyRecordMapper;
import org.example.fitness_backend.mapper.UserMapper;
import org.example.fitness_backend.service.BodyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class BodyRecordServiceImpl extends ServiceImpl<BodyRecordMapper, BodyRecord> implements BodyRecordService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean save(BodyRecord entity) {
        calculateAndSetBmi(entity);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return super.save(entity);
    }

    @Override
    public boolean updateById(BodyRecord entity) {
        calculateAndSetBmi(entity);
        entity.setUpdateTime(LocalDateTime.now());
        return super.updateById(entity);
    }

    @Override
    public void calculateAndSetBmi(BodyRecord record) {
        record.setBmi(null);
        if (record.getWeight() != null && record.getUserId() != null) {
            User user =userMapper.selectById(record.getUserId());
            if(user != null && user.getHeight() != null && user.getHeight().compareTo(BigDecimal.ZERO) > 0){
                BigDecimal heightInMeter = user.getHeight().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal heightSquared = heightInMeter.multiply(heightInMeter);
                BigDecimal bmi = record.getWeight().divide(heightSquared, 1, RoundingMode.HALF_UP);
                record.setBmi(bmi);
            }
        }
    }
}
