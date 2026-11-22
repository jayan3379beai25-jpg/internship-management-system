import java.util.Scanner;

public class User {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        menu(sc);
        sc.close();
    }

    // User / Intern Portal Menu Loop
    public static void menu(Scanner sc) {
        System.out.println("\n=================================");
        System.out.println("      USER / INTERN LOGIN        ");
        System.out.println("=================================");
        System.out.print("Enter your Intern ID (or 0 to go back): ");
        String idStr = sc.nextLine().trim();

        if (idStr.equals("0") || idStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to Main Menu...");
            return;
        }

        int internId;
        try {
            internId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID. Returning to Main Menu.");
            return;
        }

        String internName = Intern.getInternName(internId);
        if (internName == null) {
            System.out.println("No intern found with ID: " + internId + ". Returning to Main Menu.");
            return;
        }

        System.out.println("\nWelcome, " + internName + "!");

        int choice = -1;
        while (choice != 0) {
            System.out.println("\n=================================");
            System.out.println("  INTERN PORTAL (" + internName + ")  ");
            System.out.println("=================================");
            System.out.println("1. View My Profile");
            System.out.println("2. View My Assigned Tasks");
            System.out.println("3. Update My Task Status");
            System.out.println("4. Mark My Attendance (Today)");
            System.out.println("5. View My Attendance Summary");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter choice (0-5): ");

            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1:
                    Intern.viewMyProfile(internId);
                    promptBack(sc);
                    break;
                case 2:
                    Task.viewTasksByInternId(internId);
                    promptBack(sc);
                    break;
                case 3:
                    updateUserTask(sc, internId);
                    promptBack(sc);
                    break;
                case 4:
                    markUserAttendance(sc, internId);
                    promptBack(sc);
                    break;
                case 5:
                    Attendance.viewMyAttendance(internId);
                    promptBack(sc);
                    break;
                case 0:
                    System.out.println("Logging out and returning to Main Menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please choose 0 to 5.");
                    break;
            }
        }
    }

    private static void updateUserTask(Scanner sc, int internId) {
        Task.viewTasksByInternId(internId);
        System.out.print("Enter Task ID to update (or 0 to go back): ");
        String idStr = sc.nextLine().trim();
        if (idStr.equals("0") || idStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        int taskId;
        try {
            taskId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Task ID.");
            return;
        }

        System.out.print("New status (Pending/In Progress/Done) (or 0 to go back): ");
        String status = sc.nextLine().trim();
        if (status.equals("0") || status.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        boolean updated = Task.updateTaskStatusDirect(internId, taskId, status);
        if (updated) {
            System.out.println("Task status updated successfully!");
        } else {
            System.out.println("Could not update task. Please verify that Task ID " + taskId + " belongs to you.");
        }
    }

    private static void markUserAttendance(Scanner sc, int internId) {
        System.out.print("Are you present today? (y/n, or 0 to go back): ");
        String ans = sc.nextLine().trim();
        if (ans.equals("0") || ans.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        boolean present = ans.equalsIgnoreCase("y");
        if (Attendance.markAttendanceDirect(internId, present)) {
            System.out.println("Attendance marked successfully for today!");
        }
    }

    // Helper: allows user to review the result and choose when to go back
    private static void promptBack(Scanner sc) {
        System.out.print("\nPress Enter to go back to Intern menu...");
        sc.nextLine();
    }
}
