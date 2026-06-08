package org.example.fitness_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.fitness_backend.entity.Exercise;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExerciseMapper extends BaseMapper<Exercise> {
}