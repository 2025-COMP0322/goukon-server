package com.university.matching.ui;

import com.university.matching.dao.GroupDAO;
import com.university.matching.model.Group;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 그룹 관리 메뉴 UI
 */
public class GroupMenuUI {
    private final Scanner scanner;
    private final GroupDAO groupDAO;

    public GroupMenuUI(Scanner scanner) {
        this.scanner = scanner;
        this.groupDAO = new GroupDAO();
    }

    public void showMenu() {
        String[] options = {
                "View All Groups",
                "Find Group by ID",
                "Add New Group",
                "Update Group Status",
                "Delete Group",
                "Add Student to Group",
                "Remove Student from Group",
                "Query 1-2: Find Matched Queues",
                "Query 2-2: Find Group Members from Queue",
                "Query 8-1: Find Matched Session Details",
                "Query 9-1: Get Group Statistics",
                "Query 9-2: Get Department Group Participation",
                "Back to Main Menu"
        };

        OutputFormatter.printMenu("Group Management", options);
        OutputFormatter.printPrompt("Select an option (1-13)");

        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1":
                    viewAllGroups();
                    break;
                case "2":
                    findGroupById();
                    break;
                case "3":
                    addNewGroup();
                    break;
                case "4":
                    updateGroupStatus();
                    break;
                case "5":
                    deleteGroup();
                    break;
                case "6":
                    addStudentToGroup();
                    break;
                case "7":
                    removeStudentFromGroup();
                    break;
                case "8":
                    findMatchedQueues();
                    break;
                case "9":
                    findGroupMembersFromQueue();
                    break;
                case "10":
                    findMatchedSessionDetails();
                    break;
                case "11":
                    getGroupStatistics();
                    break;
                case "12":
                    getDepartmentGroupParticipation();
                    break;
                case "13":
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

    // ==================== 기존 기능 (간소화) ====================

    private void viewAllGroups() throws SQLException {
        OutputFormatter.printSection("All Groups");
        List<Group> groups = groupDAO.findAll();
        if (groups.isEmpty()) {
            OutputFormatter.printNoData("No groups found.");
            return;
        }
        displayGroupsTable(groups);
    }

    private void findGroupById() throws SQLException {
        OutputFormatter.printSection("Find Group by ID");
        OutputFormatter.printPrompt("Enter Group ID");
        String input = scanner.nextLine().trim();

        if (!InputValidator.isValidNumber(input)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }

        Long groupId = Long.parseLong(input);
        Group group = groupDAO.findById(groupId);

        if (group == null) {
            OutputFormatter.printWarning("Group not found with ID: " + groupId);
            return;
        }

        displayGroupDetails(group);
    }

    private void addNewGroup() throws SQLException {
        OutputFormatter.printSection("Add New Group");

        OutputFormatter.printPrompt("Gender (M/F)");
        String gender = scanner.nextLine().trim().toUpperCase();
        if (!InputValidator.isValidGender(gender)) {
            OutputFormatter.printError(InputValidator.getGenderErrorMessage());
            return;
        }

        OutputFormatter.printInfo("Valid statuses: " + String.join(", ", InputValidator.getValidGroupStatuses()));
        OutputFormatter.printPrompt("Status");
        String status = scanner.nextLine().trim().toUpperCase();
        if (!InputValidator.isValidGroupStatus(status)) {
            OutputFormatter.printError(InputValidator.getGroupStatusErrorMessage());
            return;
        }

        Group group = new Group();
        group.setGender(gender);
        group.setStatus(status);

        Long id = groupDAO.insert(group);
        if (id != null) {
            OutputFormatter.printSuccess("Group created successfully with ID: " + id);
        } else {
            OutputFormatter.printError("Failed to create group.");
        }
    }

    private void updateGroupStatus() throws SQLException {
        OutputFormatter.printSection("Update Group Status");
        OutputFormatter.printPrompt("Enter Group ID");
        String input = scanner.nextLine().trim();

        if (!InputValidator.isValidNumber(input)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }

        Long groupId = Long.parseLong(input);
        Group group = groupDAO.findById(groupId);

        if (group == null) {
            OutputFormatter.printWarning("Group not found with ID: " + groupId);
            return;
        }

        displayGroupDetails(group);

        OutputFormatter.printInfo("Valid statuses: " + String.join(", ", InputValidator.getValidGroupStatuses()));
        OutputFormatter.printPrompt("New Status");
        String status = scanner.nextLine().trim().toUpperCase();
        if (!InputValidator.isValidGroupStatus(status)) {
            OutputFormatter.printError(InputValidator.getGroupStatusErrorMessage());
            return;
        }

        group.setStatus(status);
        if (groupDAO.update(group)) {
            OutputFormatter.printSuccess("Group status updated successfully.");
        } else {
            OutputFormatter.printError("Failed to update group status.");
        }
    }

    private void deleteGroup() throws SQLException {
        OutputFormatter.printSection("Delete Group");
        OutputFormatter.printPrompt("Enter Group ID to delete");
        String input = scanner.nextLine().trim();

        if (!InputValidator.isValidNumber(input)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }

        Long groupId = Long.parseLong(input);
        Group group = groupDAO.findById(groupId);

        if (group == null) {
            OutputFormatter.printWarning("Group not found with ID: " + groupId);
            return;
        }

        displayGroupDetails(group);
        OutputFormatter.printConfirmPrompt("Are you sure you want to delete this group?");
        String confirm = scanner.nextLine().trim();

        if (!InputValidator.parseYesNo(confirm)) {
            OutputFormatter.printInfo("Delete cancelled.");
            return;
        }

        if (groupDAO.delete(groupId)) {
            OutputFormatter.printSuccess("Group deleted successfully.");
        } else {
            OutputFormatter.printError("Failed to delete group.");
        }
    }

    private void addStudentToGroup() throws SQLException {
        OutputFormatter.printSection("Add Student to Group");

        OutputFormatter.printPrompt("Enter Group ID");
        String groupInput = scanner.nextLine().trim();
        if (!InputValidator.isValidNumber(groupInput)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        Long groupId = Long.parseLong(groupInput);

        OutputFormatter.printPrompt("Enter Student ID");
        String studentInput = scanner.nextLine().trim();
        if (!InputValidator.isValidNumber(studentInput)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        Long studentId = Long.parseLong(studentInput);

        if (groupDAO.addStudentToGroup(groupId, studentId)) {
            OutputFormatter.printSuccess("Student added to group successfully.");
        } else {
            OutputFormatter.printError("Failed to add student to group.");
        }
    }

    private void removeStudentFromGroup() throws SQLException {
        OutputFormatter.printSection("Remove Student from Group");

        OutputFormatter.printPrompt("Enter Group ID");
        String groupInput = scanner.nextLine().trim();
        if (!InputValidator.isValidNumber(groupInput)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        Long groupId = Long.parseLong(groupInput);

        OutputFormatter.printPrompt("Enter Student ID");
        String studentInput = scanner.nextLine().trim();
        if (!InputValidator.isValidNumber(studentInput)) {
            OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
            return;
        }
        Long studentId = Long.parseLong(studentInput);

        if (groupDAO.removeStudentFromGroup(groupId, studentId)) {
            OutputFormatter.printSuccess("Student removed from group successfully.");
        } else {
            OutputFormatter.printError("Failed to remove student from group.");
        }
    }

    // ==================== 동적 쿼리 ====================

    /**
     * Query 1-2: Find Matched Queues
     */
    private void findMatchedQueues() throws SQLException {
        OutputFormatter.printSection("Query 1-2: Find Matched Queues");
        OutputFormatter.printPrompt("Matching Status (e.g., WAITING, MATCHED, CANCELED)");
        String matchingStatus = scanner.nextLine().trim().toUpperCase();

        List<GroupDAO.MatchedQueueInfo> queues = groupDAO.findMatchedQueues(matchingStatus);

        if (queues.isEmpty()) {
            OutputFormatter.printNoData("No queues found with matching status: " + matchingStatus);
            return;
        }

        OutputFormatter.printInfo("Found " + queues.size() + " queues:");

        String[] headers = {"Queue ID", "Matching Type", "Matching Status", "Created At"};
        List<String[]> rows = new ArrayList<>();

        for (GroupDAO.MatchedQueueInfo queue : queues) {
            rows.add(new String[]{
                    String.valueOf(queue.queueId()),
                    queue.matchingType(),
                    queue.matchingStatus(),
                    OutputFormatter.formatTimestamp(queue.createdAt())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    /**
     * Query 2-2: Find Group Members from Queue
     */
    private void findGroupMembersFromQueue() throws SQLException {
        OutputFormatter.printSection("Query 2-2: Find Group Members from Queue");
        OutputFormatter.printPrompt("Enter Queue ID (or press Enter for all queues)");
        String input = scanner.nextLine().trim();

        Long queueId = null;
        if (!input.isEmpty()) {
            if (!InputValidator.isValidNumber(input)) {
                OutputFormatter.printError(InputValidator.getInvalidNumberErrorMessage());
                return;
            }
            queueId = Long.parseLong(input);
        }

        List<GroupDAO.GroupMemberFromQueue> members = groupDAO.findGroupMembersFromQueue(queueId);

        if (members.isEmpty()) {
            OutputFormatter.printNoData("No group members found.");
            return;
        }

        OutputFormatter.printInfo("Found " + members.size() + " members:");

        String[] headers = {"Queue ID", "Matching Type", "Group ID", "Student ID", "Name", "Department"};
        List<String[]> rows = new ArrayList<>();

        for (GroupDAO.GroupMemberFromQueue member : members) {
            rows.add(new String[]{
                    String.valueOf(member.queueId()),
                    member.matchingType(),
                    String.valueOf(member.groupId()),
                    String.valueOf(member.studentId()),
                    member.studentName(),
                    member.department()
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    /**
     * Query 8-1: Find Matched Session Details
     */
    private void findMatchedSessionDetails() throws SQLException {
        OutputFormatter.printSection("Query 8-1: Find Matched Session Details");
        OutputFormatter.printPrompt("Enter Matching Status (or press Enter for all)");
        String input = scanner.nextLine().trim().toUpperCase();

        String matchingStatus = input.isEmpty() ? null : input;

        List<GroupDAO.MatchedSessionDetail> details = groupDAO.findMatchedSessionDetails(matchingStatus);

        if (details.isEmpty()) {
            OutputFormatter.printNoData("No matched session details found.");
            return;
        }

        OutputFormatter.printInfo("Found " + details.size() + " session details:");

        String[] headers = {"Session ID", "Session Status", "Group ID", "Gender", "Matching Type", "Matching Status", "Created At"};
        List<String[]> rows = new ArrayList<>();

        for (GroupDAO.MatchedSessionDetail detail : details) {
            rows.add(new String[]{
                    String.valueOf(detail.sessionId()),
                    detail.sessionStatus(),
                    String.valueOf(detail.groupId()),
                    detail.groupGender(),
                    detail.matchingType(),
                    detail.matchingStatus(),
                    OutputFormatter.formatTimestamp(detail.createdAt())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    /**
     * Query 9-1: Get Group Statistics
     */
    private void getGroupStatistics() throws SQLException {
        OutputFormatter.printSection("Query 9-1: Get Group Statistics");
        List<GroupDAO.GroupStatistics> stats = groupDAO.getGroupStatistics();

        if (stats.isEmpty()) {
            OutputFormatter.printNoData("No group statistics found.");
            return;
        }

        OutputFormatter.printInfo("Found statistics for " + stats.size() + " groups:");

        String[] headers = {"Group ID", "Gender", "Members", "Avg Age", "Created At"};
        List<String[]> rows = new ArrayList<>();

        for (GroupDAO.GroupStatistics stat : stats) {
            rows.add(new String[]{
                    String.valueOf(stat.groupId()),
                    stat.gender(),
                    String.valueOf(stat.memberCount()),
                    OutputFormatter.formatDouble(stat.avgAge()),
                    OutputFormatter.formatTimestamp(stat.createdAt())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    /**
     * Query 9-2: Get Department Group Participation
     */
    private void getDepartmentGroupParticipation() throws SQLException {
        OutputFormatter.printSection("Query 9-2: Get Department Group Participation");
        List<GroupDAO.DepartmentGroupParticipation> participations = groupDAO.getDepartmentGroupParticipation();

        if (participations.isEmpty()) {
            OutputFormatter.printNoData("No department participation statistics found.");
            return;
        }

        OutputFormatter.printInfo("Found statistics for " + participations.size() + " departments:");

        String[] headers = {"Department", "Participating Students", "Total Sessions", "Matched Sessions"};
        List<String[]> rows = new ArrayList<>();

        for (GroupDAO.DepartmentGroupParticipation participation : participations) {
            rows.add(new String[]{
                    participation.department(),
                    String.valueOf(participation.participatingStudents()),
                    String.valueOf(participation.totalSessions()),
                    String.valueOf(participation.matchedSessions())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    // ==================== Display Methods ====================

    private void displayGroupsTable(List<Group> groups) {
        String[] headers = {"Group ID", "Gender", "Status", "Created At"};
        List<String[]> rows = new ArrayList<>();

        for (Group g : groups) {
            rows.add(new String[]{
                    String.valueOf(g.getGroupId()),
                    g.getGender(),
                    g.getStatus(),
                    OutputFormatter.formatTimestamp(g.getCreatedAt())
            });
        }

        OutputFormatter.printTable(headers, rows);
    }

    private void displayGroupDetails(Group g) {
        OutputFormatter.printSection("Group Details");
        OutputFormatter.printKeyValue("Group ID", g.getGroupId());
        OutputFormatter.printKeyValue("Gender", g.getGender());
        OutputFormatter.printKeyValue("Status", g.getStatus());
        OutputFormatter.printKeyValue("Created At", OutputFormatter.formatTimestamp(g.getCreatedAt()));
    }

    private void waitForEnter() {
        System.out.println();
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
}
