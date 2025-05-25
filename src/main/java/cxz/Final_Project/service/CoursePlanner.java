package cxz.Final_Project.service;

import cxz.Final_Project.dao.CourseOfferingDAO;
import cxz.Final_Project.model.RatedSolution;
import cxz.Final_Project.model.SchedulableCourse;

import java.util.*;

public class CoursePlanner {

    private final CourseOfferingDAO courseOfferingDAO;
    private List<SchedulableCourse> allCourses; // 存储当学期所有课程

    public CoursePlanner() {
        this.courseOfferingDAO = new CourseOfferingDAO();
    }

    public List<RatedSolution> plan(String semester, Map<String, Double> requiredCredits, SolutionScorer scorer) {
        this.allCourses = courseOfferingDAO.getSchedulableCoursesBySemester(semester);

        List<List<SchedulableCourse>> allFoundSolutions = new ArrayList<>();
        // 克隆一份需求Map，因为回溯会修改它
        Map<String, Double> creditsToFulfill = new HashMap<>(requiredCredits);

        dfs(new ArrayList<>(), creditsToFulfill, 0, allFoundSolutions);

        // 【新增】对所有找到的方案进行打分
        List<RatedSolution> ratedSolutions = new ArrayList<>();
        for (List<SchedulableCourse> solution : allFoundSolutions) {
            double score = scorer.score(solution, requiredCredits);
            ratedSolutions.add(new RatedSolution(solution, score));
        }

        // 【新增】对方案按分数进行排序
        Collections.sort(ratedSolutions);

        return ratedSolutions;
    }

    private void dfs(List<SchedulableCourse> courses,
                     Map<String, Double> require,
                     int start,
                     List<List<SchedulableCourse>> allSolutions) {

        if (isGoalMet(require)) {
            allSolutions.add(new ArrayList<>(courses));
            return;  // TODO: 可以是找到一个就返回，也可以找多个
        }

        for (int i = start; i < allCourses.size(); i++) {
            SchedulableCourse cur = allCourses.get(i);

            if (isValidChoice(cur, courses, require)) {
                courses.add(cur);
                require.computeIfPresent(cur.getModuleName(), (k, v) -> v - cur.getCredits());
                dfs(courses, require, i + 1, allSolutions);

                // 回溯
                courses.remove(courses.size() - 1);
                require.computeIfPresent(cur.getModuleName(), (k, v) -> v + cur.getCredits());
            }
        }
    }

    private boolean isValidChoice(SchedulableCourse current, List<SchedulableCourse> courses, Map<String, Double> require) {
        Double curReq = require.get(current.getModuleName());
        if (curReq == null || curReq < current.getCredits()) {
            return false;
        }

        // b. 检查时间是否冲突
        for (SchedulableCourse selectedCourse : courses) {
            if (current.conflictsWith(selectedCourse)) {
                return false;
            }
        }
        return true;
    }

    // 辅助方法：检查所有模块的学分是否都已满足
    private boolean isGoalMet(Map<String, Double> creditsToFulfill) {
        return creditsToFulfill.values().stream().allMatch(v -> v <= 0);
    }
}