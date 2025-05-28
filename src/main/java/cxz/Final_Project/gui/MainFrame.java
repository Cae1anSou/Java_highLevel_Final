package cxz.Final_Project.gui;

import cxz.Final_Project.controller.MainController;
import cxz.Final_Project.model.RatedSolution;
import cxz.Final_Project.model.SchedulableCourse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    private final JButton planButton;
    private final JComboBox<String> semesterComboBox;
    private final JTable requirementsTable;
    private final DefaultTableModel requirementsTableModel;
    private final JLabel statusLabel;

    // 结果展示区组件
    private final JList<RatedSolution> solutionList;
    private final DefaultListModel<RatedSolution> solutionListModel;
    private final JTextArea solutionDetailArea;

    public MainFrame() {
        setTitle("智能课程规划系统");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ---- 1. 北部：控制面板 ----
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.add(new JLabel("目标学期:"));
        semesterComboBox = new JComboBox<>();
        semesterComboBox.setPreferredSize(new Dimension(150, 25));
        controlPanel.add(semesterComboBox);
        planButton = new JButton("开始智能排课");
        controlPanel.add(planButton);

        // ---- 2. 西部：学分要求 ----
        JPanel westPanel = new JPanel(new BorderLayout(0, 5));
        westPanel.setBorder(BorderFactory.createTitledBorder("学分要求设置"));
        String[] columnNames = {"课程模块", "要求学分"};
        Object[][] data = {
                {"军事体育素养与身心健康素养模块", 0.0}, {"经管社科基础素养（创新创业）", 0.0},
                {"人文艺术素养与应用写作能力模块", 0.0}, {"通用能力拓展模块", 0.0},
                {"外语交流能力模块", 0.0}, {"自然科学与信息技术模块", 0.0}
        };
        requirementsTableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Double.class : String.class;
            }
        };
        requirementsTable = new JTable(requirementsTableModel);
        requirementsTable.setRowHeight(25);
        JScrollPane reqTableScrollPane = new JScrollPane(requirementsTable);
        reqTableScrollPane.setPreferredSize(new Dimension(320, 200));
        westPanel.add(reqTableScrollPane, BorderLayout.CENTER);

        // ---- 3. 中部：结果展示区 ----
        solutionListModel = new DefaultListModel<>();
        solutionList = new JList<>(solutionListModel);
        solutionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        solutionDetailArea = new JTextArea("请先进行排课，然后在此查看选中方案的详情...");
        solutionDetailArea.setEditable(false);
        solutionDetailArea.setFont(new Font("SF Pro Display", Font.PLAIN, 14));

        // 使用JSplitPane来分隔方案列表和详情
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(solutionList), new JScrollPane(solutionDetailArea));
        splitPane.setDividerLocation(250); // 设置初始分隔位置

        // ---- 4. 南部：状态栏 ----
        statusLabel = new JLabel("就绪");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 5. 组装所有面板
        add(controlPanel, BorderLayout.NORTH);
        add(westPanel, BorderLayout.WEST);
        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // 6. 添加事件监听器
        solutionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                RatedSolution selected = solutionList.getSelectedValue();
                if (selected != null) {
                    updateSolutionDetails(selected);
                }
            }
        });

        // 7. MVC连接
        MainController controller = new MainController(this);
        controller.initController();
        controller.initViewData();
    }

    // --- 以下是提供给Controller或内部调用的公共方法 ---

    public JButton getPlanButton() {
        return planButton;
    }

    public String getSelectedSemester() {
        Object selected = semesterComboBox.getSelectedItem();
        return selected != null ? selected.toString() : null;
    }

    public void setSemesterComboBox(List<String> semesters) {
        SwingUtilities.invokeLater(() -> {
            semesterComboBox.removeAllItems();
            if (semesters == null || semesters.isEmpty()) {
                semesterComboBox.addItem("数据库无可用学期");
            } else {
                for (String semester : semesters) {
                    semesterComboBox.addItem(semester);
                }
            }
        });
    }

    public Map<String, Double> getCreditRequire() {
        Map<String, Double> requirements = new HashMap<>();
        for (int i = 0; i < requirementsTableModel.getRowCount(); i++) {
            String module = (String) requirementsTableModel.getValueAt(i, 0);
            Double credits = (Double) requirementsTableModel.getValueAt(i, 1);
            if (credits != null && credits > 0) {
                requirements.put(module, credits);
            }
        }
        return requirements;
    }

    public void setPlanningState(boolean inProgress) {
        SwingUtilities.invokeLater(() -> {
            planButton.setEnabled(!inProgress);
            statusLabel.setText(inProgress ? "正在排课中，请稍候..." : "就绪");
        });
    }

    public void clearResults() {
        SwingUtilities.invokeLater(() -> {
            solutionListModel.clear();
            solutionDetailArea.setText("请先进行排课，然后在此查看选中方案的详情...");
        });
    }

    public void displayResults(List<RatedSolution> solutions) {
        SwingUtilities.invokeLater(() -> {
            solutionListModel.clear();
            for (RatedSolution solution : solutions) {
                solutionListModel.addElement(solution);
            }
            statusLabel.setText("排课完成！共找到 " + solutions.size() + " 个可行方案。");
        });
    }

    private void updateSolutionDetails(RatedSolution solution) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("方案详情 (总学分: %.1f, 得分: %.2f)\n", solution.getTotalCredits(), solution.getScore()));
        sb.append("========================================\n");
        for (SchedulableCourse course : solution.getSolution()) {
            sb.append(String.format("课程: %s (%s)\n", course.getCourseName(), course.getCourseCode()));
            sb.append(String.format("所属模块：%s\n", course.getModuleName()));
            sb.append(String.format("学分: %.1f\n", course.getCredit()));
            sb.append(String.format("教师: %s\n", course.getTeacherName()));
            sb.append("时间: ");
            course.getTimeSlots().forEach(ts -> sb.append(ts.getOriginalTimeString()).append("; "));
            sb.append("\n----------------------------------------\n");
        }
        solutionDetailArea.setText(sb.toString());
        solutionDetailArea.setCaretPosition(0); // 滚动到顶部
    }
}