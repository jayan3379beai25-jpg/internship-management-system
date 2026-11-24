import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

// Internship Tracker - console app using JDBC + MySQL
// Features role-based login (Admin & User/Intern) with Back options throughout
public class Main {

    static Scanner sc = new Scanner(System.in);

    // --- Admin login — returns username on success, null on failure ---
    private static String adminLogin() {
        System.out.println("\n===== ADMIN LOGIN =====");
        System.out.print("Username (or 0 to go back): ");
        String username = sc.nextLine().trim();
        if (username.equals("0")) return null;

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        String sql = "SELECT * FROM admin_accounts WHERE username = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Login successful! Welcome, " + username + ".");
                return username;
            } else {
                System.out.println("Invalid username or password.");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("DB error: " + e.getMessage());
            System.out.println("Hint: Run DBSetup first if tables are not created yet.");
            return null;
        }
    }

    // --- Intern login — returns intern_id on success, -1 on failure ---
    private static int internLogin() {
        System.out.println("\n===== INTERN LOGIN =====");
        System.out.print("Email (or 0 to go back): ");
        String email = sc.nextLine().trim();
        if (email.equals("0")) return -1;

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        String sql = "SELECT intern_id, full_name FROM interns WHERE email = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("intern_id");
                System.out.println("Login successful! Welcome, " + rs.getString("full_name") + ".");
                return id;
            } else {
                System.out.println("Invalid email or password.");
                System.out.println("Hint: Default password for existing interns is 'intern123'");
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("DB error: " + e.getMessage());
            System.out.println("Hint: Run DBSetup first if 'password' column does not exist.");
            return -1;
        }
    }

    public static void main(String[] args) {
        int choice = -1;
        while (choice != 3) {
            System.out.println("\n==============================================");
            System.out.println("       INTERNSHIP TRACKER SYSTEM              ");
            System.out.println("==============================================");
            System.out.println("1. Admin Portal");
            System.out.println("2. User / Intern Portal");
            System.out.println("3. Exit");
            System.out.print("Select role (1-3): ");

            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            if (choice == 1) {
                String adminUser = adminLogin();
                if (adminUser != null) {
                    Admin.menu(sc, adminUser);
                }
            } else if (choice == 2) {
                int internId = internLogin();
                if (internId != -1) {
                    User.menu(sc, internId);
                }
            } else if (choice == 3) {
                System.out.println("Thank you for using Internship Tracker. Goodbye!");
            } else {
                System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
        sc.close();
    }
}
