import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {
    public static void main(String[] args) {
        Pattern WEEK_PATTERN = Pattern.compile("(\\d+)(?:-(\\d+))?周(?:[(（]([单双])[)）])?");
        String[] testStrings = {
            "1-16周",
            "1周",
            "1-15周(单)",
            "2-16周(双)",
            "13-16周",
            "6-10周",
            "1-5周",
            "1-6周",
            "1-12周",
            "9-16周",
            "1-8周",
            "14-16周(双)",
            "2-12周(双)",
            "1-11周(单)",
            "13-15周(单)",
            "1-9周(单)",
            "11-15周(单)",
            "3-9周(单)",
            "4-10周(双)",
            "5-11周(单)",
            "1周 " // 测试带尾部空格
        };

        for (String s : testStrings) {
            String toTest = s.trim(); //确保测试的是trim后的
            Matcher matcher = WEEK_PATTERN.matcher(toTest);
            if (matcher.find()) {
                System.out.println("匹配成功: [" + toTest + "]");
                System.out.println("  Group 1 (startWeek): " + matcher.group(1));
                if (matcher.group(2) != null) {
                    System.out.println("  Group 2 (endWeek): " + matcher.group(2));
                }
                if (matcher.group(3) != null) {
                    System.out.println("  Group 3 (type): " + matcher.group(3));
                }
            } else {
                System.err.println("匹配失败: [" + toTest + "]");
            }
            System.out.println("---");
        }
    }
}