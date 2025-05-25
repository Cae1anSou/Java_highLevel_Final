package cxz.Final_Project.service;

import cxz.Final_Project.model.SchedulableCourse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreditSatisfactionScorer implements SolutionScorer {
    // 定义一些权重，方便调整
    private static final double SATISFACTION_WEIGHT = 10.0; // 满足学分的基准分
    private static final double EXCESS_PENALTY_WEIGHT = 0.5;   // 超出学分的扣分权重

    @Override
    public double score(List<SchedulableCourse> solution, Map<String, Double> originalRequirements) {
        double totalScore = 0;

        // 1. 按模块对已选课程的学分进行分组求和
        Map<String, Double> actualCreditsPerModule = solution.stream()
                .collect(Collectors.groupingBy(
                        SchedulableCourse::getModuleName,
                        Collectors.summingDouble(SchedulableCourse::getCredits)
                ));

        // 2. 遍历所有原始需求，计算得分
        for (Map.Entry<String, Double> entry : originalRequirements.entrySet()) {
            String moduleName = entry.getKey();
            double required = entry.getValue();
            double actual = actualCreditsPerModule.getOrDefault(moduleName, 0.0);

            if (actual >= required) {
                // 如果满足或超出要求
                double excess = actual - required;
                // 得分 = 满足部分得分 - 超出部分扣分
                totalScore += (required * SATISFACTION_WEIGHT) - (excess * EXCESS_PENALTY_WEIGHT);
            } else {
                // 如果未满足要求
                // 得分 = 实际获得部分得分
                totalScore += actual * SATISFACTION_WEIGHT;
            }
        }
        return totalScore;
    }
}