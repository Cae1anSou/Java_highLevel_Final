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
        this.allCourses = courseOfferingDAO.getSchedulableCoursesBySemester(semester);

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

    private void dfs(List<SchedulableCourse> currentCombination,
                     Map<String, Double> required,
                     BitSet Bitmask,
                     int startIndex,
                     List<List<SchedulableCourse>> allSolutions) {

        if (isGoalMet(required)) {
            allSolutions.add(new ArrayList<>(currentCombination));
            return;
        }

        if (startIndex >= allCourses.size()) {
             if (allSolutions.isEmpty()) {
                 allSolutions.add(new ArrayList<>(currentCombination));
             }
            return;
        }

        for (int i = startIndex; i < allCourses.size(); i++) {
            SchedulableCourse cur = allCourses.get(i);

            if (isValidChoice(cur, Bitmask, required)) {

                currentCombination.add(cur);
                required.computeIfPresent(cur.getModuleName(), (k, v) -> v - cur.getCredits());
                markSchedule(Bitmask, cur, true); // 在位图中标记占用的时间

                dfs(currentCombination, required, Bitmask, i + 1, allSolutions);

                markSchedule(Bitmask, cur, false); // 在位图中释放占用的时间
                required.computeIfPresent(cur.getModuleName(), (k, v) -> v + cur.getCredits());
                currentCombination.remove(currentCombination.size() - 1);
            }
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