package org.example.fitness_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.fitness_backend.entity.FitnessGoal;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FitnessGoalMapper extends BaseMapper<FitnessGoal> {
}