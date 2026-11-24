import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Admin Portal — combines features from BOTH projects:
 *
 * From YOUR project  : Intern CRUD, Task management, Attendance, Leave approval, Admin Profile
 * From FRIEND'S project: Add Companies, View Students, Manage Internship Applications, Reports
 */
public class Admin {

    private static boolean isLoggedIn = false;
    private static String loggedInAdmin = null;

    public static void menu(Scanner sc) {
        if (!ensureLoggedIn(sc)) return;

        int choice = -1;
        while (choice != 0) {
            System.out.println("\n==============================================");
            System.out.println("    ADMIN PORTAL (Logged in as: " + loggedInAdmin + ")");
            System.out.println("==============================================");

            System.out.println("--- INTERN MANAGEMENT ---");
            System.out.println("1.  Add Intern");
            System.out.println("2.  View All Interns");
            System.out.println("3.  Search Intern by Name");
            System.out.println("4.  Update Intern Status");
            System.out.println("5.  Delete Intern");

            System.out.println("--- TASK MANAGEMENT ---");
            System.out.println("6.  Assign Task to Intern");
            System.out.println("7.  View Tasks of an Intern");
            System.out.println("8.  Update Task Status");

            System.out.println("--- ATTENDANCE & LEAVE ---");
            System.out.println("9.  Mark Attendance (Today)");
            System.out.println("10. Attendance Report");
            System.out.println("11. Approve / Reject Leave");

            System.out.println("--- COMPANY & STUDENT MODULE ---");
            System.out.println("12. Add New Company");
            System.out.println("13. View Registered Students");
            System.out.println("14. View All Internship Listings");
            System.out.println("15. Approve / Reject Internship Applications");
            System.out.println("16. Generate System Report");

            System.out.println("--- PROFILE ---");
            System.out.println("17. My Admin Profile");
            System.out.println("0.  Logout");
            System.out.println("==============================================");
            System.out.print("Enter choice (0-17): ");

            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (choice) {
                // --- Intern ---
                case 1:  Intern.addIntern(sc);           promptBack(sc); break;
                case 2:  Intern.viewInterns();            promptBack(sc); break;
                case 3:  Intern.searchIntern(sc);         promptBack(sc); break;
                case 4:  Intern.updateInternStatus(sc);   promptBack(sc); break;
                case 5:  Intern.deleteIntern(sc);         promptBack(sc); break;
                // --- Task ---
                case 6:  Task.assignTask(sc);             promptBack(sc); break;
                case 7:  Task.viewTasks(sc);              promptBack(sc); break;
                case 8:  Task.updateTaskStatus(sc);       promptBack(sc); break;
                // --- Attendance & Leave ---
                case 9:  Attendance.markAttendance(sc);   promptBack(sc); break;
                case 10: Attendance.attendanceReport();   promptBack(sc); break;
                case 11: Leave.approveRejectLeave(sc);    promptBack(sc); break;
                // --- Company / Student module ---
                case 12: addNewCompany(sc);               promptBack(sc); break;
                case 13: viewRegisteredStudents();         promptBack(sc); break;
                case 14: viewAllInternships();             promptBack(sc); break;
                case 15: approveRejectApplications(sc);   promptBack(sc); break;
                case 16: generateReports();                promptBack(sc); break;
                // --- Profile ---
                case 17: adminProfile(sc);                promptBack(sc); break;
                case 0:
                    isLoggedIn = false;
                    loggedInAdmin = null;
                    System.out.println("Logged out. Returning to Main Menu.");
                    break;
                default:
                    System.out.println("Invalid choice. Please choose 0 to 17.");
            }
        }
    }

    // ── Login ────────────────────────────────────────────────────────────────────
    private static boolean ensureLoggedIn(Scanner sc) {
        if (isLoggedIn) return true;

        System.out.println("\n===== ADMIN LOGIN =====");
        System.out.print("Username (or 0 to go back): ");
        String username = sc.nextLine().trim();
        if (username.equals("0")) return false;

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        // DB check
        String sql = "SELECT username FROM admin_accounts WHERE username = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                isLoggedIn = true;
                loggedInAdmin = rs.getString("username");
                System.out.println("[✓] Login successful! Welcome, " + loggedInAdmin + ".");
                return true;
            }
        } catch (SQLException e) {
            // Fallback to default if DB issue
            if (username.equals("admin") && password.equals("admin123")) {
                isLoggedIn = true;
                loggedInAdmin = "admin";
                System.out.println("[✓] Login successful (default mode)! Welcome, admin.");
                return true;
            }
            System.out.println("DB error during login: " + e.getMessage());
            return false;
        }

        // Fallback
        if (username.equals("admin") && password.equals("admin123")) {
            isLoggedIn = true;
            loggedInAdmin = "admin";
            System.out.println("[✓] Login successful! Welcome, admin.");
            return true;
        }

        System.out.println("[✗] Invalid username or password. (Default: admin / admin123)");
        return false;
    }

    // ── Admin Profile ────────────────────────────────────────────────────────────
    private static void adminProfile(Scanner sc) {
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
                case 1: viewAdminDetails(); break;
                case 2: changeAdminUsername(sc); break;
                case 3: changeAdminPassword(sc); break;
                case 0: break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewAdminDetails() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║        ADMIN PROFILE         ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.printf ("║  %-10s : %-15s ║%n", "Role",     "Administrator");
        System.out.printf ("║  %-10s : %-15s ║%n", "Username", loggedInAdmin);
        System.out.printf ("║  %-10s : %-15s ║%n", "Access",   "Full System");
        System.out.println("╚══════════════════════════════╝");
        System.out.println("\nPermissions:");
        System.out.println("  ✓ Add / Delete Interns");
        System.out.println("  ✓ Assign & Manage Tasks");
        System.out.println("  ✓ Mark Attendance");
        System.out.println("  ✓ Approve / Reject Leaves");
        System.out.println("  ✓ Add Companies & Manage Students");
        System.out.println("  ✓ Approve Internship Applications");
        System.out.println("  ✓ View All Reports");
    }

    private static void changeAdminUsername(Scanner sc) {
        System.out.print("New username (or 0 to go back): ");
        String newUsername = sc.nextLine().trim();
        if (newUsername.equals("0")) return;

        System.out.print("Confirm current password: ");
        String password = sc.nextLine().trim();

        String sql = "UPDATE admin_accounts SET username = ? WHERE username = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setString(2, loggedInAdmin);
            ps.setString(3, password);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Username updated to '" + newUsername + "' successfully!");
                loggedInAdmin = newUsername;
            } else {
                System.out.println("Wrong password. Username not changed.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void changeAdminPassword(Scanner sc) {
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
            ps.setString(2, loggedInAdmin);
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

    // ── 12. Add New Company ──────────────────────────────────────────────────────
    public static void addNewCompany(Scanner sc) {
        System.out.println("\n----------- ADD NEW COMPANY -----------");
        System.out.print("Company Name (or 0 to cancel): ");
        String name = sc.nextLine().trim();
        if (name.equals("0")) { System.out.println("Cancelled."); return; }

        System.out.print("Company Email (login identifier): ");
        String email = sc.nextLine().trim();
        if (email.isEmpty()) { System.out.println("Email cannot be empty."); return; }

        System.out.print("Password for Company account: ");
        String password = sc.nextLine().trim();
        if (password.isEmpty()) { System.out.println("Password cannot be empty."); return; }

        System.out.print("Domain / Industry (e.g. IT, AI, Finance): ");
        String domain = sc.nextLine().trim();

        System.out.print("Location / City: ");
        String location = sc.nextLine().trim();

        System.out.print("Contact Number: ");
        String contact = sc.nextLine().trim();

        String sql = "INSERT INTO companies (name, email, password, domain, location, contact) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, domain);
            ps.setString(5, location);
            ps.setString(6, contact);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int newId = rs.next() ? rs.getInt(1) : -1;
            System.out.println("[✓] Company '" + name + "' added! (Company ID: " + newId + ")");
        } catch (SQLException e) {
            System.out.println("[✗] Error adding company: " + e.getMessage());
        }
    }

    // ── 13. View Registered Students ────────────────────────────────────────────
    public static void viewRegisteredStudents() {
        System.out.println("\n------------------------ REGISTERED STUDENTS ------------------------");
        String sql = "SELECT student_id, full_name, email, phone, college, domain FROM students ORDER BY student_id ASC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.printf("%-5s | %-20s | %-25s | %-12s | %-20s | %-15s%n",
                    "ID", "Name", "Email", "Phone", "College", "Domain");
            System.out.println("-----------------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d | %-20s | %-25s | %-12s | %-20s | %-15s%n",
                        rs.getInt("student_id"),
                        truncate(rs.getString("full_name"), 20),
                        truncate(rs.getString("email"), 25),
                        truncate(rs.getString("phone"), 12),
                        truncate(rs.getString("college"), 20),
                        truncate(rs.getString("domain"), 15));
            }
            if (!found) System.out.println("No registered students found yet.");
        } catch (SQLException e) {
            System.out.println("[✗] Error fetching students: " + e.getMessage());
        }
    }

    // ── 14. View All Internships ─────────────────────────────────────────────────
    public static void viewAllInternships() {
        System.out.println("\n---------------------------- ALL INTERNSHIPS ----------------------------");
        String sql = "SELECT i.internship_id, i.title, c.name AS company_name, i.domain, " +
                     "i.stipend, i.duration, i.vacancies, i.status " +
                     "FROM internships i JOIN companies c ON i.company_id = c.company_id " +
                     "ORDER BY i.internship_id DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.printf("%-5s | %-22s | %-18s | %-15s | %-12s | %-10s | %-6s | %-8s%n",
                    "ID", "Title", "Company", "Domain", "Stipend", "Duration", "Seats", "Status");
            System.out.println("----------------------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d | %-22s | %-18s | %-15s | %-12s | %-10s | %-6d | %-8s%n",
                        rs.getInt("internship_id"),
                        truncate(rs.getString("title"), 22),
                        truncate(rs.getString("company_name"), 18),
                        truncate(rs.getString("domain"), 15),
                        truncate(rs.getString("stipend"), 12),
                        truncate(rs.getString("duration"), 10),
                        rs.getInt("vacancies"),
                        rs.getString("status"));
            }
            if (!found) System.out.println("No internships found.");
        } catch (SQLException e) {
            System.out.println("[✗] Error fetching internships: " + e.getMessage());
        }
    }

    // ── 15. Approve / Reject Internship Applications ────────────────────────────
    public static void approveRejectApplications(Scanner sc) {
        System.out.println("\n-------------------- INTERNSHIP APPLICATIONS --------------------");
        String sql = "SELECT a.application_id, s.full_name AS student_name, c.name AS company_name, " +
                     "i.title AS internship_title, a.status, a.applied_date " +
                     "FROM applications a " +
                     "JOIN students s   ON a.student_id    = s.student_id " +
                     "JOIN internships i ON a.internship_id = i.internship_id " +
                     "JOIN companies c   ON i.company_id    = c.company_id " +
                     "ORDER BY a.application_id DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.printf("%-5s | %-18s | %-18s | %-20s | %-10s | %-10s%n",
                    "AppID", "Student", "Company", "Internship", "Date", "Status");
            System.out.println("---------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d | %-18s | %-18s | %-20s | %-10s | %-10s%n",
                        rs.getInt("application_id"),
                        truncate(rs.getString("student_name"), 18),
                        truncate(rs.getString("company_name"), 18),
                        truncate(rs.getString("internship_title"), 20),
                        String.valueOf(rs.getDate("applied_date")),
                        rs.getString("status"));
            }
            if (!found) { System.out.println("No applications received yet."); return; }

            System.out.print("\nEnter Application ID to update (or 0 to cancel): ");
            String idInput = sc.nextLine().trim();
            if (idInput.equals("0")) return;

            int appId;
            try { appId = Integer.parseInt(idInput); }
            catch (NumberFormatException e) { System.out.println("Invalid Application ID."); return; }

            System.out.println("Select new status:\n1. Approved\n2. Rejected\n3. Pending");
            System.out.print("Choice (1-3): ");
            String statusChoice = sc.nextLine().trim();
            String newStatus;
            if (statusChoice.equals("1"))      newStatus = "Approved";
            else if (statusChoice.equals("2")) newStatus = "Rejected";
            else if (statusChoice.equals("3")) newStatus = "Pending";
            else { System.out.println("Invalid choice."); return; }

            String updateSql = "UPDATE applications SET status = ? WHERE application_id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setString(1, newStatus);
                ps.setInt(2, appId);
                int rows = ps.executeUpdate();
                if (rows > 0)
                    System.out.println("[✓] Application #" + appId + " updated to: " + newStatus);
                else
                    System.out.println("[✗] No application found with ID: " + appId);
            }
        } catch (SQLException e) {
            System.out.println("[✗] Database error: " + e.getMessage());
        }
    }

    // ── 16. Generate System Report ───────────────────────────────────────────────
    public static void generateReports() {
        System.out.println("\n=======================================================");
        System.out.println("          INTERNSHIP SYSTEM SUMMARY REPORT             ");
        System.out.println("=======================================================");
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            ResultSet rs;

            int totalInterns = 0;
            rs = st.executeQuery("SELECT COUNT(*) FROM interns");
            if (rs.next()) totalInterns = rs.getInt(1);

            int totalStudents = 0;
            rs = st.executeQuery("SELECT COUNT(*) FROM students");
            if (rs.next()) totalStudents = rs.getInt(1);

            int totalCompanies = 0;
            rs = st.executeQuery("SELECT COUNT(*) FROM companies");
            if (rs.next()) totalCompanies = rs.getInt(1);

            int totalInternships = 0, activeInternships = 0;
            rs = st.executeQuery("SELECT COUNT(*), SUM(CASE WHEN status='Open' THEN 1 ELSE 0 END) FROM internships");
            if (rs.next()) { totalInternships = rs.getInt(1); activeInternships = rs.getInt(2); }

            int totalApps = 0, pendingApps = 0, approvedApps = 0, rejectedApps = 0;
            rs = st.executeQuery("SELECT status, COUNT(*) FROM applications GROUP BY status");
            while (rs.next()) {
                String s = rs.getString("status"); int cnt = rs.getInt(2);
                totalApps += cnt;
                if ("Pending".equalsIgnoreCase(s))  pendingApps  = cnt;
                else if ("Approved".equalsIgnoreCase(s) || "Accepted".equalsIgnoreCase(s)) approvedApps += cnt;
                else if ("Rejected".equalsIgnoreCase(s)) rejectedApps = cnt;
            }

            int totalTasks = 0, doneTasks = 0;
            rs = st.executeQuery("SELECT COUNT(*), SUM(CASE WHEN task_status='Done' THEN 1 ELSE 0 END) FROM tasks");
            if (rs.next()) { totalTasks = rs.getInt(1); doneTasks = rs.getInt(2); }

            System.out.println("1. USERS & ORGANISATIONS:");
            System.out.println("   • Active Interns (staff)       : " + totalInterns);
            System.out.println("   • Registered Students           : " + totalStudents);
            System.out.println("   • Registered Companies          : " + totalCompanies);
            System.out.println();
            System.out.println("2. INTERNSHIP LISTINGS:");
            System.out.println("   • Total Internships Posted      : " + totalInternships);
            System.out.println("   • Open / Active Internships     : " + activeInternships);
            System.out.println("   • Closed Internships            : " + (totalInternships - activeInternships));
            System.out.println();
            System.out.println("3. APPLICATIONS:");
            System.out.println("   • Total Applications            : " + totalApps);
            System.out.println("   • Pending                       : " + pendingApps);
            System.out.println("   • Approved / Accepted           : " + approvedApps);
            System.out.println("   • Rejected                      : " + rejectedApps);
            System.out.println();
            System.out.println("4. TASKS (intern staff):");
            System.out.println("   • Total Tasks Assigned          : " + totalTasks);
            System.out.println("   • Tasks Completed               : " + doneTasks);
            System.out.println();
            System.out.println("5. TOP DOMAINS:");
            rs = st.executeQuery("SELECT domain, COUNT(*) AS cnt FROM internships GROUP BY domain ORDER BY cnt DESC LIMIT 5");
            boolean hasDomains = false;
            while (rs.next()) {
                hasDomains = true;
                System.out.printf("   • %-20s : %d internship(s)%n", rs.getString("domain"), rs.getInt("cnt"));
            }
            if (!hasDomains) System.out.println("   • No domain data available yet.");
            System.out.println("=======================================================");

        } catch (SQLException e) {
            System.out.println("[✗] Error generating report: " + e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────
    private static String truncate(String val, int maxLen) {
        if (val == null) return "-";
        return val.length() <= maxLen ? val : val.substring(0, maxLen - 2) + "..";
    }

    private static void promptBack(Scanner sc) {
        System.out.print("\nPress Enter to go back to Admin menu...");
        sc.nextLine();
    }
}
