package cxz.Final_Project.service;

import cxz.Final_Project.dao.CourseOfferingDAO;
import cxz.Final_Project.model.RatedSolution;
import cxz.Final_Project.model.SchedulableCourse;
import cxz.Final_Project.model.TimeSlot;

import java.util.*;

public class CoursePlanner {

    private final CourseOfferingDAO courseOfferingDAO;
    private List<SchedulableCourse> allCourses;

    public CoursePlanner() {
        this.courseOfferingDAO = new CourseOfferingDAO();
    }

    public List<RatedSolution> plan(String semester, Map<String, Double> required, SolutionScorer scorer) {
        this.allCourses = courseOfferingDAO.getSchedulableCourses(semester);

        List<List<SchedulableCourse>> allFoundSolutions = new ArrayList<>();
        Map<String, Double> curCredit = new HashMap<>(required);

        BitSet Bitmask = new BitSet(7 * 13);              // TODO: 后期可以拓展

        dfs(new ArrayList<>(), curCredit, Bitmask, 0, allFoundSolutions);

        List<RatedSolution> ratedSolutions = new ArrayList<>();
        for (List<SchedulableCourse> solution : allFoundSolutions) {
            double score = scorer.score(solution, required);
            ratedSolutions.add(new RatedSolution(solution, score));
        }

        Collections.sort(ratedSolutions);

        return ratedSolutions;
    }

    // cxz.Final_Project.service.CoursePlanner.java
    private void dfs(List<SchedulableCourse> currentCombination,
                     Map<String, Double> required, // 实际需求，会被修改
                     BitSet Bitmask,             // 当前时间占用图，会被修改
                     int startIndex,
                     List<List<SchedulableCourse>> allSolutions) {

        // 【新的保存策略】当探索到某个叶子节点（无法再加课，或者所有课都试过）
        // 或者，在尝试所有从 startIndex 开始的课程后，都应该将当前的 currentCombination 视为一个候选解
        // 但为了避免在中间步骤保存不完整的解，我们主要在 startIndex 到达末尾时保存。
        // 并且，为了收集所有可能的组合（包括不满足学分的），在每次尝试添加课程后，
        // 无论是否满足 isGoalMet，都继续向下探索。

        if (startIndex == allCourses.size()) {
            // 所有课程都已考虑完毕，将当前组合（如果非空）作为一个候选解
            if (!currentCombination.isEmpty()) {
                allSolutions.add(new ArrayList<>(currentCombination));
            }
            return;
        }

        // 决策1：不选择 allCourses.get(startIndex) 这门课
        dfs(currentCombination, required, Bitmask, startIndex + 1, allSolutions);

        // 决策2：尝试选择 allCourses.get(startIndex) 这门课
        SchedulableCourse candidate = allCourses.get(startIndex);
        if (isValidChoice(candidate, Bitmask, required)) { // isValidChoice 只检查时间冲突和模块是否需要
            currentCombination.add(candidate);
            markSchedule(Bitmask, candidate, true);
            // 更新学分需求 (直接在共享的 'required' map 上操作)
            // 注意：这种直接修改共享状态的方式，在回溯时必须精确恢复
            double originalCreditsForModule = required.getOrDefault(candidate.getModuleName(), 0.0);
            required.computeIfPresent(candidate.getModuleName(), (k, v) -> v - candidate.getCredit());

            dfs(currentCombination, required, Bitmask, startIndex + 1, allSolutions);

            // 回溯
            required.put(candidate.getModuleName(), originalCreditsForModule); // 精确恢复学分
            markSchedule(Bitmask, candidate, false);
            currentCombination.remove(currentCombination.size() - 1);
        }
    }

    private boolean isValidChoice(SchedulableCourse cur, BitSet Bitmask, Map<String, Double> required) {
        if (!required.containsKey(cur.getModuleName())) {
            return false;
        }

        for (TimeSlot slot : cur.getTimeSlots()) {
            for (int p = slot.getStartPeriod(); p <= slot.getEndPeriod(); p++) {
                int bitIndex = (slot.getDayOfWeek() - 1) * 13 + (p - 1);
                if (Bitmask.get(bitIndex)) {
                    return false; // 该时间点已被占用，冲突
                }
            }
        }
        return true;
    }

    private boolean isGoalMet(Map<String, Double> required) {
        return required.values().stream().allMatch(v -> v <= 0);
    }

    private void markSchedule(BitSet Bitmask, SchedulableCourse course, boolean job) {
        for (TimeSlot slot : course.getTimeSlots()) {
            for (int p = slot.getStartPeriod(); p <= slot.getEndPeriod(); p++) {
                int bitIndex = (slot.getDayOfWeek() - 1) * 13 + (p - 1);
                if (job) {
                    Bitmask.set(bitIndex);
                } else {
                    Bitmask.clear(bitIndex);
                }
            }
        }
    }
}