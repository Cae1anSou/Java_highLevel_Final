package cxz.Final_Project.model;

import java.util.List;

public class RatedSolution implements Comparable<RatedSolution> {
    private final List<SchedulableCourse> solution;
    private final double score;
    private double totalCredit = -1;

    public RatedSolution(List<SchedulableCourse> solution, double score) {
        this.solution = solution;
        this.score = score;
    }

    public List<SchedulableCourse> getSolution() {
        return solution;
    }


    public double getScore() {
        return score;
    }

    public double getTotalCredits() {
        if (totalCredit == -1) {
            double total = 0;
            for (SchedulableCourse per : solution) {
                total += per.getCredit();
            }
            totalCredit = total;
        }
        return totalCredit;
    }

    @Override
    public int compareTo(RatedSolution other) {
        // 实现Comparable接口，用于排序，分数高的排在前面
        return Double.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        if (totalCredit == -1) {
            double total = 0;
            for (SchedulableCourse per : solution) {
                total += per.getCredit();
            }
            totalCredit = total;
        }
        return String.format("总学分：%.1f, 评分：%.2f", totalCredit, score);
    }
}