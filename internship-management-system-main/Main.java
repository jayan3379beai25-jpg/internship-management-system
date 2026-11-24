import java.util.Scanner;

/**
 * INTERNSHIP MANAGEMENT SYSTEM — Merged Edition
 *
 * Features from YOUR project (Jayan):
 *   • Admin Portal: Intern CRUD, Assign Tasks, Attendance, Leave Approval, Admin Profile
 *   • Intern Portal: View Profile, Tasks, Attendance, Apply/View Leaves, Change Password
 *
 * Features from FRIEND'S project (Aarush):
 *   • Company Portal: Register/Login, Post Internships, View & Process Applications
 *   • Student Portal: Register/Login, Browse & Search Internships, Apply, Track Status, Update Profile
 *   • Admin extras  : Add Companies, View Students, Approve Applications, Generate Reports
 *
 * Database: MySQL Workbench (configure credentials in DBConnection.java)
 */
public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        // Auto-create DB + all tables on first run
        DBConnection.initDatabase();

        int choice = -1;
        while (choice != 0) {
            System.out.println("\n==============================================");
            System.out.println("       INTERNSHIP MANAGEMENT SYSTEM           ");
            System.out.println("==============================================");
            System.out.println("1. Admin Portal");
            System.out.println("2. Intern / Employee Portal");
            System.out.println("3. Student Portal (Apply for Internships)");
            System.out.println("4. Company Portal");
            System.out.println("0. Exit");
            System.out.println("==============================================");
            System.out.print("Select portal (0-4): ");

            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number (0-4).");
                continue;
            }

            switch (choice) {
                case 1:
                    Admin.menu(sc);
                    break;
                case 2:
                    internLogin();
                    break;
                case 3:
                    Student.menu(sc);
                    break;
                case 4:
                    Company.menu(sc);
                    break;
                case 0:
                    System.out.println("\nThank you for using Internship Management System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 0 to 4.");
                    break;
            }
        }
        sc.close();
    }

    // ── Intern login ────────────────────────────────────────────────────────────
    private static void internLogin() {
        System.out.println("\n===== INTERN LOGIN =====");
        System.out.print("Email (or 0 to go back): ");
        String email = sc.nextLine().trim();
        if (email.equals("0")) return;

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        String sql = "SELECT intern_id, full_name FROM interns WHERE email = ? AND password = ?";
        try (java.sql.Connection con = DBConnection.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("intern_id");
                System.out.println("Login successful! Welcome, " + rs.getString("full_name") + ".");
                InternPortal.menu(sc, id);
            } else {
                System.out.println("Invalid email or password.");
                System.out.println("Hint: Default password for existing interns is 'intern123'");
            }
        } catch (java.sql.SQLException e) {
            System.out.println("DB error: " + e.getMessage());
        }
    }
}
