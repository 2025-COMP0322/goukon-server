package com.university.matching.ui;

import com.university.matching.dao.MessageDAO;
import com.university.matching.model.Message;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 메시지 관리 메뉴 UI
 */
public class MessageMenuUI {
    private final Scanner scanner;
    private final MessageDAO messageDAO;

    public MessageMenuUI(Scanner scanner) {
        this.scanner = scanner;
        this.messageDAO = new MessageDAO();
    }

    public void showMenu() {
        String[] options = {
                "View All Messages",
                "Add New Message",
                "Delete Message",
                "Query 2-1: Find Messages with Sender Info",
                "Query 3-2: Get Message Statistics by Session",
                "Query 7-2: Find Students with Above Average Messages",
                "Back to Main Menu"
        };

        OutputFormatter.printMenu("Message Management", options);
        OutputFormatter.printPrompt("Select an option (1-7)");

        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1":
                    viewAllMessages();
                    break;
                case "2":
                    addNewMessage();
                    break;
                case "3":
                    deleteMessage();
                    break;
                case "4":
                    findMessagesWithSenderInfo();
                    break;
                case "5":
                    getMessageStatisticsBySession();
                    break;
                case "6":
                    findStudentsWithAboveAverageMessages();
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

    private void viewAllMessages() throws SQLException {
        OutputFormatter.printSection("All Messages");
        List<Message> messages = messageDAO.findAll();

        if (messages.isEmpty()) {
            OutputFormatter.printNoData("No messages found.");
            return;
        }

        displayMessagesTable(messages);
    }

    private void addNewMessage() throws SQLException {
        OutputFormatter.printSection("Add New Message");

        OutputFormatter.printPrompt("Session ID");
        String sessionInput = scanner.nextLine().trim();
        if (!InputValidator.isValidNumber(sessionInput)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        Long sessionId = Long.parseLong(sessionInput);

        OutputFormatter.printPrompt("Student ID");
        String studentInput = scanner.nextLine().trim();
        if (!InputValidator.isValidNumber(studentInput)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        Long studentId = Long.parseLong(studentInput);

        OutputFormatter.printPrompt("Message Content");
        String content = scanner.nextLine().trim();
        if (!InputValidator.isNotEmpty(content)) {
            OutputFormatter.printError(InputValidator.getNotEmptyErrorMessage("Content"));
            return;
        }

        Message message = new Message();
        message.setSessionId(sessionId);
        message.setStudentId(studentId);
        message.setContent(content);

        if (messageDAO.insert(message)) {
            OutputFormatter.printSuccess("Message added successfully.");
        } else {
            OutputFormatter.printError("Failed to add message.");
        }
    }

    private void deleteMessage() throws SQLException {
        OutputFormatter.printSection("Delete Message");

        OutputFormatter.printPrompt("Session ID");
        String sessionInput = scanner.nextLine().trim();
        if (!InputValidator.isValidNumber(sessionInput)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        Long sessionId = Long.parseLong(sessionInput);

        OutputFormatter.printPrompt("Student ID");
        String studentInput = scanner.nextLine().trim();
        if (!InputValidator.isValidNumber(studentInput)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        Long studentId = Long.parseLong(studentInput);

        OutputFormatter.printPrompt("Message ID");
        String messageInput = scanner.nextLine().trim();
        if (!InputValidator.isValidNumber(messageInput)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        Long messageId = Long.parseLong(messageInput);

        OutputFormatter.printConfirmPrompt("Are you sure you want to delete this message?");
        String confirm = scanner.nextLine().trim();

        if (!InputValidator.parseYesNo(confirm)) {
            OutputFormatter.printInfo("Delete cancelled.");
            return;
        }

        if (messageDAO.delete(sessionId, studentId, messageId)) {
            OutputFormatter.printSuccess("Message deleted successfully.");
        } else {
            OutputFormatter.printError("Failed to delete message.");
        }
    }

    // ==================== 동적 쿼리 ====================

    /**
     * Query 2-1: Find Messages with Sender Info
     */
    private void findMessagesWithSenderInfo() throws SQLException {
        OutputFormatter.printSection("Query 2-1: Find Messages with Sender Info");
        OutputFormatter.printPrompt("Enter Session ID (or press Enter for all sessions)");
        String input = scanner.nextLine().trim();

        Long sessionId = null;
        if (!input.isEmpty()) {
            if (!InputValidator.isValidNumber(input)) {
                OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
                return;
            }
            sessionId = Long.parseLong(input);
        }

        List<MessageDAO.MessageWithSender> messages = messageDAO.findMessagesWithSenderInfo(sessionId);

        if (messages.isEmpty()) {
            OutputFormatter.printNoData("No messages found.");
            return;
        }

        OutputFormatter.printInfo("Found " + messages.size() + " messages:");
        displayMessagesWithSenderTable(messages);
    }

    /**
     * Query 3-2: Get Message Statistics by Session
     */
    private void getMessageStatisticsBySession() throws SQLException {
        OutputFormatter.printSection("Query 3-2: Get Message Statistics by Session");
        OutputFormatter.printInfo("Find students with at least N messages per session");

        OutputFormatter.printPrompt("Minimum Message Count");
        String input = scanner.nextLine().trim();
        if (!InputValidator.isValidInteger(input)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        int minMessageCount = Integer.parseInt(input);

        List<MessageDAO.SessionMessageStatistics> stats = messageDAO.getMessageStatisticsBySession(minMessageCount);

        if (stats.isEmpty()) {
            OutputFormatter.printNoData("No statistics found with at least " + minMessageCount + " messages");
            return;
        }

        OutputFormatter.printInfo("Found " + stats.size() + " results:");

        String[] headers = {"Session ID", "Student Name", "Department", "Message Count"};
        List<String[]> rows = new ArrayList<>();

        for (MessageDAO.SessionMessageStatistics stat : stats) {
            rows.add(new String[]{
                    String.valueOf(stat.sessionId()),
                    stat.studentName(),
                    stat.department(),
                    String.valueOf(stat.messageCount())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    /**
     * Query 7-2: Find Students with Above Average Messages
     */
    private void findStudentsWithAboveAverageMessages() throws SQLException {
        OutputFormatter.printSection("Query 7-2: Find Students with Above Average Messages");
        List<MessageDAO.StudentWithMessageCount> students = messageDAO.findStudentsWithAboveAverageMessages();

        if (students.isEmpty()) {
            OutputFormatter.printNoData("No students found with above average messages.");
            return;
        }

        OutputFormatter.printInfo("Found " + students.size() + " students:");

        String[] headers = {"Student ID", "Name", "Department", "Message Count"};
        List<String[]> rows = new ArrayList<>();

        for (MessageDAO.StudentWithMessageCount student : students) {
            rows.add(new String[]{
                    String.valueOf(student.studentId()),
                    student.name(),
                    student.department(),
                    String.valueOf(student.messageCount())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    // ==================== Display Methods ====================

    private void displayMessagesTable(List<Message> messages) {
        String[] headers = {"Session ID", "Student ID", "Message ID", "Content", "Created At"};
        List<String[]> rows = new ArrayList<>();

        for (Message m : messages) {
            rows.add(new String[]{
                    String.valueOf(m.getSessionId()),
                    String.valueOf(m.getStudentId()),
                    String.valueOf(m.getMessageId()),
                    OutputFormatter.truncate(m.getContent(), 40),
                    OutputFormatter.formatTimestamp(m.getCreatedAt())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    private void displayMessagesWithSenderTable(List<MessageDAO.MessageWithSender> messages) {
        String[] headers = {"Sender Name", "Department", "Content", "Created At"};
        List<String[]> rows = new ArrayList<>();

        for (MessageDAO.MessageWithSender m : messages) {
            rows.add(new String[]{
                    m.senderName(),
                    OutputFormatter.truncate(m.senderDepartment(), 20),
                    OutputFormatter.truncate(m.content(), 40),
                    OutputFormatter.formatTimestamp(m.createdAt())
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
