package cxz.Final_Project.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeSlot {

    private final String originalTimeString;
    private final int dayOfWeek;      // 1-7, 代表周一到周日
    private final int startPeriod;    // 开始节次
    private final int endPeriod;      // 结束节次
    private final Set<Integer> weeks; // 包含所有上课周数的集合

    private static final Map<String, Integer> DAY_MAP = new HashMap<>();

    static {
        DAY_MAP.put("一", 1);
        DAY_MAP.put("二", 2);
        DAY_MAP.put("三", 3);
        DAY_MAP.put("四", 4);
        DAY_MAP.put("五", 5);
        DAY_MAP.put("六", 6);
        DAY_MAP.put("日", 7);
    }

    // 星期三第6-7节{2-16周(双)}
    private static final Pattern TIME_PATTERN = Pattern.compile("星期([一二三四五六日])第(\\d+)(?:-(\\d+))?节\\{(.+?)\\}");
    private static final Pattern WEEK_PATTERN = Pattern.compile("(\\\\d+)(?:-(\\\\d+))?周(?:[(（]([单双])[)）])?");

    public TimeSlot(String timeString) {
        this.originalTimeString = timeString;

        Matcher matcher = TIME_PATTERN.matcher(timeString);
        if (matcher.find()) {
            this.dayOfWeek = DAY_MAP.get(matcher.group(1));

            this.startPeriod = Integer.parseInt(matcher.group(2));
            this.endPeriod = (matcher.group(3) != null) ? Integer.parseInt(matcher.group(3)) : this.startPeriod;

            this.weeks = parseWeeks(matcher.group(4));
        } else {
            throw new IllegalArgumentException("无法解析的时间格式: " + timeString);
        }
    }

private Set<Integer> parseWeeks(String weekSpecifier) {
    // 确保在所有操作之前清理字符串
    weekSpecifier = weekSpecifier.trim().replaceAll("\\p{C}", ""); // 强力清理

    Set<Integer> parsedWeeks = new HashSet<>();
    // 注意：这里我将你的 WEEK_PATTERN 中的 \\d+ 改回了 \d+
    // 在Java字符串中定义正则表达式时，单个反斜杠 \ 就够了，除非你想匹配字面上的反斜杠本身。
    // Pattern.compile("(\\d+)(?:-(\\d+))?周(?:[(（]([单双])[)）])?") 是正确的。
    // 你之前代码中的 private static final Pattern WEEK_PATTERN = Pattern.compile("(\\\\d+)(?:-(\\\\d+))?周(?:[(（]([单双])[)）])?");
    // 这里的 \\\\d+ 会匹配字面上的 \d+ 而不是数字，这很可能是导致匹配失败的真正原因！
    Pattern currentWeekPattern = Pattern.compile("(\\d+)(?:-(\\d+))?周(?:[(（]([单双])[)）])?");
    Matcher weekMatcher = currentWeekPattern.matcher(weekSpecifier);

    if (weekMatcher.find()) {
        int startWeek = Integer.parseInt(weekMatcher.group(1));
        // 【关键修正】正确处理可选的 group(2)
        int endWeek = (weekMatcher.group(2) != null) ? Integer.parseInt(weekMatcher.group(2)) : startWeek;
        String type = weekMatcher.group(3); // "单", "双" 或 null

        for (int i = startWeek; i <= endWeek; i++) {
            if (type == null) {
                parsedWeeks.add(i);
            } else if ("单".equals(type) && i % 2 != 0) {
                parsedWeeks.add(i);
            } else if ("双".equals(type) && i % 2 == 0) {
                parsedWeeks.add(i);
            }
        }
    } else {
        System.err.println("警告: 遇到无法解析的周数格式。");
        System.err.println("   - 清理后的字符串: [" + weekSpecifier + "]");
        System.err.println("   - 长度: " + weekSpecifier.length());
        System.err.println("   - 使用的正则: " + currentWeekPattern.pattern());
        System.err.println("   - 原始完整时间: " + this.originalTimeString);
    }
    return parsedWeeks;
}

    public boolean conflictsWith(TimeSlot other) {
        if (this.dayOfWeek != other.dayOfWeek) {
            return false;
        }
        if (this.endPeriod < other.startPeriod || this.startPeriod > other.endPeriod) {
            return false;
        }
        // 星期和节次都有重叠，最后检查周数是否有交集
        return !Collections.disjoint(this.weeks, other.weeks);
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getStartPeriod() {
        return startPeriod;
    }

    public int getEndPeriod() {
        return endPeriod;
    }

    public Set<Integer> getWeeks() {
        return weeks;
    }

    public String getOriginalTimeString() {
        return originalTimeString;
    }

    @Override
    public String toString() {
        return originalTimeString;
    }

    public static void main(String[] args) {
        try {
            TimeSlot ts1 = new TimeSlot("星期三第3-4节{1-16周}");
            TimeSlot ts2 = new TimeSlot("星期三第5-6节{1-16周}");
            TimeSlot ts3 = new TimeSlot("星期五第3-4节{1-8周}");
            TimeSlot ts4 = new TimeSlot("星期三第4-5节{2-16周(双)}"); // 与ts1有冲突
            TimeSlot ts5 = new TimeSlot("星期三第1-2节{1-15周(单)}");

            System.out.println("ts1: " + ts1.getOriginalTimeString());
            System.out.println("ts1 解析结果: 周" + ts1.getDayOfWeek() + ", " + ts1.getStartPeriod() + "-" + ts1.getEndPeriod() + "节, 周数: " + ts1.getWeeks().size() + "周");
            System.out.println("---");
            System.out.println("ts4: " + ts4.getOriginalTimeString());
            System.out.println("ts4 解析结果: 周" + ts4.getDayOfWeek() + ", " + ts4.getStartPeriod() + "-" + ts4.getEndPeriod() + "节, 周数: " + ts4.getWeeks());
            System.out.println("---");

            System.out.println("ts1 和 ts2 是否冲突? " + ts1.conflictsWith(ts2));
            System.out.println("ts1 和 ts3 是否冲突? " + ts1.conflictsWith(ts3));
            System.out.println("ts1 和 ts4 是否冲突? " + ts1.conflictsWith(ts4));
            System.out.println("ts1 和 ts5 是否冲突? " + ts1.conflictsWith(ts5));

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}