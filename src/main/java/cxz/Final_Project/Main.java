// 在 cxz.Final_Project.Main.java 中
package cxz.Final_Project;

import cxz.Final_Project.gui.MainFrame; // 稍后我们会创建这个类
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 使用 SwingUtilities.invokeLater 来确保GUI在事件分发线程上创建
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}