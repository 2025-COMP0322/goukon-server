package com.university.matching.ui;

import com.university.matching.dao.ReportDAO;
import com.university.matching.model.Report;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 신고 관리 메뉴 UI
 */
public class ReportMenuUI {
    private final Scanner scanner;
    private final ReportDAO reportDAO;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ReportMenuUI(Scanner scanner) {
        this.scanner = scanner;
        this.reportDAO = new ReportDAO();
    }

    public void showMenu() {
        String[] options = {
                "View All Reports",
                "Find Report by ID",
                "Add New Report",
                "Update Report Status",
                "Delete Report",
                "Query 8-2: View Reports with Student Info",
                "Back to Main Menu"
        };

        OutputFormatter.printMenu("Report Management", options);
        OutputFormatter.printPrompt("Select an option (1-7)");

        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1":
                    viewAllReports();
                    break;
                case "2":
                    findReportById();
                    break;
                case "3":
                    addNewReport();
                    break;
                case "4":
                    updateReportStatus();
                    break;
                case "5":
                    deleteReport();
                    break;
                case "6":
                    viewReportsWithStudentInfo();
                    break;
                case "7":
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

    private void viewAllReports() throws SQLException {
        OutputFormatter.printSection("All Reports");
        List<Report> reports = reportDAO.findAll();

        if (reports.isEmpty()) {
            OutputFormatter.printNoData("No reports found.");
            return;
        }

        displayReportsTable(reports);
    }

    private void findReportById() throws SQLException {
        OutputFormatter.printSection("Find Report by ID");
        OutputFormatter.printPrompt("Enter Report ID");
        String input = scanner.nextLine().trim();

        if (!InputValidator.isValidNumber(input)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }

        Long reportId = Long.parseLong(input);
        Report report = reportDAO.findById(reportId);

        if (report == null) {
            OutputFormatter.printWarning("Report not found with ID: " + reportId);
            return;
        }

        displayReportDetails(report);
    }

    private void addNewReport() throws SQLException {
        OutputFormatter.printSection("Add New Report");

        OutputFormatter.printPrompt("Student ID");
        String studentInput = scanner.nextLine().trim();
        if (!InputValidator.isValidNumber(studentInput)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        Long studentId = Long.parseLong(studentInput);

        OutputFormatter.printPrompt("Report Title");
        String title = scanner.nextLine().trim();
        if (!InputValidator.isNotEmpty(title)) {
            OutputFormatter.printError(InputValidator.getNotEmptyErrorMessage("Title"));
            return;
        }

        OutputFormatter.printPrompt("Report Content");
        String content = scanner.nextLine().trim();
        if (!InputValidator.isNotEmpty(content)) {
            OutputFormatter.printError(InputValidator.getNotEmptyErrorMessage("Content"));
            return;
        }

        OutputFormatter.printInfo("Valid statuses: " + String.join(", ", InputValidator.getValidReportStatuses()));
        OutputFormatter.printPrompt("Status (default: PENDING)");
        String status = scanner.nextLine().trim().toUpperCase();
        if (status.isEmpty()) {
            status = "PENDING";
        } else if (!InputValidator.isValidReportStatus(status)) {
            OutputFormatter.printError(InputValidator.getReportStatusErrorMessage());
            return;
        }

        Report report = new Report();
        report.setStudentId(studentId);
        report.setTitle(title);
        report.setContent(content);
        report.setStatus(status);

        Long id = reportDAO.insert(report);
        if (id != null) {
            OutputFormatter.printSuccess("Report created successfully with ID: " + id);
        } else {
            OutputFormatter.printError("Failed to create report.");
        }
    }

    private void updateReportStatus() throws SQLException {
        OutputFormatter.printSection("Update Report Status");
        OutputFormatter.printPrompt("Enter Report ID");
        String input = scanner.nextLine().trim();

        if (!InputValidator.isValidNumber(input)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }

        Long reportId = Long.parseLong(input);
        Report report = reportDAO.findById(reportId);

        if (report == null) {
            OutputFormatter.printWarning("Report not found with ID: " + reportId);
            return;
        }

        displayReportDetails(report);

        OutputFormatter.printInfo("Valid statuses: " + String.join(", ", InputValidator.getValidReportStatuses()));
        OutputFormatter.printPrompt("New Status");
        String status = scanner.nextLine().trim().toUpperCase();
        if (!InputValidator.isValidReportStatus(status)) {
            OutputFormatter.printError(InputValidator.getReportStatusErrorMessage());
            return;
        }

        if (reportDAO.updateStatus(reportId, status)) {
            OutputFormatter.printSuccess("Report status updated successfully.");
        } else {
            OutputFormatter.printError("Failed to update report status.");
        }
    }

    private void deleteReport() throws SQLException {
        OutputFormatter.printSection("Delete Report");
        OutputFormatter.printPrompt("Enter Report ID to delete");
        String input = scanner.nextLine().trim();

        if (!InputValidator.isValidNumber(input)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }

        Long reportId = Long.parseLong(input);
        Report report = reportDAO.findById(reportId);

        if (report == null) {
            OutputFormatter.printWarning("Report not found with ID: " + reportId);
            return;
        }

        displayReportDetails(report);
        OutputFormatter.printConfirmPrompt("Are you sure you want to delete this report?");
        String confirm = scanner.nextLine().trim();

        if (!InputValidator.parseYesNo(confirm)) {
            OutputFormatter.printInfo("Delete cancelled.");
            return;
        }

        if (reportDAO.delete(reportId)) {
            OutputFormatter.printSuccess("Report deleted successfully.");
        } else {
            OutputFormatter.printError("Failed to delete report.");
        }
    }

    // ==================== 동적 쿼리 ====================

    /**
     * Query 8-2: View Reports with Student Info
     */
    private void viewReportsWithStudentInfo() throws SQLException {
        OutputFormatter.printSection("Query 8-2: View Reports with Student Info");
        List<ReportDAO.ReportWithStudentInfo> reports = reportDAO.findReportsWithStudentInfo();

        if (reports.isEmpty()) {
            OutputFormatter.printNoData("No reports found.");
            return;
        }

        OutputFormatter.printInfo("Found " + reports.size() + " reports:");
        displayReportsWithStudentInfoTable(reports);
    }

    // ==================== Display Methods ====================

    private void displayReportsTable(List<Report> reports) {
        String[] headers = {"Report ID", "Student ID", "Title", "Status", "Created At"};
        List<String[]> rows = new ArrayList<>();

        for (Report r : reports) {
            rows.add(new String[]{
                    String.valueOf(r.getReportId()),
                    String.valueOf(r.getStudentId()),
                    OutputFormatter.truncate(r.getTitle(), 30),
                    r.getStatus(),
                    OutputFormatter.formatTimestamp(r.getCreatedAt())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    private void displayReportDetails(Report r) {
        OutputFormatter.printSection("Report Details");
        OutputFormatter.printKeyValue("Report ID", r.getReportId());
        OutputFormatter.printKeyValue("Student ID", r.getStudentId());
        OutputFormatter.printKeyValue("Title", r.getTitle());
        OutputFormatter.printKeyValue("Content", OutputFormatter.truncate(r.getContent(), 100));
        OutputFormatter.printKeyValue("Status", r.getStatus());
        OutputFormatter.printKeyValue("Created At", OutputFormatter.formatTimestamp(r.getCreatedAt()));
    }

    private void displayReportsWithStudentInfoTable(List<ReportDAO.ReportWithStudentInfo> reports) {
        String[] headers = {"Report ID", "Title", "Student ID", "Reporter", "Department", "Status", "Created"};
        List<String[]> rows = new ArrayList<>();

        for (ReportDAO.ReportWithStudentInfo r : reports) {
            rows.add(new String[]{
                    String.valueOf(r.reportId()),
                    OutputFormatter.truncate(r.title(), 25),
                    String.valueOf(r.studentId()),
                    r.reporterName(),
                    OutputFormatter.truncate(r.reporterDept(), 15),
                    r.status(),
                    OutputFormatter.formatTimestamp(r.createdAt())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    private void waitForEnter() {
        System.out.println();
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
}
