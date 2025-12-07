package com.university.matching.ui;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 콘솔 출력을 포맷팅하는 유틸리티 클래스
 * 테이블, 리스트, 구분선 등을 깔끔하게 표시합니다
 */
public class OutputFormatter {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 제목을 출력합니다 (상단 구분선 포함)
     */
    public static void printTitle(String title) {
        int length = title.length() + 4;
        String border = "=".repeat(Math.max(length, 50));

        System.out.println();
        System.out.println(border);
        System.out.println("  " + title);
        System.out.println(border);
    }

    /**
     * 섹션 제목을 출력합니다
     */
    public static void printSection(String section) {
        System.out.println();
        System.out.println("[ " + section + " ]");
        System.out.println("-".repeat(50));
    }

    /**
     * 일반 구분선을 출력합니다
     */
    public static void printDivider() {
        System.out.println("-".repeat(80));
    }

    /**
     * 굵은 구분선을 출력합니다
     */
    public static void printThickDivider() {
        System.out.println("=".repeat(80));
    }

    /**
     * 성공 메시지를 출력합니다
     */
    public static void printSuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    /**
     * 에러 메시지를 출력합니다
     */
    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    /**
     * 경고 메시지를 출력합니다
     */
    public static void printWarning(String message) {
        System.out.println("[WARNING] " + message);
    }

    /**
     * 정보 메시지를 출력합니다
     */
    public static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    /**
     * 테이블을 출력합니다
     */
    public static void printTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return;
        }

        // 각 컬럼의 최대 너비 계산
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, headers.length); i++) {
                if (row[i] != null && row[i].length() > widths[i]) {
                    widths[i] = row[i].length();
                }
            }
        }

        // 상단 경계선
        printTableBorder(widths);

        // 헤더 출력
        printTableRow(headers, widths);

        // 헤더 하단 경계선
        printTableBorder(widths);

        // 데이터 행 출력
        for (String[] row : rows) {
            printTableRow(row, widths);
        }

        // 하단 경계선
        printTableBorder(widths);
    }

    /**
     * 테이블 경계선을 출력합니다
     */
    private static void printTableBorder(int[] widths) {
        System.out.print("+");
        for (int width : widths) {
            System.out.print("-".repeat(width + 2) + "+");
        }
        System.out.println();
    }

    /**
     * 테이블 행을 출력합니다
     */
    private static void printTableRow(String[] cells, int[] widths) {
        System.out.print("|");
        for (int i = 0; i < widths.length; i++) {
            String cell = i < cells.length && cells[i] != null ? cells[i] : "";
            System.out.print(" " + padRight(cell, widths[i]) + " |");
        }
        System.out.println();
    }

    /**
     * 문자열을 오른쪽에 공백을 추가하여 패딩합니다
     */
    private static String padRight(String s, int n) {
        if (s.length() >= n) {
            return s;
        }
        return s + " ".repeat(n - s.length());
    }

    /**
     * 키-값 쌍을 출력합니다
     */
    public static void printKeyValue(String key, Object value) {
        System.out.printf("  %-20s: %s%n", key, value != null ? value.toString() : "N/A");
    }

    /**
     * Timestamp를 포맷팅합니다
     */
    public static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "N/A";
        }
        return DATE_FORMAT.format(timestamp);
    }

    /**
     * 숫자를 포맷팅합니다 (소수점 2자리)
     */
    public static String formatDouble(double value) {
        return String.format("%.2f", value);
    }

    /**
     * 불리언을 Yes/No로 변환합니다
     */
    public static String formatBoolean(boolean value) {
        return value ? "Yes" : "No";
    }

    /**
     * 긴 텍스트를 잘라냅니다
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * 빈 줄을 출력합니다
     */
    public static void printEmptyLine() {
        System.out.println();
    }

    /**
     * 메뉴를 출력합니다
     */
    public static void printMenu(String title, String[] options) {
        printTitle(title);
        System.out.println();
        for (int i = 0; i < options.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, options[i]);
        }
        System.out.println();
    }

    /**
     * 통계 요약을 출력합니다
     */
    public static void printStats(String title, String[][] stats) {
        printSection(title);
        for (String[] stat : stats) {
            if (stat.length >= 2) {
                printKeyValue(stat[0], stat[1]);
            }
        }
    }

    /**
     * 리스트 항목을 번호와 함께 출력합니다
     */
    public static void printNumberedList(List<String> items) {
        for (int i = 0; i < items.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, items.get(i));
        }
    }

    /**
     * 데이터가 없을 때 메시지를 출력합니다
     */
    public static void printNoData() {
        System.out.println();
        System.out.println("  No data found.");
        System.out.println();
    }

    /**
     * 데이터가 없을 때 커스텀 메시지를 출력합니다
     */
    public static void printNoData(String message) {
        System.out.println();
        System.out.println("  " + message);
        System.out.println();
    }

    /**
     * 프롬프트를 출력합니다 (입력 요청)
     */
    public static void printPrompt(String prompt) {
        System.out.print(prompt + ": ");
    }

    /**
     * 작업 진행 중 메시지를 출력합니다
     */
    public static void printProcessing(String message) {
        System.out.println("Processing: " + message + "...");
    }

    /**
     * 확인 프롬프트를 출력합니다
     */
    public static void printConfirmPrompt(String message) {
        System.out.print(message + " (y/n): ");
    }
}
