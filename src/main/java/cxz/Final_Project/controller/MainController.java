package cxz.Final_Project.controller;

import cxz.Final_Project.dao.CourseOfferingDAO;
import cxz.Final_Project.gui.MainFrame;
import cxz.Final_Project.model.RatedSolution;
import cxz.Final_Project.service.CoursePlanner;
import cxz.Final_Project.service.CreditSatisfactionScorer;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 主控制器 (Controller)
 */
public class MainController {

    private final MainFrame view;
    // 持有DAO和Service的引用，作为Model部分
    private final CourseOfferingDAO courseOfferingDAO;
    private final CoursePlanner coursePlanner;
    private final CreditSatisfactionScorer creditSatisfactionScorer;

    public MainController(MainFrame view) {
        this.view = view;
        // 初始化Model
        this.courseOfferingDAO = new CourseOfferingDAO();
        this.coursePlanner = new CoursePlanner();
        this.creditSatisfactionScorer = new CreditSatisfactionScorer();
    }

    public void initController() {
        view.getPlanButton().addActionListener(e -> startPlanning());
    }

    public void initViewData() {
        List<String> semesters = courseOfferingDAO.findDistinctSemesters();
        view.setSemesterComboBox(semesters);
    }

    private void startPlanning() {
        // 1. 从视图获取用户输入
        String selectedSemester = view.getSelectedSemester();
        Map<String, Double> require = view.getCreditRequire();

        if (selectedSemester == null || selectedSemester.contains("数据库无可用学期")) {
            JOptionPane.showMessageDialog(view, "请先选择一个有效的学期！", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (require.isEmpty()) {
            JOptionPane.showMessageDialog(view, "请至少设置一个模块的学分要求！", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. 准备执行耗时任务，先更新UI状态
        view.setPlanningState(true); // 禁用按钮，显示状态
        view.clearResults(); // 清空上次的结果

        // 3. 使用SwingWorker在后台执行排课算法
        SwingWorker<List<RatedSolution>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RatedSolution> doInBackground() throws Exception {
                // 这个方法在后台线程执行，不会冻结GUI
                return coursePlanner.plan(selectedSemester, require, creditSatisfactionScorer);
            }

            @Override
            protected void done() {
                // 这个方法在事件分发线程(EDT)执行，可以安全地更新GUI
                try {
                    List<RatedSolution> solutions = get(); // 获取doInBackground的结果
                    if (solutions == null || solutions.isEmpty()) {
                        JOptionPane.showMessageDialog(view, "未找到符合条件的课程组合。", "提示", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        view.displayResults(solutions); // 将结果更新到界面
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(view, "排课过程中发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    view.setPlanningState(false); // 无论成功失败，都恢复UI状态
                }
            }
        };

        worker.execute(); // 启动任务
    }
}