package cxz.Final_Project.service;

import cxz.Final_Project.dao.*;

// 这是我们下一步要创建的类
public class DataImporter {

    // 1. 首先，把所有需要的“厨师”(DAO)都聘请过来，作为类的成员变量
    private final ModuleDAO moduleDAO;
    private final TeacherDAO teacherDAO;
    private final CourseDAO courseDAO;
    private final CourseOfferingDAO courseOfferingDAO;
    private final CourseTimeDAO courseTimeDAO;

    // 构造方法，初始化所有DAO
    public DataImporter() {
        this.moduleDAO = new ModuleDAO();
        this.teacherDAO = new TeacherDAO();
        this.courseDAO = new CourseDAO();
        this.courseOfferingDAO = new CourseOfferingDAO();
        this.courseTimeDAO = new CourseTimeDAO();
    }

    // 2. 这是核心的业务方法，它就是“总厨”
    public void importFromXLSX(String filePath) {
        // (这里是使用Apache POI读取XLSX文件的代码)
        // ...

        // 循环处理XLSX的每一行数据
        for (Row row : sheet) {

            // === 开始指挥所有DAO协同工作 ===

            // A. 从行中读取数据
            String moduleName = row.getCell(X).getStringCellValue();
            String teacherName = row.getCell(Y).getStringCellValue();
            String college = row.getCell(Z).getStringCellValue();
            String courseCode = row.getCell(C).getStringCellValue();
            // ...读取其他所有需要的数据...
            String rawTeachingClassCode = row.getCell(T).getStringCellValue(); // e.g., ...-01A

            // B. 调用 ModuleDAO
            int moduleId = moduleDAO.findOrInsertModule(moduleName);

            // C. 调用 TeacherDAO
            int teacherId = teacherDAO.findOrInsertTeacher(teacherName, college);

            // D. 准备 Course "食材" (Model)
            Course course = new Course();
            course.setCourseCode(courseCode);
            course.setName(...);
            course.setCredits(...);
            course.setModuleId(moduleId);
            // 调用 CourseDAO
            courseDAO.insertIfNotExist(course);

            // E. 处理教学班号，得到基础班号
            String baseTeachingClassCode = normalize(rawTeachingClassCode); // e.g., ...-01

            // F. 准备 CourseOffering "食材" (Model)
            CourseOffering offering = new CourseOffering();
            offering.setTeachingClassCode(baseTeachingClassCode);
            offering.setCourseCode(courseCode);
            offering.setTeacherId(teacherId);
            offering.setSemester(...);
            // 调用 CourseOfferingDAO
            courseOfferingDAO.insertIfNotExist(offering);

            // G. 准备 CourseTime "食材" (Model)
            CourseTime time = new CourseTime();
            time.setTeachingClassCode(baseTeachingClassCode);
            time.setTimeString(...);
            time.setTimeType(...);
            // 调用 CourseTimeDAO
            courseTimeDAO.insert(time);

            System.out.println("成功处理一行数据: " + course.getName());
        }
    }
}
