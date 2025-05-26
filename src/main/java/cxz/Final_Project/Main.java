package cxz.Final_Project;

import cxz.Final_Project.model.RatedSolution;
import cxz.Final_Project.model.SchedulableCourse;
import cxz.Final_Project.service.CoursePlanner;
import cxz.Final_Project.service.CreditSatisfactionScorer;
import cxz.Final_Project.service.DataImporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- 课程规划系统启动 ---");

        // --- 步骤一 (可选，首次运行时执行): 导入数据 ---
        // 确保你的数据库是空的，或者你想更新数据时，再取消这部分代码的注释
        DataImporter dataImporter = new DataImporter();
        dataImporter.importFromXLSX("src/main/java/cxz/Final_Project/2024-2025学年第二学期全校总课表20250219.xlsx"); // <<--- 替换成你的XLSX文件实际路径
//        dataImporter.importFromXLSX("src/main/java/cxz/Final_Project/附件2：2024-2025学年第一学期通识选修课开课情况（含模块信息）20240605.xlsx"); // <<--- 替换成你的XLSX文件实际路径

        // --- 步骤二: 模拟用户输入，测试排课算法 ---
        System.out.println("\n--- 开始执行排课规划 ---");

        // 1. 创建核心规划器
        CoursePlanner planner = new CoursePlanner();

        // 2. 模拟用户输入的学期和学分要求
        String targetSemester = "(2024-2025-1)"; // <<--- 设置你要规划的学期
        Map<String, Double> requirements = new HashMap<>();
        requirements.put("经管社科基础素养（创新创业）", 4.0);
        // requirements.put("专业核心课", 3.0); // 你可以根据需要添加更多模块要求

        System.out.println("规划学期: " + targetSemester);
        System.out.println("学分要求: " + requirements);

        // 3. 执行规划
        List<RatedSolution> solutions = planner.plan(
                targetSemester,
                requirements,
                new CreditSatisfactionScorer() // 使用我们定义的学分满意度评价策略
        );

        // --- 步骤三: 在控制台打印结果 ---
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
                            course.getCredits(),
                            course.getModuleName());
                    totalCredits += course.getCredits();
                }
                System.out.println("  总学分: " + totalCredits);
                // 只显示前5个最优方案，避免信息过多
                if (solutionCount >= 5) {
                    System.out.println("\n(后续方案省略...)");
                    break;
                }
            }
        }
    }
}