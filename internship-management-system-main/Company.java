import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Company Portal — from FRIEND's project (Aarush).
 * Companies can self-register, login, post internships, view & process applications.
 */
public class Company {

    private static boolean isLoggedIn = false;
    private static int     currentCompanyId   = -1;
    private static String  currentCompanyName = null;
    private static String  currentCompanyEmail = null;

    public static void menu(Scanner sc) {
        int choice = -1;
        while (choice != 0) {
            System.out.println("\n==============================================");
            System.out.println("               COMPANY PORTAL                ");
            if (isLoggedIn) System.out.println("       (Logged in as: " + currentCompanyName + ")");
            System.out.println("==============================================");
            System.out.println("1. Company Registration & Login" + (isLoggedIn ? " (Logout / Switch)" : ""));
            System.out.println("2. Post New Internship");
            System.out.println("3. View Applications Received");
            System.out.println("4. Accept / Reject Candidates");
            System.out.println("5. View Selected / Hired Candidates");
            System.out.println("0. Back to Main Menu");
            System.out.println("==============================================");
            System.out.print("Enter choice (0-5): ");

            String input = sc.nextLine().trim();
            try { choice = Integer.parseInt(input); }
            catch (NumberFormatException e) { System.out.println("Please enter a valid number (0-5)."); continue; }

            switch (choice) {
                case 1: authMenu(sc);                                   promptBack(sc); break;
                case 2: if (ensureLoggedIn(sc)) postNewInternship(sc);  promptBack(sc); break;
                case 3: if (ensureLoggedIn(sc)) viewApplications();     promptBack(sc); break;
                case 4: if (ensureLoggedIn(sc)) acceptRejectStudents(sc); promptBack(sc); break;
                case 5: if (ensureLoggedIn(sc)) viewSelectedCandidates(); promptBack(sc); break;
                case 0: System.out.println("Returning to Main Menu..."); break;
                default: System.out.println("Invalid choice. Please enter 0 to 5.");
            }
        }
    }

    // ── Auth submenu ─────────────────────────────────────────────────────────────
    private static void authMenu(Scanner sc) {
        System.out.println("\n--------- COMPANY REGISTRATION & LOGIN ---------");
        if (isLoggedIn) {
            System.out.println("Currently logged in as: " + currentCompanyName + " (" + currentCompanyEmail + ")");
            System.out.println("1. Logout\n2. Switch Account\n0. Back");
            System.out.print("Choice (0-2): ");
            String ch = sc.nextLine().trim();
            if (ch.equals("1"))      logout();
            else if (ch.equals("2")) { logout(); login(sc); }
            return;
        }
        System.out.println("1. Register New Company\n2. Company Login\n0. Back");
        System.out.print("Choice (0-2): ");
        String ch = sc.nextLine().trim();
        if (ch.equals("1"))      register(sc);
        else if (ch.equals("2")) login(sc);
    }

    private static void logout() {
        isLoggedIn = false; currentCompanyId = -1;
        currentCompanyName = null; currentCompanyEmail = null;
        System.out.println("Company logged out successfully.");
    }

    private static boolean ensureLoggedIn(Scanner sc) {
        if (isLoggedIn) return true;
        System.out.println("\n[!] Please login first.");
        System.out.println("1. Login\n2. Register New Company\n0. Cancel");
        System.out.print("Choice (0-2): ");
        String ch = sc.nextLine().trim();
        if (ch.equals("1"))      return login(sc);
        else if (ch.equals("2")) { if (register(sc)) return login(sc); }
        return false;
    }

    // ── 1.1 Register ─────────────────────────────────────────────────────────────
    public static boolean register(Scanner sc) {
        System.out.println("\n---------- COMPANY REGISTRATION ----------");
        System.out.print("Company Name (or 0 to cancel): ");
        String name = sc.nextLine().trim();
        if (name.equals("0")) return false;

        System.out.print("Company Email (login identifier): ");
        String email = sc.nextLine().trim();
        if (email.isEmpty()) { System.out.println("Email cannot be empty."); return false; }

        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        if (password.isEmpty()) { System.out.println("Password cannot be empty."); return false; }

        System.out.print("Industry / Domain (e.g. IT, FinTech, AI): ");
        String domain = sc.nextLine().trim();

        System.out.print("Headquarters / Location: ");
        String location = sc.nextLine().trim();

        System.out.print("Contact Number: ");
        String contact = sc.nextLine().trim();

        String sql = "INSERT INTO companies (name, email, password, domain, location, contact) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, email); ps.setString(3, password);
            ps.setString(4, domain); ps.setString(5, location); ps.setString(6, contact);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int newId = rs.next() ? rs.getInt(1) : -1;
            System.out.println("[✓] Company registered! ID: " + newId + "  — Login with email: " + email);
            return true;
        } catch (SQLException e) {
            System.out.println("[✗] Registration failed: " + e.getMessage());
            return false;
        }
    }

    // ── 1.2 Login ────────────────────────────────────────────────────────────────
    public static boolean login(Scanner sc) {
        System.out.println("\n------------- COMPANY LOGIN -------------");
        System.out.print("Company Email (or 0 to cancel): ");
        String email = sc.nextLine().trim();
        if (email.equals("0")) return false;

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        String sql = "SELECT company_id, name, email FROM companies WHERE email = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email); ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentCompanyId    = rs.getInt("company_id");
                currentCompanyName  = rs.getString("name");
                currentCompanyEmail = rs.getString("email");
                isLoggedIn = true;
                System.out.println("[✓] Welcome, " + currentCompanyName + "!");
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

    // ── 2. Post New Internship ───────────────────────────────────────────────────
    public static void postNewInternship(Scanner sc) {
        System.out.println("\n----------- POST NEW INTERNSHIP -----------");
        System.out.print("Internship Title (or 0 to cancel): ");
        String title = sc.nextLine().trim();
        if (title.equals("0")) return;

        System.out.print("Domain / Technology (e.g. Web Dev, AI, Data): ");
        String domain = sc.nextLine().trim();

        System.out.print("Short Description / Requirements: ");
        String description = sc.nextLine().trim();

        System.out.print("Stipend (e.g. Rs. 15,000/month or Unpaid): ");
        String stipend = sc.nextLine().trim();

        System.out.print("Duration (e.g. 3 Months, 6 Months): ");
        String duration = sc.nextLine().trim();

        System.out.print("Number of Vacancies: ");
        int vacancies = 1;
        try { vacancies = Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Defaulting to 1 vacancy."); }

        String sql = "INSERT INTO internships (company_id, title, domain, description, stipend, duration, vacancies, status, posted_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'Open', CURDATE())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, currentCompanyId); ps.setString(2, title); ps.setString(3, domain);
            ps.setString(4, description);   ps.setString(5, stipend); ps.setString(6, duration);
            ps.setInt(7, vacancies);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int newId = rs.next() ? rs.getInt(1) : -1;
            System.out.println("[✓] Internship posted successfully! (ID: " + newId + ")");
        } catch (SQLException e) {
            System.out.println("[✗] Error posting internship: " + e.getMessage());
        }
    }

    // ── 3. View Applications ─────────────────────────────────────────────────────
    public static void viewApplications() {
        System.out.println("\n------------------------- RECEIVED APPLICATIONS -------------------------");
        String sql = "SELECT a.application_id, i.title AS internship_title, s.full_name AS student_name, " +
                     "s.email AS student_email, s.phone, s.college, a.applied_date, a.status " +
                     "FROM applications a " +
                     "JOIN internships i ON a.internship_id = i.internship_id " +
                     "JOIN students s    ON a.student_id    = s.student_id " +
                     "WHERE i.company_id = ? ORDER BY a.application_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentCompanyId);
            ResultSet rs = ps.executeQuery();

            System.out.printf("%-6s | %-20s | %-18s | %-20s | %-15s | %-10s | %-12s%n",
                    "AppID", "Internship", "Candidate", "Email", "College", "Date", "Status");
            System.out.println("----------------------------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-6d | %-20s | %-18s | %-20s | %-15s | %-10s | %-12s%n",
                        rs.getInt("application_id"),
                        truncate(rs.getString("internship_title"), 20),
                        truncate(rs.getString("student_name"), 18),
                        truncate(rs.getString("student_email"), 20),
                        truncate(rs.getString("college"), 15),
                        String.valueOf(rs.getDate("applied_date")),
                        rs.getString("status"));
            }
            if (!found) System.out.println("No applications received yet.");
        } catch (SQLException e) {
            System.out.println("[✗] Error: " + e.getMessage());
        }
    }

    // ── 4. Accept / Reject Students ──────────────────────────────────────────────
    public static void acceptRejectStudents(Scanner sc) {
        viewApplications();
        System.out.print("\nEnter Application ID to update (or 0 to cancel): ");
        String idInput = sc.nextLine().trim();
        if (idInput.equals("0")) return;

        int appId;
        try { appId = Integer.parseInt(idInput); }
        catch (NumberFormatException e) { System.out.println("Invalid Application ID."); return; }

        String verifySql = "SELECT a.application_id, s.full_name, i.title " +
                           "FROM applications a " +
                           "JOIN internships i ON a.internship_id = i.internship_id " +
                           "JOIN students s    ON a.student_id    = s.student_id " +
                           "WHERE a.application_id = ? AND i.company_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement verifyPs = con.prepareStatement(verifySql)) {
            verifyPs.setInt(1, appId); verifyPs.setInt(2, currentCompanyId);
            ResultSet rs = verifyPs.executeQuery();
            if (!rs.next()) {
                System.out.println("[✗] Application not found or doesn't belong to your company.");
                return;
            }
            String candidateName = rs.getString("full_name");
            String internTitle   = rs.getString("title");

            System.out.println("\nApplication for: " + candidateName + " (" + internTitle + ")");
            System.out.println("1. Accept Candidate\n2. Reject Candidate\n3. Mark Under Review");
            System.out.print("Choice (1-3): ");
            String action = sc.nextLine().trim();
            String newStatus;
            if (action.equals("1"))      newStatus = "Accepted";
            else if (action.equals("2")) newStatus = "Rejected";
            else if (action.equals("3")) newStatus = "Under Review";
            else { System.out.println("Invalid selection."); return; }

            String updateSql = "UPDATE applications SET status = ? WHERE application_id = ?";
            try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                updatePs.setString(1, newStatus); updatePs.setInt(2, appId);
                updatePs.executeUpdate();
                System.out.println("[✓] Application #" + appId + " for '" + candidateName + "' → " + newStatus);
            }
        } catch (SQLException e) {
            System.out.println("[✗] Error: " + e.getMessage());
        }
    }

    // ── 5. View Selected Candidates ──────────────────────────────────────────────
    public static void viewSelectedCandidates() {
        System.out.println("\n------------------------- SELECTED / HIRED CANDIDATES -------------------------");
        String sql = "SELECT a.application_id, s.full_name, s.email, s.phone, s.college, i.title " +
                     "FROM applications a " +
                     "JOIN internships i ON a.internship_id = i.internship_id " +
                     "JOIN students s    ON a.student_id    = s.student_id " +
                     "WHERE i.company_id = ? AND a.status = 'Accepted' ORDER BY a.application_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentCompanyId);
            ResultSet rs = ps.executeQuery();

            System.out.printf("%-6s | %-18s | %-20s | %-12s | %-15s | %-20s%n",
                    "AppID", "Candidate", "Email", "Phone", "College", "Internship");
            System.out.println("------------------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-6d | %-18s | %-20s | %-12s | %-15s | %-20s%n",
                        rs.getInt("application_id"),
                        truncate(rs.getString("full_name"), 18),
                        truncate(rs.getString("email"), 20),
                        truncate(rs.getString("phone"), 12),
                        truncate(rs.getString("college"), 15),
                        truncate(rs.getString("title"), 20));
            }
            if (!found) System.out.println("No candidates marked as 'Accepted' yet.");
        } catch (SQLException e) {
            System.out.println("[✗] Error: " + e.getMessage());
        }
    }

    private static String truncate(String val, int maxLen) {
        if (val == null) return "-";
        return val.length() <= maxLen ? val : val.substring(0, maxLen - 2) + "..";
    }

    private static void promptBack(Scanner sc) {
        System.out.print("\nPress Enter to go back to Company menu...");
        sc.nextLine();
    }
}
