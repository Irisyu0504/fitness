package org.example.fitness_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.fitness_backend.entity.Exercise;
import org.example.fitness_backend.mapper.ExerciseMapper;
import org.example.fitness_backend.service.ExerciseService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExerciseServiceImpl extends ServiceImpl<ExerciseMapper, Exercise> implements ExerciseService {
    @Override
    public boolean save(Exercise exercise) {
        if(exercise.getStatus() == null) {
            exercise.setStatus(1);
        }
        exercise.setCreateTime(LocalDateTime.now());
        exercise.setUpdateTime(LocalDateTime.now());
        return super.save(exercise);
    }

    @Override
    public boolean updateById(Exercise exercise) {
        exercise.setUpdateTime(LocalDateTime.now());
        return super.updateById(exercise);
    }
}
