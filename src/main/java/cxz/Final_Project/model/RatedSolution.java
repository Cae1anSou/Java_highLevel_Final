package cxz.Final_Project.model;

import java.util.List;

public class RatedSolution implements Comparable<RatedSolution> {
    private final List<SchedulableCourse> solution;
    private final double score;

    public RatedSolution(List<SchedulableCourse> solution, double score) {
        this.solution = solution;
        this.score = score;
    }

    public List<SchedulableCourse> getSolution() { return solution; }
    public double getScore() { return score; }

    @Override
    public int compareTo(RatedSolution other) {
        // 实现Comparable接口，用于排序，分数高的排在前面
        return Double.compare(other.score, this.score);
    }
}