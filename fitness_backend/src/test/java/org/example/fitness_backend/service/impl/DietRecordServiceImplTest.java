package org.example.fitness_backend.service.impl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DietRecordServiceImplTest {

    @Test
    void buildMacroAnalysisReturnsEmptyTodayMessageWhenNoRecordsExist() {
        DietRecordServiceImpl service = new DietRecordServiceImpl();

        Map<String, Object> analysis = service.buildMacroAnalysis(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

        assertEquals("今天还未记录，快来添加饮食记录吧", analysis.get("macroReminder"));
        assertEquals(0, analysis.get("proteinPercent"));
        assertEquals(0, analysis.get("carbsPercent"));
        assertEquals(0, analysis.get("fatPercent"));
    }

    @Test
    void buildMacroAnalysisWarnsWhenCarbsAreAboveAdultAmdrRange() {
        DietRecordServiceImpl service = new DietRecordServiceImpl();

        Map<String, Object> analysis = service.buildMacroAnalysis(
                new BigDecimal("10"),
                new BigDecimal("250"),
                new BigDecimal("20"),
                3
        );

        assertEquals(82, analysis.get("carbsPercent"));
        assertTrue(((String) analysis.get("macroReminder")).contains("碳水占比偏高"));
        assertTrue(((String) analysis.get("macroReminder")).contains("65%"));
    }

    @Test
    void buildMacroAnalysisAcceptsMacrosInsideAdultAmdrRange() {
        DietRecordServiceImpl service = new DietRecordServiceImpl();

        Map<String, Object> analysis = service.buildMacroAnalysis(
                new BigDecimal("75"),
                new BigDecimal("250"),
                new BigDecimal("65"),
                3
        );

        assertEquals(16, analysis.get("proteinPercent"));
        assertEquals(53, analysis.get("carbsPercent"));
        assertEquals(31, analysis.get("fatPercent"));
        assertEquals("今日三大营养素占比在推荐范围内，继续保持。", analysis.get("macroReminder"));
    }

    @Test
    void normalizeMealTimeToMinuteDropsSecondsAndNanos() {
        DietRecordServiceImpl service = new DietRecordServiceImpl();

        LocalDateTime normalized = service.normalizeMealTimeToMinute(
                LocalDateTime.of(2026, 5, 29, 12, 34, 56, 123_000_000)
        );

        assertEquals(LocalDateTime.of(2026, 5, 29, 12, 34), normalized);
    }
}
