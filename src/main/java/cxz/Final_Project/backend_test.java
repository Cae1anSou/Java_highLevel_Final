package cxz.Final_Project;

import cxz.Final_Project.model.RatedSolution;
import cxz.Final_Project.model.SchedulableCourse;
import cxz.Final_Project.service.CoursePlanner;
import cxz.Final_Project.service.CreditSatisfactionScorer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class backend_test {
    public static void main(String[] args) {
        System.out.println("--- 课程规划系统启动 ---");

//        DataImporter dataImporter = new DataImporter();
//        dataImporter.importFromXLSX("src/main/java/cxz/Final_Project/2024-2025学年第二学期全校总课表20250219.xlsx");
//        dataImporter.importFromXLSX("src/main/java/cxz/Final_Project/附件2：2024-2025学年第一学期通识选修课开课情况（含模块信息）20240605.xlsx");

        System.out.println("\n--- 开始执行排课规划 ---");

        CoursePlanner planner = new CoursePlanner();

        String targetSemester = "(2024-2025-2)";
        Map<String, Double> requirements = new HashMap<>();
        requirements.put("经管社科基础素养（创新创业）", 4.0);
        // requirements.put("专业核心课", 3.0); // 可以根据需要添加更多模块要求

        System.out.println("规划学期: " + targetSemester);
        System.out.println("学分要求: " + requirements);

        List<RatedSolution> solutions = planner.plan(
                targetSemester,
                requirements,
                new CreditSatisfactionScorer()
        );

        System.out.println("\n--- 规划完成，找到 " + solutions.size() + " 个方案 ---");
        if (solutions.isEmpty()) {
            System.out.println("未能找到满足所有硬性约束的方案。");
        } else {
            int solutionCount = 0;
            for (RatedSolution ratedSolution : solutions) {
                solutionCount++;
                System.out.println("\n--------- 方案 " + solutionCount + " (得分: " + String.format("%.2f", ratedSolution.getScore()) + ") ---------");
                double totalCredits = 0;
                for (SchedulableCourse course : ratedSolution.getSolution()) {
                    System.out.printf("  - %-20s (%s, %.1f学分, %s)\n",
                            course.getCourseName(),
                            course.getCourseCode(),
                            course.getCredit(),
                            course.getModuleName());
                    totalCredits += course.getCredit();
                }
                System.out.println("  总学分: " + totalCredits);
                if (solutionCount >= 5) {
                    System.out.println("\n(后续方案省略...)");
                    break;
                }
            }
        }
    }
}