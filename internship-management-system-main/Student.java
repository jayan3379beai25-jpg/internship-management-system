import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Student Portal — from FRIEND's project (Aarush).
 * Students (external applicants) can register, browse internships, apply, and track status.
 *
 * NOTE: "Student" here means an external applicant looking for an internship to join.
 *       Once selected, they may become an "Intern" (staff) tracked in the Admin portal.
 */
public class Student {

    private static boolean isLoggedIn = false;
    private static int     currentStudentId   = -1;
    private static String  currentStudentName = null;
    private static String  currentStudentEmail = null;

    public static void menu(Scanner sc) {
        int choice = -1;
        while (choice != 0) {
            System.out.println("\n==============================================");
            System.out.println("               STUDENT PORTAL                ");
            if (isLoggedIn) System.out.println("       (Logged in as: " + currentStudentName + ")");
            System.out.println("==============================================");
            System.out.println("1. Student Registration & Login" + (isLoggedIn ? " (Logout / Switch)" : ""));
            System.out.println("2. View Available Internships");
            System.out.println("3. Search Internships by Domain / Keyword");
            System.out.println("4. Apply for Internship");
            System.out.println("5. View My Application Status");
            System.out.println("6. View / Update My Profile");
            System.out.println("0. Back to Main Menu");
            System.out.println("==============================================");
            System.out.print("Enter choice (0-6): ");

            String input = sc.nextLine().trim();
            try { choice = Integer.parseInt(input); }
            catch (NumberFormatException e) { System.out.println("Please enter a valid number (0-6)."); continue; }

            switch (choice) {
                case 1: authMenu(sc);                                      promptBack(sc); break;
                case 2: viewAvailableInternships();                        promptBack(sc); break;
                case 3: searchInternshipsByDomain(sc);                     promptBack(sc); break;
                case 4: if (ensureLoggedIn(sc)) applyForInternship(sc);    promptBack(sc); break;
                case 5: if (ensureLoggedIn(sc)) viewApplicationStatus();   promptBack(sc); break;
                case 6: if (ensureLoggedIn(sc)) viewPersonalProfile(sc);   promptBack(sc); break;
                case 0: System.out.println("Returning to Main Menu..."); break;
                default: System.out.println("Invalid choice. Please enter 0 to 6.");
            }
        }
    }

    // ── Auth submenu ─────────────────────────────────────────────────────────────
    private static void authMenu(Scanner sc) {
        System.out.println("\n--------- STUDENT REGISTRATION & LOGIN ---------");
        if (isLoggedIn) {
            System.out.println("Currently logged in as: " + currentStudentName + " (" + currentStudentEmail + ")");
            System.out.println("1. Logout\n2. Switch Account\n0. Back");
            System.out.print("Choice (0-2): ");
            String ch = sc.nextLine().trim();
            if (ch.equals("1"))      logout();
            else if (ch.equals("2")) { logout(); login(sc); }
            return;
        }
        System.out.println("1. Register New Student\n2. Student Login\n0. Back");
        System.out.print("Choice (0-2): ");
        String ch = sc.nextLine().trim();
        if (ch.equals("1"))      register(sc);
        else if (ch.equals("2")) login(sc);
    }

    private static void logout() {
        isLoggedIn = false; currentStudentId = -1;
        currentStudentName = null; currentStudentEmail = null;
        System.out.println("Logged out successfully.");
    }

    private static boolean ensureLoggedIn(Scanner sc) {
        if (isLoggedIn) return true;
        System.out.println("\n[!] Please login first.");
        System.out.println("1. Login\n2. Register New Student\n0. Cancel");
        System.out.print("Choice (0-2): ");
        String ch = sc.nextLine().trim();
        if (ch.equals("1"))      return login(sc);
        else if (ch.equals("2")) { if (register(sc)) return login(sc); }
        return false;
    }

    // ── 1.1 Register ─────────────────────────────────────────────────────────────
    public static boolean register(Scanner sc) {
        System.out.println("\n---------- STUDENT REGISTRATION ----------");
        System.out.print("Full Name (or 0 to cancel): ");
        String name = sc.nextLine().trim();
        if (name.equals("0")) return false;

        System.out.print("Email (login identifier): ");
        String email = sc.nextLine().trim();
        if (email.isEmpty()) { System.out.println("Email cannot be empty."); return false; }

        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        if (password.isEmpty()) { System.out.println("Password cannot be empty."); return false; }

        System.out.print("Phone Number: ");
        String phone = sc.nextLine().trim();

        System.out.print("College / University: ");
        String college = sc.nextLine().trim();

        System.out.print("Preferred Domain / Skills (e.g. Java, Python, Web Dev): ");
        String domain = sc.nextLine().trim();

        String sql = "INSERT INTO students (full_name, email, password, phone, college, domain) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, email); ps.setString(3, password);
            ps.setString(4, phone); ps.setString(5, college); ps.setString(6, domain);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int newId = rs.next() ? rs.getInt(1) : -1;
            System.out.println("[✓] Registration successful! Your Student ID: " + newId);
            System.out.println("    Login with email: " + email);
            return true;
        } catch (SQLException e) {
            System.out.println("[✗] Registration failed: " + e.getMessage());
            return false;
        }
    }

    // ── 1.2 Login ────────────────────────────────────────────────────────────────
    public static boolean login(Scanner sc) {
        System.out.println("\n------------- STUDENT LOGIN -------------");
        System.out.print("Email (or 0 to cancel): ");
        String email = sc.nextLine().trim();
        if (email.equals("0")) return false;

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        String sql = "SELECT student_id, full_name, email FROM students WHERE email = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email); ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentStudentId    = rs.getInt("student_id");
                currentStudentName  = rs.getString("full_name");
                currentStudentEmail = rs.getString("email");
                isLoggedIn = true;
                System.out.println("[✓] Welcome back, " + currentStudentName + "!");
                return true;
            } else {
                System.out.println("[✗] Invalid email or password.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("[✗] Database error: " + e.getMessage());
            return false;
        }
    }

    // ── 2. View Available Internships ────────────────────────────────────────────
    public static void viewAvailableInternships() {
        System.out.println("\n---------------------------- AVAILABLE INTERNSHIPS ----------------------------");
        String sql = "SELECT i.internship_id, i.title, c.name AS company_name, i.domain, " +
                     "i.description, i.stipend, i.duration, i.vacancies " +
                     "FROM internships i JOIN companies c ON i.company_id = c.company_id " +
                     "WHERE i.status = 'Open' ORDER BY i.internship_id DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.printf("%-5s | %-22s | %-18s | %-15s | %-12s | %-10s | %-6s%n",
                    "ID", "Title", "Company", "Domain", "Stipend", "Duration", "Seats");
            System.out.println("--------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d | %-22s | %-18s | %-15s | %-12s | %-10s | %-6d%n",
                        rs.getInt("internship_id"),
                        truncate(rs.getString("title"), 22),
                        truncate(rs.getString("company_name"), 18),
                        truncate(rs.getString("domain"), 15),
                        truncate(rs.getString("stipend"), 12),
                        truncate(rs.getString("duration"), 10),
                        rs.getInt("vacancies"));
            }
            if (!found) System.out.println("No open internships available at the moment.");
        } catch (SQLException e) {
            System.out.println("[✗] Error: " + e.getMessage());
        }
    }

    // ── 3. Search Internships ────────────────────────────────────────────────────
    public static void searchInternshipsByDomain(Scanner sc) {
        System.out.print("\nEnter domain or keyword (e.g. Java, Web, AI) (or 0 to cancel): ");
        String keyword = sc.nextLine().trim();
        if (keyword.equals("0")) return;

        System.out.println("\n----------------------- SEARCH RESULTS FOR '" + keyword + "' -----------------------");
        String sql = "SELECT i.internship_id, i.title, c.name AS company_name, i.domain, " +
                     "i.stipend, i.duration, i.vacancies " +
                     "FROM internships i JOIN companies c ON i.company_id = c.company_id " +
                     "WHERE i.status = 'Open' AND (i.domain LIKE ? OR i.title LIKE ? OR i.description LIKE ?) " +
                     "ORDER BY i.internship_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern); ps.setString(2, pattern); ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();

            System.out.printf("%-5s | %-22s | %-18s | %-15s | %-12s | %-10s | %-6s%n",
                    "ID", "Title", "Company", "Domain", "Stipend", "Duration", "Seats");
            System.out.println("--------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d | %-22s | %-18s | %-15s | %-12s | %-10s | %-6d%n",
                        rs.getInt("internship_id"),
                        truncate(rs.getString("title"), 22),
                        truncate(rs.getString("company_name"), 18),
                        truncate(rs.getString("domain"), 15),
                        truncate(rs.getString("stipend"), 12),
                        truncate(rs.getString("duration"), 10),
                        rs.getInt("vacancies"));
            }
            if (!found) System.out.println("No internships found matching '" + keyword + "'.");
        } catch (SQLException e) {
            System.out.println("[✗] Error: " + e.getMessage());
        }
    }

    // ── 4. Apply for Internship ──────────────────────────────────────────────────
    public static void applyForInternship(Scanner sc) {
        viewAvailableInternships();
        System.out.print("\nEnter Internship ID to apply for (or 0 to cancel): ");
        String input = sc.nextLine().trim();
        if (input.equals("0")) return;

        int internshipId;
        try { internshipId = Integer.parseInt(input); }
        catch (NumberFormatException e) { System.out.println("Invalid Internship ID."); return; }

        String checkSql = "SELECT title, status FROM internships WHERE internship_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setInt(1, internshipId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { System.out.println("[✗] Internship ID " + internshipId + " does not exist."); return; }
            if (!"Open".equalsIgnoreCase(rs.getString("status"))) {
                System.out.println("[!] This internship is no longer accepting applications (Status: " + rs.getString("status") + ")."); return;
            }
            String internTitle = rs.getString("title");

            // Duplicate check
            String dupSql = "SELECT status FROM applications WHERE student_id = ? AND internship_id = ?";
            try (PreparedStatement dupPs = con.prepareStatement(dupSql)) {
                dupPs.setInt(1, currentStudentId); dupPs.setInt(2, internshipId);
                ResultSet dupRs = dupPs.executeQuery();
                if (dupRs.next()) {
                    System.out.println("[!] You have already applied. Status: " + dupRs.getString("status")); return;
                }
            }

            // Submit
            String applySql = "INSERT INTO applications (student_id, internship_id, status, applied_date) VALUES (?, ?, 'Pending', CURDATE())";
            try (PreparedStatement applyPs = con.prepareStatement(applySql, Statement.RETURN_GENERATED_KEYS)) {
                applyPs.setInt(1, currentStudentId); applyPs.setInt(2, internshipId);
                applyPs.executeUpdate();
                ResultSet genRs = applyPs.getGeneratedKeys();
                int appId = genRs.next() ? genRs.getInt(1) : -1;
                System.out.println("[✓] Application submitted for: " + internTitle);
                System.out.println("    Your Application ID: " + appId + " (Status: Pending)");
            }
        } catch (SQLException e) {
            System.out.println("[✗] Error: " + e.getMessage());
        }
    }

    // ── 5. View Application Status ───────────────────────────────────────────────
    public static void viewApplicationStatus() {
        System.out.println("\n------------------------- MY APPLICATION STATUS -------------------------");
        String sql = "SELECT a.application_id, i.title, c.name AS company_name, " +
                     "i.domain, a.applied_date, a.status " +
                     "FROM applications a " +
                     "JOIN internships i ON a.internship_id = i.internship_id " +
                     "JOIN companies c   ON i.company_id    = c.company_id " +
                     "WHERE a.student_id = ? ORDER BY a.application_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentStudentId);
            ResultSet rs = ps.executeQuery();

            System.out.printf("%-6s | %-22s | %-18s | %-15s | %-12s | %-14s%n",
                    "AppID", "Internship Title", "Company", "Domain", "Date", "Status");
            System.out.println("--------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                String status = rs.getString("status");
                String displayStatus = status;
                if ("Accepted".equalsIgnoreCase(status))     displayStatus = "[ACCEPTED ✓]";
                else if ("Rejected".equalsIgnoreCase(status)) displayStatus = "[REJECTED ✗]";
                else if ("Approved".equalsIgnoreCase(status)) displayStatus = "[APPROVED]";

                System.out.printf("%-6d | %-22s | %-18s | %-15s | %-12s | %-14s%n",
                        rs.getInt("application_id"),
                        truncate(rs.getString("title"), 22),
                        truncate(rs.getString("company_name"), 18),
                        truncate(rs.getString("domain"), 15),
                        String.valueOf(rs.getDate("applied_date")),
                        displayStatus);
            }
            if (!found) System.out.println("You have not applied for any internships yet.");
        } catch (SQLException e) {
            System.out.println("[✗] Error: " + e.getMessage());
        }
    }

    // ── 6. View / Update Profile ─────────────────────────────────────────────────
    public static void viewPersonalProfile(Scanner sc) {
        System.out.println("\n================ PERSONAL PROFILE ================");
        String sql = "SELECT student_id, full_name, email, phone, college, domain, created_at FROM students WHERE student_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentStudentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Student ID         : " + rs.getInt("student_id"));
                System.out.println("Full Name          : " + rs.getString("full_name"));
                System.out.println("Email              : " + rs.getString("email"));
                System.out.println("Phone Number       : " + rs.getString("phone"));
                System.out.println("College/University : " + rs.getString("college"));
                System.out.println("Domain / Skills    : " + rs.getString("domain"));
                System.out.println("Registered On      : " + rs.getTimestamp("created_at"));
                System.out.println("==================================================");

                System.out.print("\nDo you want to update profile details? (y/n): ");
                if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                    updateProfile(sc);
                }
            } else {
                System.out.println("Profile not found.");
            }
        } catch (SQLException e) {
            System.out.println("[✗] Error: " + e.getMessage());
        }
    }

    private static void updateProfile(Scanner sc) {
        System.out.print("New Phone Number  (Enter to keep current): ");
        String phone   = sc.nextLine().trim();
        System.out.print("New College/Univ  (Enter to keep current): ");
        String college = sc.nextLine().trim();
        System.out.print("New Domain/Skills (Enter to keep current): ");
        String domain  = sc.nextLine().trim();

        String sql = "UPDATE students SET " +
                     "phone   = CASE WHEN ? = '' THEN phone   ELSE ? END, " +
                     "college = CASE WHEN ? = '' THEN college ELSE ? END, " +
                     "domain  = CASE WHEN ? = '' THEN domain  ELSE ? END " +
                     "WHERE student_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);  ps.setString(2, phone);
            ps.setString(3, college); ps.setString(4, college);
            ps.setString(5, domain);  ps.setString(6, domain);
            ps.setInt(7, currentStudentId);
            if (ps.executeUpdate() > 0) System.out.println("[✓] Profile updated successfully!");
        } catch (SQLException e) {
            System.out.println("[✗] Error: " + e.getMessage());
        }
    }

    private static String truncate(String val, int maxLen) {
        if (val == null) return "-";
        return val.length() <= maxLen ? val : val.substring(0, maxLen - 2) + "..";
    }

    private static void promptBack(Scanner sc) {
        System.out.print("\nPress Enter to go back to Student menu...");
        sc.nextLine();
    }
}
