package com.university.matching.ui;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 사용자 입력을 검증하는 유틸리티 클래스
 * 데이터베이스 제약 조건에 맞게 입력을 검증합니다
 */
public class InputValidator {

    // MBTI 패턴 (E/I + N/S + T/F + J/P)
    private static final Pattern MBTI_PATTERN = Pattern.compile("^[IE][NS][TF][JP]$");

    // 학번 패턴 (예: 2024001234)
    private static final Pattern STUDENT_NUMBER_PATTERN = Pattern.compile("^\\d{10}$");

    // 유효한 성별
    private static final List<String> VALID_GENDERS = Arrays.asList("M", "F");

    // 유효한 그룹 상태
    private static final List<String> VALID_GROUP_STATUSES =
            Arrays.asList("AVAILABLE", "QUEUING", "MATCHED");

    // 유효한 신고 상태
    private static final List<String> VALID_REPORT_STATUSES =
            Arrays.asList("PENDING", "REVIEWING", "RESOLVED", "REJECTED");

    // 유효한 매칭 세션 상태
    private static final List<String> VALID_SESSION_STATUSES =
            Arrays.asList("ACTIVE", "COMPLETED", "CANCELED");

    // 유효한 매칭 큐 상태
    private static final List<String> VALID_QUEUE_STATUSES =
            Arrays.asList("WAITING", "MATCHED", "CANCELED");

    // 유효한 매칭 타입
    private static final List<String> VALID_MATCHING_TYPES =
            Arrays.asList("ONE_TO_ONE", "THREE_TO_THREE");

    /**
     * 나이가 유효한지 검증합니다 (18-30)
     */
    public static boolean isValidAge(int age) {
        return age >= 18 && age <= 30;
    }

    /**
     * 성별이 유효한지 검증합니다 (M 또는 F)
     */
    public static boolean isValidGender(String gender) {
        if (gender == null) {
            return false;
        }
        return VALID_GENDERS.contains(gender.toUpperCase());
    }

    /**
     * MBTI가 유효한지 검증합니다
     */
    public static boolean isValidMBTI(String mbti) {
        if (mbti == null || mbti.isEmpty()) {
            return true; // MBTI는 선택 사항
        }
        return MBTI_PATTERN.matcher(mbti.toUpperCase()).matches();
    }

    /**
     * 학번이 유효한지 검증합니다
     */
    public static boolean isValidStudentNumber(String studentNumber) {
        if (studentNumber == null) {
            return false;
        }
        return STUDENT_NUMBER_PATTERN.matcher(studentNumber).matches();
    }

    /**
     * 그룹 상태가 유효한지 검증합니다
     */
    public static boolean isValidGroupStatus(String status) {
        if (status == null) {
            return false;
        }
        return VALID_GROUP_STATUSES.contains(status.toUpperCase());
    }

    /**
     * 신고 상태가 유효한지 검증합니다
     */
    public static boolean isValidReportStatus(String status) {
        if (status == null) {
            return false;
        }
        return VALID_REPORT_STATUSES.contains(status.toUpperCase());
    }

    /**
     * 매칭 세션 상태가 유효한지 검증합니다
     */
    public static boolean isValidSessionStatus(String status) {
        if (status == null) {
            return false;
        }
        return VALID_SESSION_STATUSES.contains(status);
    }

    /**
     * 매칭 큐 상태가 유효한지 검증합니다
     */
    public static boolean isValidQueueStatus(String status) {
        if (status == null) {
            return false;
        }
        return VALID_QUEUE_STATUSES.contains(status);
    }

    /**
     * 매칭 타입이 유효한지 검증합니다
     */
    public static boolean isValidMatchingType(String type) {
        if (type == null) {
            return false;
        }
        return VALID_MATCHING_TYPES.contains(type.toUpperCase());
    }

    /**
     * 문자열이 비어있지 않은지 검증합니다
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 문자열 길이가 최대값을 초과하지 않는지 검증합니다
     */
    public static boolean isWithinLength(String str, int maxLength) {
        if (str == null) {
            return true;
        }
        return str.length() <= maxLength;
    }

    /**
     * 숫자 문자열이 유효한지 검증합니다
     */
    public static boolean isValidNumber(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 정수 문자열이 유효한지 검증합니다
     */
    public static boolean isValidInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 숫자가 범위 내에 있는지 검증합니다
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Yes/No 입력이 유효한지 검증합니다
     */
    public static boolean isValidYesNo(String input) {
        if (input == null) {
            return false;
        }
        String lower = input.toLowerCase().trim();
        return lower.equals("y") || lower.equals("n") ||
                lower.equals("yes") || lower.equals("no");
    }

    /**
     * Yes/No 입력을 boolean으로 변환합니다
     */
    public static boolean parseYesNo(String input) {
        if (input == null) {
            return false;
        }
        String lower = input.toLowerCase().trim();
        return lower.equals("y") || lower.equals("yes");
    }

    /**
     * 에러 메시지: 나이
     */
    public static String getAgeErrorMessage() {
        return "Age must be between 18 and 30.";
    }

    /**
     * 에러 메시지: 성별
     */
    public static String getGenderErrorMessage() {
        return "Gender must be 'M' or 'F'.";
    }

    /**
     * 에러 메시지: MBTI
     */
    public static String getMBTIErrorMessage() {
        return "MBTI must be 4 characters (e.g., ENFP, ISTJ). Format: [E/I][N/S][T/F][J/P]";
    }

    /**
     * 에러 메시지: 학번
     */
    public static String getStudentNumberErrorMessage() {
        return "Student number must be 10 digits.";
    }

    /**
     * 에러 메시지: 그룹 상태
     */
    public static String getGroupStatusErrorMessage() {
        return "Valid group statuses: " + String.join(", ", VALID_GROUP_STATUSES);
    }

    /**
     * 에러 메시지: 신고 상태
     */
    public static String getReportStatusErrorMessage() {
        return "Valid report statuses: " + String.join(", ", VALID_REPORT_STATUSES);
    }

    /**
     * 에러 메시지: 빈 문자열
     */
    public static String getNotEmptyErrorMessage(String fieldName) {
        return fieldName + " cannot be empty.";
    }

    /**
     * 에러 메시지: 길이 초과
     */
    public static String getLengthErrorMessage(String fieldName, int maxLength) {
        return fieldName + " cannot exceed " + maxLength + " characters.";
    }

    /**
     * 에러 메시지: 유효하지 않은 숫자
     */
    public static String getInvalidNumberErrorMessage() {
        return "Please enter a valid number.";
    }

    /**
     * 유효한 그룹 상태 목록을 반환합니다
     */
    public static List<String> getValidGroupStatuses() {
        return VALID_GROUP_STATUSES;
    }

    /**
     * 유효한 신고 상태 목록을 반환합니다
     */
    public static List<String> getValidReportStatuses() {
        return VALID_REPORT_STATUSES;
    }

    /**
     * 유효한 성별 목록을 반환합니다
     */
    public static List<String> getValidGenders() {
        return VALID_GENDERS;
    }

    /**
     * 유효한 매칭 타입 목록을 반환합니다
     */
    public static List<String> getValidMatchingTypes() {
        return VALID_MATCHING_TYPES;
    }
}
