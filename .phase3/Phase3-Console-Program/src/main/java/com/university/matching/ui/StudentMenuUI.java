package com.university.matching.ui;

import com.university.matching.dao.StudentDAO;
import com.university.matching.model.Student;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 학생 관리 메뉴 UI
 * 모든 학생 관련 기능과 동적 쿼리를 처리합니다
 */
public class StudentMenuUI {
    private final Scanner scanner;
    private final StudentDAO studentDAO;

    public StudentMenuUI(Scanner scanner) {
        this.scanner = scanner;
        this.studentDAO = new StudentDAO();
    }

    /**
     * 학생 관리 메뉴를 표시합니다
     */
    public void showMenu() {
        String[] options = {
                "View All Students",
                "Find Student by ID",
                "Add New Student",
                "Update Student",
                "Delete Student",
                "Query 1-1: Find Students by Gender and Age",
                "Query 3-1: Get Department Statistics",
                "Query 4-1: Find Students Below Average Age",
                "Query 4-2: Find Students from Top Group Department",
                "Query 5-1: Find Active Students (who sent messages)",
                "Query 5-2: Find Reporting Students",
                "Query 6-1: Find Students by MBTI",
                "Query 6-2: Find Students by Multiple Departments",
                "Query 7-1: Find Students Above Department Average",
                "Query 10-1: Find Active or Reporting Students (UNION)",
                "Query 10-2: Find Message Senders Without Reports (MINUS)",
                "Back to Main Menu"
        };

        OutputFormatter.printMenu("Student Management", options);
        OutputFormatter.printPrompt("Select an option (1-17)");

        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1":
                    viewAllStudents();
                    break;
                case "2":
                    findStudentById();
                    break;
                case "3":
                    addNewStudent();
                    break;
                case "4":
                    updateStudent();
                    break;
                case "5":
                    deleteStudent();
                    break;
                case "6":
                    findStudentsByGenderAndAge();
                    break;
                case "7":
                    getDepartmentStatistics();
                    break;
                case "8":
                    findStudentsBelowAverageAge();
                    break;
                case "9":
                    findStudentsFromTopGroupDepartment();
                    break;
                case "10":
                    findActiveStudents();
                    break;
                case "11":
                    findReportingStudents();
                    break;
                case "12":
                    findStudentsByMBTI();
                    break;
                case "13":
                    findStudentsByDepartments();
                    break;
                case "14":
                    findStudentsAboveDepartmentAverage();
                    break;
                case "15":
                    findActiveOrReportingStudents();
                    break;
                case "16":
                    findMessageSendersWithoutReports();
                    break;
                case "17":
                    return;
                default:
                    OutputFormatter.printWarning("Invalid option.");
            }
        } catch (SQLException e) {
            OutputFormatter.printError("Database error: " + e.getMessage());
            e.printStackTrace();
        }

        waitForEnter();
    }

    // ==================== 기존 CRUD 기능 ====================

    private void viewAllStudents() throws SQLException {
        OutputFormatter.printSection("All Students");
        List<Student> students = studentDAO.findAll();

        if (students.isEmpty()) {
            OutputFormatter.printNoData("No students found.");
            return;
        }

        displayStudentsTable(students);
    }

    private void findStudentById() throws SQLException {
        OutputFormatter.printSection("Find Student by ID");
        OutputFormatter.printPrompt("Enter Student ID");
        String input = scanner.nextLine().trim();

        if (!InputValidator.isValidNumber(input)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }

        Long studentId = Long.parseLong(input);
        Student student = studentDAO.findById(studentId);

        if (student == null) {
            OutputFormatter.printWarning("Student not found with ID: " + studentId);
            return;
        }

        displayStudentDetails(student);
    }

    private void addNewStudent() throws SQLException {
        OutputFormatter.printSection("Add New Student");

        OutputFormatter.printPrompt("Student Number (10 digits)");
        String studentNumber = scanner.nextLine().trim();
        if (!InputValidator.isValidStudentNumber(studentNumber)) {
            OutputFormatter.printError(InputValidator.getStudentNumberErrorMessage());
            return;
        }

        OutputFormatter.printPrompt("Name");
        String name = scanner.nextLine().trim();
        if (!InputValidator.isNotEmpty(name)) {
            OutputFormatter.printError(InputValidator.getNotEmptyErrorMessage("Name"));
            return;
        }

        OutputFormatter.printPrompt("Age (18-30)");
        String ageStr = scanner.nextLine().trim();
        if (!InputValidator.isValidInteger(ageStr)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        int age = Integer.parseInt(ageStr);
        if (!InputValidator.isValidAge(age)) {
            OutputFormatter.printError(InputValidator.getAgeErrorMessage());
            return;
        }

        OutputFormatter.printPrompt("Gender (M/F)");
        String gender = scanner.nextLine().trim().toUpperCase();
        if (!InputValidator.isValidGender(gender)) {
            OutputFormatter.printError(InputValidator.getGenderErrorMessage());
            return;
        }

        OutputFormatter.printPrompt("MBTI (e.g., ENFP) - Press Enter to skip");
        String mbti = scanner.nextLine().trim().toUpperCase();
        if (!mbti.isEmpty() && !InputValidator.isValidMBTI(mbti)) {
            OutputFormatter.printError(InputValidator.getMBTIErrorMessage());
            return;
        }

        OutputFormatter.printPrompt("Department");
        String department = scanner.nextLine().trim();
        if (!InputValidator.isNotEmpty(department)) {
            OutputFormatter.printError(InputValidator.getNotEmptyErrorMessage("Department"));
            return;
        }

        OutputFormatter.printPrompt("Profile (optional) - Press Enter to skip");
        String profile = scanner.nextLine().trim();

        Student student = new Student();
        student.setStudentNumber(studentNumber);
        student.setName(name);
        student.setAge(age);
        student.setGender(gender);
        student.setMbti(mbti.isEmpty() ? null : mbti);
        student.setDepartment(department);
        student.setProfile(profile.isEmpty() ? null : profile);

        Long id = studentDAO.insert(student);
        if (id != null) {
            OutputFormatter.printSuccess("Student added successfully with ID: " + id);
        } else {
            OutputFormatter.printError("Failed to add student.");
        }
    }

    private void updateStudent() throws SQLException {
        OutputFormatter.printSection("Update Student");
        OutputFormatter.printPrompt("Enter Student ID to update");
        String input = scanner.nextLine().trim();

        if (!InputValidator.isValidNumber(input)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }

        Long studentId = Long.parseLong(input);
        Student student = studentDAO.findById(studentId);

        if (student == null) {
            OutputFormatter.printWarning("Student not found with ID: " + studentId);
            return;
        }

        OutputFormatter.printInfo("Current student details:");
        displayStudentDetails(student);

        OutputFormatter.printPrompt("New Age (current: " + student.getAge() + ") - Press Enter to keep");
        String ageStr = scanner.nextLine().trim();
        if (!ageStr.isEmpty()) {
            if (!InputValidator.isValidInteger(ageStr)) {
                OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
                return;
            }
            int age = Integer.parseInt(ageStr);
            if (!InputValidator.isValidAge(age)) {
                OutputFormatter.printError(InputValidator.getAgeErrorMessage());
                return;
            }
            student.setAge(age);
        }

        OutputFormatter.printPrompt("New Department (current: " + student.getDepartment() + ") - Press Enter to keep");
        String department = scanner.nextLine().trim();
        if (!department.isEmpty()) {
            student.setDepartment(department);
        }

        if (studentDAO.update(student)) {
            OutputFormatter.printSuccess("Student updated successfully.");
        } else {
            OutputFormatter.printError("Failed to update student.");
        }
    }

    private void deleteStudent() throws SQLException {
        OutputFormatter.printSection("Delete Student");
        OutputFormatter.printPrompt("Enter Student ID to delete");
        String input = scanner.nextLine().trim();

        if (!InputValidator.isValidNumber(input)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }

        Long studentId = Long.parseLong(input);
        Student student = studentDAO.findById(studentId);

        if (student == null) {
            OutputFormatter.printWarning("Student not found with ID: " + studentId);
            return;
        }

        displayStudentDetails(student);
        OutputFormatter.printConfirmPrompt("Are you sure you want to delete this student?");
        String confirm = scanner.nextLine().trim();

        if (!InputValidator.parseYesNo(confirm)) {
            OutputFormatter.printInfo("Delete cancelled.");
            return;
        }

        if (studentDAO.delete(studentId)) {
            OutputFormatter.printSuccess("Student deleted successfully.");
        } else {
            OutputFormatter.printError("Failed to delete student.");
        }
    }

    // ==================== 동적 쿼리 ====================

    /**
     * Query 1-1: Find Students by Gender and Age
     */
    private void findStudentsByGenderAndAge() throws SQLException {
        OutputFormatter.printSection("Query 1-1: Find Students by Gender and Age");

        OutputFormatter.printPrompt("Gender (M/F)");
        String gender = scanner.nextLine().trim().toUpperCase();
        if (!InputValidator.isValidGender(gender)) {
            OutputFormatter.printError(InputValidator.getGenderErrorMessage());
            return;
        }

        OutputFormatter.printPrompt("Minimum Age");
        String ageStr = scanner.nextLine().trim();
        if (!InputValidator.isValidInteger(ageStr)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        int minAge = Integer.parseInt(ageStr);

        List<Student> students = studentDAO.findStudentsByGenderAndAge(gender, minAge);

        if (students.isEmpty()) {
            OutputFormatter.printNoData("No students found matching criteria.");
            return;
        }

        OutputFormatter.printInfo("Found " + students.size() + " students:");
        displayStudentsTable(students);
    }

    /**
     * Query 3-1: Get Department Statistics
     */
    private void getDepartmentStatistics() throws SQLException {
        OutputFormatter.printSection("Query 3-1: Get Department Statistics");
        List<StudentDAO.DepartmentStats> stats = studentDAO.getDepartmentStatistics();

        if (stats.isEmpty()) {
            OutputFormatter.printNoData("No department statistics found.");
            return;
        }

        OutputFormatter.printInfo("Found statistics for " + stats.size() + " departments:");

        String[] headers = {"Department", "Student Count", "Average Age"};
        List<String[]> rows = new ArrayList<>();

        for (StudentDAO.DepartmentStats stat : stats) {
            rows.add(new String[]{
                    stat.department(),
                    String.valueOf(stat.studentCount()),
                    OutputFormatter.formatDouble(stat.avgAge())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    /**
     * Query 4-1: Find Students Below Average Age
     */
    private void findStudentsBelowAverageAge() throws SQLException {
        OutputFormatter.printSection("Query 4-1: Find Students Below Average Age");
        List<Student> students = studentDAO.findStudentsBelowAverageAge();

        if (students.isEmpty()) {
            OutputFormatter.printNoData("No students found below average age.");
            return;
        }

        OutputFormatter.printInfo("Found " + students.size() + " students below average age:");
        displayStudentsTable(students);
    }

    /**
     * Query 4-2: Find Students from Top Group Department
     */
    private void findStudentsFromTopGroupDepartment() throws SQLException {
        OutputFormatter.printSection("Query 4-2: Find Students from Top Group Department");
        List<Student> students = studentDAO.findStudentsFromTopGroupDepartment();

        if (students.isEmpty()) {
            OutputFormatter.printNoData("No students found from top group department.");
            return;
        }

        OutputFormatter.printInfo("Found " + students.size() + " students from top group department:");
        displayStudentsTable(students);
    }

    /**
     * Query 5-1: Find Active Students (who sent messages)
     */
    private void findActiveStudents() throws SQLException {
        OutputFormatter.printSection("Query 5-1: Find Active Students");
        List<Student> students = studentDAO.findActiveStudents();

        if (students.isEmpty()) {
            OutputFormatter.printNoData("No active students found.");
            return;
        }

        OutputFormatter.printInfo("Found " + students.size() + " active students:");
        displayStudentsTable(students);
    }

    /**
     * Query 5-2: Find Reporting Students
     */
    private void findReportingStudents() throws SQLException {
        OutputFormatter.printSection("Query 5-2: Find Reporting Students");
        List<Student> students = studentDAO.findReportingStudents();

        if (students.isEmpty()) {
            OutputFormatter.printNoData("No reporting students found.");
            return;
        }

        OutputFormatter.printInfo("Found " + students.size() + " reporting students:");
        displayStudentsTable(students);
    }

    /**
     * Query 6-1: Find Students by MBTI
     */
    private void findStudentsByMBTI() throws SQLException {
        OutputFormatter.printSection("Query 6-1: Find Students by MBTI");
        OutputFormatter.printInfo("Enter MBTI types separated by commas (e.g., ENFP,ISTJ,INFJ)");
        OutputFormatter.printPrompt("MBTI types");
        String input = scanner.nextLine().trim().toUpperCase();

        String[] mbtiArray = input.split(",");
        List<String> mbtiList = new ArrayList<>();

        for (String mbti : mbtiArray) {
            String trimmed = mbti.trim();
            if (!InputValidator.isValidMBTI(trimmed)) {
                OutputFormatter.printError("Invalid MBTI: " + trimmed);
                return;
            }
            mbtiList.add(trimmed);
        }

        List<Student> students = studentDAO.findStudentsByMBTI(mbtiList);

        if (students.isEmpty()) {
            OutputFormatter.printNoData("No students found with specified MBTI types.");
            return;
        }

        OutputFormatter.printInfo("Found " + students.size() + " students:");
        displayStudentsTable(students);
    }

    /**
     * Query 6-2: Find Students by Multiple Departments
     */
    private void findStudentsByDepartments() throws SQLException {
        OutputFormatter.printSection("Query 6-2: Find Students by Multiple Departments");
        OutputFormatter.printInfo("Enter department names separated by commas");
        OutputFormatter.printPrompt("Department Names (e.g., 컴퓨터공학과,소프트웨어학과,전자공학과)");

        String input = scanner.nextLine().trim();
        String[] deptArray = input.split(",");
        List<String> departments = new ArrayList<>();

        for (String dept : deptArray) {
            String trimmed = dept.trim();
            if (!trimmed.isEmpty()) {
                departments.add(trimmed);
            }
        }

        if (departments.isEmpty()) {
            OutputFormatter.printError("No departments specified.");
            return;
        }

        List<Student> students = studentDAO.findStudentsByDepartments(departments);

        if (students.isEmpty()) {
            OutputFormatter.printNoData("No students found in specified departments");
            return;
        }

        OutputFormatter.printInfo("Found " + students.size() + " students in " + departments.size() + " departments:");
        displayStudentsTable(students);
    }

    /**
     * Query 7-1: Find Students Above Department Average
     */
    private void findStudentsAboveDepartmentAverage() throws SQLException {
        OutputFormatter.printSection("Query 7-1: Find Students Above Department Average");
        List<StudentDAO.StudentWithDeptAvg> results = studentDAO.findStudentsAboveDepartmentAverage();

        if (results.isEmpty()) {
            OutputFormatter.printNoData("No students found above department average age.");
            return;
        }

        OutputFormatter.printInfo("Found " + results.size() + " students:");

        String[] headers = {"Student ID", "Name", "Age", "Department", "Dept Avg Age"};
        List<String[]> rows = new ArrayList<>();

        for (StudentDAO.StudentWithDeptAvg result : results) {
            rows.add(new String[]{
                    String.valueOf(result.studentId),
                    result.name,
                    String.valueOf(result.age),
                    result.department,
                    OutputFormatter.formatDouble(result.deptAvgAge)
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    /**
     * Query 10-1: Find Active or Reporting Students (UNION)
     */
    private void findActiveOrReportingStudents() throws SQLException {
        OutputFormatter.printSection("Query 10-1: Find Active or Reporting Students (UNION)");
        List<StudentDAO.StudentActivity> results = studentDAO.findActiveOrReportingStudents();

        if (results.isEmpty()) {
            OutputFormatter.printNoData("No active or reporting students found.");
            return;
        }

        OutputFormatter.printInfo("Found " + results.size() + " students:");

        String[] headers = {"Student ID", "Name", "Department", "Activity Type"};
        List<String[]> rows = new ArrayList<>();

        for (StudentDAO.StudentActivity result : results) {
            rows.add(new String[]{
                    String.valueOf(result.studentId),
                    result.name,
                    result.department,
                    result.activityType
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    /**
     * Query 10-2: Find Message Senders Without Reports (MINUS)
     */
    private void findMessageSendersWithoutReports() throws SQLException {
        OutputFormatter.printSection("Query 10-2: Find Message Senders Without Reports (MINUS)");
        List<Student> students = studentDAO.findMessageSendersWithoutReports();

        if (students.isEmpty()) {
            OutputFormatter.printNoData("No message senders without reports found.");
            return;
        }

        OutputFormatter.printInfo("Found " + students.size() + " students:");
        displayStudentsTable(students);
    }

    // ==================== Display Helper Methods ====================

    private void displayStudentsTable(List<Student> students) {
        String[] headers = {"ID", "Number", "Name", "Age", "Gender", "MBTI", "Department", "Created"};
        List<String[]> rows = new ArrayList<>();

        for (Student s : students) {
            rows.add(new String[]{
                    String.valueOf(s.getStudentId()),
                    s.getStudentNumber(),
                    s.getName(),
                    String.valueOf(s.getAge()),
                    s.getGender(),
                    s.getMbti() != null ? s.getMbti() : "N/A",
                    OutputFormatter.truncate(s.getDepartment(), 20),
                    OutputFormatter.formatTimestamp(s.getCreatedAt())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    private void displayStudentDetails(Student s) {
        OutputFormatter.printSection("Student Details");
        OutputFormatter.printKeyValue("ID", s.getStudentId());
        OutputFormatter.printKeyValue("Student Number", s.getStudentNumber());
        OutputFormatter.printKeyValue("Name", s.getName());
        OutputFormatter.printKeyValue("Age", s.getAge());
        OutputFormatter.printKeyValue("Gender", s.getGender());
        OutputFormatter.printKeyValue("MBTI", s.getMbti());
        OutputFormatter.printKeyValue("Department", s.getDepartment());
        OutputFormatter.printKeyValue("Profile", s.getProfile() != null ? OutputFormatter.truncate(s.getProfile(), 50) : "N/A");
        OutputFormatter.printKeyValue("Created At", OutputFormatter.formatTimestamp(s.getCreatedAt()));
    }

    private void waitForEnter() {
        System.out.println();
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
}
