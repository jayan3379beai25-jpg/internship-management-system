import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * InternPortal — the portal for Interns who are staff/employees (from YOUR project).
 * Login is done through Main.java (email + password).
 *
 * Features: View Profile, Tasks, Attendance, Apply/View Leave, Change Password.
 */
public class InternPortal {

    public static void menu(Scanner sc, int internId) {
        String internName = Intern.getInternName(internId);
        if (internName == null) {
            System.out.println("Session error. Returning to Main Menu.");
            return;
        }

        System.out.println("\nWelcome, " + internName + "!");

        int choice = -1;
        while (choice != 0) {
            System.out.println("\n=================================");
            System.out.println("  INTERN PORTAL (" + internName + ")");
            System.out.println("=================================");
            System.out.println("1. View My Profile");
            System.out.println("2. View My Assigned Tasks");
            System.out.println("3. Update My Task Status");
            System.out.println("4. View My Attendance Summary");
            System.out.println("5. Apply for Leave");
            System.out.println("6. View My Leave History");
            System.out.println("7. Account Settings");
            System.out.println("0. Logout");
            System.out.print("Enter choice (0-7): ");

            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1: Intern.viewMyProfile(internId);           promptBack(sc); break;
                case 2: Task.viewTasksByInternId(internId);       promptBack(sc); break;
                case 3: updateUserTask(sc, internId);             promptBack(sc); break;
                case 4: Attendance.viewMyAttendance(internId);    promptBack(sc); break;
                case 5: Leave.applyLeave(sc, internId);           promptBack(sc); break;
                case 6: Leave.viewMyLeaves(internId);             promptBack(sc); break;
                case 7: accountSettings(sc, internId);            promptBack(sc); break;
                case 0: System.out.println("Logged out. Returning to Main Menu..."); break;
                default: System.out.println("Invalid choice. Please choose 0 to 7.");
            }
        }
    }

    // ── Account Settings ─────────────────────────────────────────────────────────
    private static void accountSettings(Scanner sc, int internId) {
        int choice = -1;
        while (choice != 0) {
            System.out.println("\n===== MY PROFILE & SETTINGS =====");
            System.out.println("1. View My Details");
            System.out.println("2. Change Password");
            System.out.println("0. Back");
            System.out.print("Choice: ");

            String input = sc.nextLine().trim();
            try { choice = Integer.parseInt(input); }
            catch (NumberFormatException e) { System.out.println("Invalid input."); continue; }

            switch (choice) {
                case 1: viewInternDetails(internId); break;
                case 2: changeInternPassword(sc, internId); break;
                case 0: break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewInternDetails(int internId) {
        String sql = "SELECT i.intern_id, i.full_name, i.email, i.phone, i.college, " +
                "i.join_date, i.status, d.dept_name, m.mentor_name " +
                "FROM interns i " +
                "LEFT JOIN departments d ON i.dept_id  = d.dept_id " +
                "LEFT JOIN mentors m     ON i.mentor_id = m.mentor_id " +
                "WHERE i.intern_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("\n╔══════════════════════════════════════════╗");
                System.out.println("║             INTERN PROFILE               ║");
                System.out.println("╠══════════════════════════════════════════╣");
                System.out.printf ("║  %-12s : %-25s ║%n", "Intern ID",   rs.getInt("intern_id"));
                System.out.printf ("║  %-12s : %-25s ║%n", "Full Name",   rs.getString("full_name"));
                System.out.printf ("║  %-12s : %-25s ║%n", "Email",       rs.getString("email"));
                System.out.printf ("║  %-12s : %-25s ║%n", "Phone",       rs.getString("phone"));
                System.out.printf ("║  %-12s : %-25s ║%n", "College",     rs.getString("college"));
                System.out.printf ("║  %-12s : %-25s ║%n", "Join Date",   rs.getDate("join_date"));
                System.out.printf ("║  %-12s : %-25s ║%n", "Status",      rs.getString("status"));
                System.out.printf ("║  %-12s : %-25s ║%n", "Department",  rs.getString("dept_name"));
                System.out.printf ("║  %-12s : %-25s ║%n", "Mentor",      rs.getString("mentor_name"));
                System.out.println("╚══════════════════════════════════════════╝");
            } else {
                System.out.println("Profile not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void changeInternPassword(Scanner sc, int internId) {
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

        String sql = "UPDATE interns SET password = ? WHERE intern_id = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPass);
            ps.setInt(2, internId);
            ps.setString(3, oldPass);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("Password changed successfully!");
            else          System.out.println("Wrong current password. Password not changed.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ── Task Update (Intern side) ─────────────────────────────────────────────────
    private static void updateUserTask(Scanner sc, int internId) {
        Task.viewTasksByInternId(internId);
        System.out.print("Enter Task ID to update (or 0 to go back): ");
        String idStr = sc.nextLine().trim();
        if (idStr.equals("0")) { System.out.println("Returning to menu..."); return; }

        int taskId;
        try { taskId = Integer.parseInt(idStr); }
        catch (NumberFormatException e) { System.out.println("Invalid Task ID."); return; }

        System.out.print("New status (Pending/In Progress/Done) (or 0 to go back): ");
        String status = sc.nextLine().trim();
        if (status.equals("0")) { System.out.println("Returning to menu..."); return; }

        boolean updated = Task.updateTaskStatusDirect(internId, taskId, status);
        if (updated) System.out.println("Task status updated successfully!");
        else         System.out.println("Could not update. Task ID " + taskId + " may not belong to you.");
    }

    private static void promptBack(Scanner sc) {
        System.out.print("\nPress Enter to go back to Intern menu...");
        sc.nextLine();
    }
}
