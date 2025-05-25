package cxz.Final_Project.service;

import cxz.Final_Project.dao.*;
import cxz.Final_Project.model.Course;
import cxz.Final_Project.model.CourseOffering;
import cxz.Final_Project.model.CourseTime;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataImporter {

    private final ModuleDAO moduleDAO;
    private final TeacherDAO teacherDAO;
    private final CourseDAO courseDAO;
    private final CourseOfferingDAO courseOfferingDAO;
    private final CourseTimeDAO courseTimeDAO;

    public DataImporter() {
        this.moduleDAO = new ModuleDAO();
        this.teacherDAO = new TeacherDAO();
        this.courseDAO = new CourseDAO();
        this.courseOfferingDAO = new CourseOfferingDAO();
        this.courseTimeDAO = new CourseTimeDAO();
    }

    public void importFromXLSX(String filePath) {
        File excelFile = new File(filePath);
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 1. 【核心步骤】创建表头映射
            Map<String, Integer> headerMap = new HashMap<>();
            Row headerRow = sheet.getRow(0); // 获取第一行作为表头
            if (headerRow == null) {
                System.err.println("错误：Excel文件没有表头！");
                return;
            }
            for (Cell cell : headerRow) {
                headerMap.put(cell.getStringCellValue(), cell.getColumnIndex());
            }

            List<String> requiredHeaders = Arrays.asList(
                    "课程代码", "课程名称", "教学班", "学分",
                    "任课教师", "上课时间", "课程归属", "开课学院"
            );

            // 使用Java Stream API来找出所有缺失的表头
            List<String> missingHeaders = requiredHeaders.stream()
                    .filter(header -> !headerMap.containsKey(header))
                    .collect(Collectors.toList());

            if (!missingHeaders.isEmpty()) {
                // 如果有缺失的，一次性全部报告出来
                System.err.println("错误：Excel文件格式不正确，缺少以下必需的列： " + String.join(", ", missingHeaders));
                return; // 直接返回，不再继续执行
            }

            System.out.println("表头校验通过，开始处理数据...");

            // 2. 循环处理数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // 3. 【核心步骤】通过列名从Map中获取索引，再获取数据
                    String courseCode = getStringCellValue(row.getCell(headerMap.get("课程代码")));
                    String courseName = getStringCellValue(row.getCell(headerMap.get("课程名称")));
                    String rawClassCode = getStringCellValue(row.getCell(headerMap.get("教学班")));
                    double credits = getNumericCellValue(row.getCell(headerMap.get("学分")));
                    String teacherName = getStringCellValue(row.getCell(headerMap.get("任课教师")));
                    String timeString = getStringCellValue(row.getCell(headerMap.get("上课时间")));
                    String moduleName = getStringCellValue(row.getCell(headerMap.get("课程归属")));
                    String college = getStringCellValue(row.getCell(headerMap.get("开课学院")));

                    // 【核心修正】从教学班号派生出学期 semester
                    String semester = parseSemesterFromTeachingClassCode(rawClassCode);

                    // 如果教学班号或课程代码为空，则跳过此行，因为它们是关键ID
                    if (rawClassCode.isEmpty() || courseCode.isEmpty()) {
                        System.err.println("警告：第 " + (i + 1) + " 行缺少关键信息（课程代码或教学班），已跳过。");
                        continue;
                    }

                    int moduleId = moduleDAO.findOrInsert(moduleName);
                    int teacherId = teacherDAO.findOrInsert(teacherName, college);

                    Course course = new Course();
                    course.setCode(courseCode);
                    course.setName(courseName);
                    course.setCredits(credits);
                    course.setModuleId(moduleId);
                    courseDAO.insertIfNotExist(course);

                    String baseClassCode = normalizeClassCode(rawClassCode);

                    CourseOffering offering = new CourseOffering();
                    offering.setClassCode(baseClassCode);
                    offering.setCourseCode(courseCode);
                    offering.setTeacherId(teacherId);
                    offering.setSemester(semester);
                    courseOfferingDAO.insertIfNotExist(offering);

                    CourseTime time = new CourseTime();
                    time.setClassCode(baseClassCode);
                    time.setTimeString(timeString);
                    time.setTimeType("讲课学时");
                    courseTimeDAO.insert(time);

                    System.out.printf("成功处理课程: %s, 授课老师：%s%n", courseName, teacherName);

                } catch (Exception e) {
                    System.err.println("处理第 " + (i + 1) + " 行数据时发生错误: " + e.getMessage());
                }
            }
            System.out.println("数据导入完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double getNumericCellValue(Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        try {
            return Double.parseDouble(cell.getStringCellValue());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return new BigDecimal(cell.getNumericCellValue()).toPlainString();
            default:
                return "";
        }
    }

    private String normalizeClassCode(String rawCode) {
        if (rawCode == null) return "";
        return rawCode.replaceAll("[A-Z]$", ""); // 去掉末尾的单个大写字母
    }

    private static final Pattern SEMESTER_PATTERN = Pattern.compile("^\\([^)]+\\)");

    private String parseSemesterFromTeachingClassCode(String rawCode) {
        if (rawCode == null) return "Unknown";
        Matcher matcher = SEMESTER_PATTERN.matcher(rawCode);
        if (matcher.find()) {
            return matcher.group(0);
        }
        // TODO: 需要再想想处理逻辑
        return "UnknownSemester";
    }
}