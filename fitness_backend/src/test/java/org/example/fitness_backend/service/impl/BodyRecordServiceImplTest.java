package org.example.fitness_backend.service.impl;

import org.example.fitness_backend.entity.BodyRecord;
import org.example.fitness_backend.entity.User;
import org.example.fitness_backend.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BodyRecordServiceImplTest {

    @Test
    void calculateAndSetBmiUsesProfileHeight() {
        BodyRecordServiceImpl service = serviceWithUserHeight(new BigDecimal("170.0"));
        BodyRecord record = recordWithWeightAndStaleBmi(new BigDecimal("68.0"));

        service.calculateAndSetBmi(record);

        assertEquals(new BigDecimal("23.5"), record.getBmi());
    }

    @Test
    void calculateAndSetBmiClearsStaleBmiWhenProfileHeightIsMissing() {
        BodyRecordServiceImpl service = serviceWithUserHeight(null);
        BodyRecord record = recordWithWeightAndStaleBmi(new BigDecimal("68.0"));

        service.calculateAndSetBmi(record);

        assertNull(record.getBmi());
    }

    private BodyRecordServiceImpl serviceWithUserHeight(BigDecimal height) {
        User user = new User();
        user.setHeight(height);

        UserMapper userMapper = mock(UserMapper.class);
        when(userMapper.selectById(1L)).thenReturn(user);

        BodyRecordServiceImpl service = new BodyRecordServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", userMapper);
        return service;
    }

    private BodyRecord recordWithWeightAndStaleBmi(BigDecimal weight) {
        BodyRecord record = new BodyRecord();
        record.setUserId(1L);
        record.setWeight(weight);
        record.setBmi(new BigDecimal("99.9"));
        return record;
    }
}
