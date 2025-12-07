package com.university.matching.ui;

import java.util.Scanner;

/**
 * University Matching System - Console UI (Refactored)
 * 메인 메뉴만 담당하고, 각 도메인별 메뉴는 분리된 UI 클래스에 위임합니다
 */
public class ConsoleUI {
    private final Scanner scanner;
    private final StudentMenuUI studentMenuUI;
    private final GroupMenuUI groupMenuUI;
    private final MessageMenuUI messageMenuUI;
    private final ReportMenuUI reportMenuUI;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.studentMenuUI = new StudentMenuUI(scanner);
        this.groupMenuUI = new GroupMenuUI(scanner);
        this.messageMenuUI = new MessageMenuUI(scanner);
        this.reportMenuUI = new ReportMenuUI(scanner);
    }

    /**
     * 애플리케이션 시작
     */
    public void start() {
        printWelcome();

        boolean running = true;
        while (running) {
            showMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    studentMenuUI.showMenu();
                    break;
                case "2":
                    groupMenuUI.showMenu();
                    break;
                case "3":
                    messageMenuUI.showMenu();
                    break;
                case "4":
                    reportMenuUI.showMenu();
                    break;
                case "5":
                    showDynamicQueriesInfo();
                    break;
                case "0":
                    running = false;
                    printGoodbye();
                    break;
                default:
                    OutputFormatter.printWarning("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    /**
     * 메인 메뉴 표시
     */
    private void showMainMenu() {
        System.out.println();
        OutputFormatter.printTitle("UNIVERSITY MATCHING SYSTEM - MAIN MENU");

        String[] options = {
                "Student Management",
                "Group Management",
                "Message Management",
                "Report Management",
                "📖 View Dynamic Queries Guide",
                "Exit"
        };

        OutputFormatter.printMenu("Main Menu", options);
        OutputFormatter.printInfo("📊 20 Dynamic Queries (11+5+3+1 from Team2-Phase2-3.sql)");
        OutputFormatter.printPrompt("Select an option (1-5, 0 to exit)");
    }

    private void printWelcome() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                University Matching System - Phase 3");
        System.out.println("            📊 20 Dynamic Queries from Team2-Phase2-3.sql");
        System.out.println("=".repeat(80) + "\n");
    }

    private void printGoodbye() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("              Thank you for using University Matching System!");
        System.out.println("                    ✨ 20 Dynamic Queries Implemented ✨");
        System.out.println("=".repeat(80) + "\n");
    }

    private void showDynamicQueriesInfo() {
        OutputFormatter.printTitle("20 DYNAMIC QUERIES - Team2-Phase2-3.sql");

        System.out.println("📊 Total: 20 Dynamic Queries from Team2-Phase2-3.sql");
        System.out.println();

        System.out.println("🔷 StudentDAO (11 queries):");
        System.out.println("   Query 1-1: Find Students by Gender and Age");
        System.out.println("   Query 3-1: Get Department Statistics");
        System.out.println("   Query 4-1: Find Students Below Average Age");
        System.out.println("   Query 4-2: Find Students from Top Group Department");
        System.out.println("   Query 5-1: Find Active Students (who sent messages)");
        System.out.println("   Query 5-2: Find Reporting Students");
        System.out.println("   Query 6-1: Find Students by MBTI");
        System.out.println("   Query 6-2: Find Students by Multiple Departments");
        System.out.println("   Query 7-1: Find Students Above Department Average");
        System.out.println("   Query 10-1: Find Active or Reporting Students (UNION)");
        System.out.println("   Query 10-2: Find Message Senders Without Reports (MINUS)");
        System.out.println();

        System.out.println("🔷 GroupDAO (5 queries):");
        System.out.println("   Query 1-2: Find Matched Queues");
        System.out.println("   Query 2-2: Find Group Members from Queue");
        System.out.println("   Query 8-1: Find Matched Session Details");
        System.out.println("   Query 9-1: Get Group Statistics");
        System.out.println("   Query 9-2: Get Department Group Participation");
        System.out.println();

        System.out.println("🔷 MessageDAO (3 queries):");
        System.out.println("   Query 2-1: Find Messages with Sender Info");
        System.out.println("   Query 3-2: Get Message Statistics by Session");
        System.out.println("   Query 7-2: Find Students with Above Average Messages");
        System.out.println();

        System.out.println("🔷 ReportDAO (1 query):");
        System.out.println("   Query 8-2: View Reports with Student Info");
        System.out.println();

        System.out.println("📖 Each query is numbered and referenced from Team2-Phase2-3.sql");
        System.out.println();

        waitForEnter();
    }

    private void waitForEnter() {
        System.out.println();
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
}
