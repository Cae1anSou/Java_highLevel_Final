package cxz.Final_Project.service;

import cxz.Final_Project.model.SchedulableCourse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreditSatisfactionScorer implements SolutionScorer {
    private static final double SATISFACTION_WEIGHT = 10.0; // 满足学分的基准分
    private static final double EXCESS_PENALTY_WEIGHT = 0.5;   // 超出学分的扣分权重

    @Override
    public double score(List<SchedulableCourse> solution, Map<String, Double> require) {
        double totalScore = 0;

        Map<String, Double> preModule = solution.stream()
                .collect(Collectors.groupingBy(
                        course -> course.getModuleName(),
                        Collectors.summingDouble(course -> course.getCredits())
                ));

        for (Map.Entry<String, Double> pair : require.entrySet()) {
            String moduleName = pair.getKey();
            double required = pair.getValue();
            double actual = preModule.getOrDefault(moduleName, 0.0);

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