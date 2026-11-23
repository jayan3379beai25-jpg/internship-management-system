import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Admin {

    // Admin Portal Menu Loop — loggedInAdmin is the current session username
    public static void menu(Scanner sc, String loggedInAdmin) {
        // Use array so inner methods can update the session username
        String[] sessionUser = { loggedInAdmin };

        int choice = -1;
        while (choice != 0) {
            System.out.println("\n=================================");
            System.out.println("    ADMIN PORTAL (" + sessionUser[0] + ")");
            System.out.println("=================================");
            System.out.println("1.  Add Intern");
            System.out.println("2.  View All Interns");
            System.out.println("3.  Search Intern by Name");
            System.out.println("4.  Update Intern Status");
            System.out.println("5.  Delete Intern");
            System.out.println("6.  Assign Task");
            System.out.println("7.  View Tasks of an Intern");
            System.out.println("8.  Update Task Status");
            System.out.println("9.  Mark Attendance (Today)");
            System.out.println("10. Attendance Report");
            System.out.println("11. Approve / Reject Leave");
            System.out.println("12. My Profile");
            System.out.println("0.  Logout");
            System.out.print("Enter choice (0-12): ");

            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1:
                    Intern.addIntern(sc);
                    promptBack(sc);
                    break;
                case 2:
                    Intern.viewInterns();
                    promptBack(sc);
                    break;
                case 3:
                    Intern.searchIntern(sc);
                    promptBack(sc);
                    break;
                case 4:
                    Intern.updateInternStatus(sc);
                    promptBack(sc);
                    break;
                case 5:
                    Intern.deleteIntern(sc);
                    promptBack(sc);
                    break;
                case 6:
                    Task.assignTask(sc);
                    promptBack(sc);
                    break;
                case 7:
                    Task.viewTasks(sc);
                    promptBack(sc);
                    break;
                case 8:
                    Task.updateTaskStatus(sc);
                    promptBack(sc);
                    break;
                case 9:
                    Attendance.markAttendance(sc);
                    promptBack(sc);
                    break;
                case 10:
                    Attendance.attendanceReport();
                    promptBack(sc);
                    break;
                case 11:
                    Leave.approveRejectLeave(sc);
                    promptBack(sc);
                    break;
                case 12:
                    adminProfile(sc, sessionUser);
                    promptBack(sc);
                    break;
                case 0:
                    System.out.println("Logging out... Returning to Main Menu.");
                    break;
                default:
                    System.out.println("Invalid choice. Please choose 0 to 12.");
                    break;
            }
        }
    }

    // ── Admin Profile ──────────────────────────────────────────────────────────
    private static void adminProfile(Scanner sc, String[] sessionUser) {
        int choice = -1;
        while (choice != 0) {
            System.out.println("\n===== MY PROFILE =====");
            System.out.println("1. View My Details");
            System.out.println("2. Change Username");
            System.out.println("3. Change Password");
            System.out.println("0. Back");
            System.out.print("Choice: ");

            String input = sc.nextLine().trim();
            try { choice = Integer.parseInt(input); }
            catch (NumberFormatException e) { System.out.println("Invalid input."); continue; }

            switch (choice) {
                case 1: viewAdminDetails(sessionUser[0]); break;
                case 2: changeAdminUsername(sc, sessionUser); break;
                case 3: changeAdminPassword(sc, sessionUser[0]); break;
                case 0: break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewAdminDetails(String username) {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║        ADMIN PROFILE         ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.printf ("║  %-10s : %-15s ║%n", "Role", "Administrator");
        System.out.printf ("║  %-10s : %-15s ║%n", "Username", username);
        System.out.printf ("║  %-10s : %-15s ║%n", "Access", "Full System");
        System.out.println("╚══════════════════════════════╝");
        System.out.println("\nPermissions:");
        System.out.println("  ✓ Add / Delete Interns");
        System.out.println("  ✓ Assign & Manage Tasks");
        System.out.println("  ✓ Mark Attendance");
        System.out.println("  ✓ Approve / Reject Leaves");
        System.out.println("  ✓ View All Reports");
    }

    private static void changeAdminUsername(Scanner sc, String[] sessionUser) {
        System.out.print("New username (or 0 to go back): ");
        String newUsername = sc.nextLine().trim();
        if (newUsername.equals("0")) return;

        System.out.print("Confirm current password: ");
        String password = sc.nextLine().trim();

        String sql = "UPDATE admin_accounts SET username = ? WHERE username = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setString(2, sessionUser[0]);
            ps.setString(3, password);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Username updated to '" + newUsername + "' successfully!");
                sessionUser[0] = newUsername; // update live session
            } else {
                System.out.println("Wrong password. Username not changed.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void changeAdminPassword(Scanner sc, String username) {
        System.out.print("Current password (or 0 to go back): ");
        String oldPass = sc.nextLine().trim();
        if (oldPass.equals("0")) return;

        System.out.print("New password: ");
        String newPass = sc.nextLine().trim();

        System.out.print("Confirm new password: ");
        String confirmPass = sc.nextLine().trim();

        if (!newPass.equals(confirmPass)) {
            System.out.println("Passwords do not match. Try again.");
            return;
        }

        String sql = "UPDATE admin_accounts SET password = ? WHERE username = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPass);
            ps.setString(2, username);
            ps.setString(3, oldPass);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Password changed successfully!");
            } else {
                System.out.println("Wrong current password. Password not changed.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Helper
    private static void promptBack(Scanner sc) {
        System.out.print("\nPress Enter to go back to Admin menu...");
        sc.nextLine();
    }
}
