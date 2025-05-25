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

    /**
     * 主规划方法，接收用户需求和评价策略，返回排好序的方案列表
     */
    public List<RatedSolution> plan(String semester, Map<String, Double> requiredCredits, SolutionScorer scorer) {
        // 1. 从数据库加载本学期的所有可排课程
        this.allCourses = courseOfferingDAO.getSchedulableCoursesBySemester(semester);

        List<List<SchedulableCourse>> allFoundSolutions = new ArrayList<>();
        Map<String, Double> creditsToFulfill = new HashMap<>(requiredCredits);

        // 2. 初始化一个 7天 * 13节 的位图，代表整周的课表
        BitSet scheduleBitmap = new BitSet(7 * 13);

        // 3. 启动核心回溯算法
        backtrack(new ArrayList<>(), creditsToFulfill, scheduleBitmap, 0, allFoundSolutions);

        // 4. 对所有找到的方案进行打分
        List<RatedSolution> ratedSolutions = new ArrayList<>();
        for (List<SchedulableCourse> solution : allFoundSolutions) {
            double score = scorer.score(solution, requiredCredits);
            ratedSolutions.add(new RatedSolution(solution, score));
        }

        // 5. 对方案按分数进行排序
        Collections.sort(ratedSolutions);

        return ratedSolutions;
    }

    /**
     * 核心回溯算法
     */
    private void backtrack(List<SchedulableCourse> currentCombination,
                           Map<String, Double> creditsToFulfill,
                           BitSet scheduleBitmap,
                           int startIndex,
                           List<List<SchedulableCourse>> allSolutions) {

        // 【新终止条件 & 剪枝策略】
        // 检查是否已满足所有模块的学分要求，如果满足，则这是一个高质量解，保存并终止本条路径的探索。
        if (isGoalMet(creditsToFulfill)) {
            allSolutions.add(new ArrayList<>(currentCombination));
            return;
        }

        // 如果已经没有课程可以选了，也终止
        if (startIndex >= allCourses.size()) {
            // 如果一个完美解都找不到，就把这种“尽力而为”的解也存下来
            // （可以根据需求决定是否保留这部分逻辑，保留可以应对无完美解的情况）
             if (allSolutions.isEmpty()) {
                 allSolutions.add(new ArrayList<>(currentCombination));
             }
            return;
        }

        // 遍历剩余课程，进行“选择-探索-回溯”
        for (int i = startIndex; i < allCourses.size(); i++) {
            SchedulableCourse candidate = allCourses.get(i);

            // 【选择】检查当前选择是否有效
            if (isValidChoice(candidate, scheduleBitmap, creditsToFulfill)) {

                // --- 做出选择 ---
                currentCombination.add(candidate);
                creditsToFulfill.computeIfPresent(candidate.getModuleName(), (k, v) -> v - candidate.getCredits());
                markSchedule(scheduleBitmap, candidate, true); // 在位图中标记占用的时间

                // --- 探索 (进入下一层决策) ---
                backtrack(currentCombination, creditsToFulfill, scheduleBitmap, i + 1, allSolutions);

                // --- 回溯 (撤销选择) ---
                markSchedule(scheduleBitmap, candidate, false); // 在位图中释放占用的时间
                creditsToFulfill.computeIfPresent(candidate.getModuleName(), (k, v) -> v + candidate.getCredits());
                currentCombination.remove(currentCombination.size() - 1);
            }
        }
    }

    /**
     * 【高效】检查选择是否有效，使用BitSet进行时间冲突判断
     */
    private boolean isValidChoice(SchedulableCourse candidate, BitSet scheduleBitmap, Map<String, Double> creditsToFulfill) {
        // a. 检查该课程的模块是否是我们需要的
        if (!creditsToFulfill.containsKey(candidate.getModuleName())) {
            return false;
        }

        // b. 检查时间冲突 - 现在变得极其高效
        for (TimeSlot slot : candidate.getTimeSlots()) {
            for (int p = slot.getStartPeriod(); p <= slot.getEndPeriod(); p++) {
                int bitIndex = (slot.getDayOfWeek() - 1) * 13 + (p - 1);
                if (scheduleBitmap.get(bitIndex)) {
                    return false; // 该时间点已被占用，冲突
                }
            }
        }
        return true;
    }

    /**
     * 检查是否所有模块学分都已满足（或超出）
     */
    private boolean isGoalMet(Map<String, Double> creditsToFulfill) {
        return creditsToFulfill.values().stream().allMatch(v -> v <= 0);
    }

    /**
     * 在位图中标记或释放课程占用的时间段
     * @param occupy true表示占用，false表示释放
     */
    private void markSchedule(BitSet scheduleBitmap, SchedulableCourse course, boolean occupy) {
        for (TimeSlot slot : course.getTimeSlots()) {
            for (int p = slot.getStartPeriod(); p <= slot.getEndPeriod(); p++) {
                // 将 (星期, 节次) 映射到 BitSet 的一维索引
                // 假设周一第1节 -> 0, 周一第2节 -> 1 ... 周日第13节 -> 7*13-1=90
                int bitIndex = (slot.getDayOfWeek() - 1) * 13 + (p - 1);
                if (occupy) {
                    scheduleBitmap.set(bitIndex);
                } else {
                    scheduleBitmap.clear(bitIndex);
                }
            }
        }
    }
}