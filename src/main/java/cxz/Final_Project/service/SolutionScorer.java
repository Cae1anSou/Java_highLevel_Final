package cxz.Final_Project.service;

import cxz.Final_Project.model.SchedulableCourse;
import java.util.List;
import java.util.Map;

/**
 * 评价策略接口
 * 定义了为一组选课方案打分的功能。
 */
@FunctionalInterface // 表示这是一个函数式接口，只有一个抽象方法
public interface SolutionScorer {
    /**
     * 为一个选课组合打分。
     * @param solution 一个完整的选课组合
     * @param originalRequirements 原始的学分需求
     * @return 该组合的得分
     */
    double score(List<SchedulableCourse> solution, Map<String, Double> originalRequirements);
}