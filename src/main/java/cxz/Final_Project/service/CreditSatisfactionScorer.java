package cxz.Final_Project.service;

import cxz.Final_Project.model.SchedulableCourse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreditSatisfactionScorer implements SolutionScorer {
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
                double excess = actual - required;
                totalScore += (required * SATISFACTION_WEIGHT) - (excess * EXCESS_PENALTY_WEIGHT);
            } else {
                totalScore += actual * SATISFACTION_WEIGHT;
            }
        }
        return totalScore;
    }
}