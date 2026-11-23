import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Intern {
    private int internId;
    private String fullName;
    private String email;
    private String phone;
    private String college;
    private Date joinDate;
    private int deptId;
    private int mentorId;
    private String status;

    public Intern() {}

    public Intern(int internId, String fullName, String email, String phone, String college,
                  Date joinDate, int deptId, int mentorId, String status) {
        this.internId = internId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.college = college;
        this.joinDate = joinDate;
        this.deptId = deptId;
        this.mentorId = mentorId;
        this.status = status;
    }

    public int getInternId() { return internId; }
    public void setInternId(int internId) { this.internId = internId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public Date getJoinDate() { return joinDate; }
    public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }

    public int getDeptId() { return deptId; }
    public void setDeptId(int deptId) { this.deptId = deptId; }

    public int getMentorId() { return mentorId; }
    public void setMentorId(int mentorId) { this.mentorId = mentorId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Helper: fetch intern full name by ID
    public static String getInternName(int internId) {
        String sql = "SELECT full_name FROM interns WHERE intern_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("full_name");
            }
        } catch (SQLException ignored) {}
        return null;
    }

    // View profile of an individual intern
    public static void viewMyProfile(int internId) {
        String sql = "SELECT i.intern_id, i.full_name, i.email, i.phone, i.college, i.join_date, i.status, "
                + "d.dept_name, m.mentor_name "
                + "FROM interns i "
                + "LEFT JOIN departments d ON i.dept_id = d.dept_id "
                + "LEFT JOIN mentors m ON i.mentor_id = m.mentor_id "
                + "WHERE i.intern_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("\n===== Intern Profile =====");
                System.out.println("ID          : " + rs.getInt("intern_id"));
                System.out.println("Full Name   : " + rs.getString("full_name"));
                System.out.println("Email       : " + rs.getString("email"));
                System.out.println("Phone       : " + rs.getString("phone"));
                System.out.println("College     : " + rs.getString("college"));
                System.out.println("Join Date   : " + rs.getDate("join_date"));
                System.out.println("Status      : " + rs.getString("status"));
                System.out.println("Department  : " + rs.getString("dept_name"));
                System.out.println("Mentor      : " + rs.getString("mentor_name"));
            } else {
                System.out.println("No intern found with ID: " + internId);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 1. Add a new intern (with option to go back)
    public static void addIntern(Scanner sc) {
        System.out.print("Full name (or 0 to go back): ");
        String name = sc.nextLine().trim();
        if (name.equals("0") || name.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        System.out.print("Email (or 0 to go back): ");
        String email = sc.nextLine().trim();
        if (email.equals("0") || email.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        System.out.print("Phone (or 0 to go back): ");
        String phone = sc.nextLine().trim();
        if (phone.equals("0") || phone.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        System.out.print("College (or 0 to go back): ");
        String college = sc.nextLine().trim();
        if (college.equals("0") || college.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        System.out.print("Join date (YYYY-MM-DD) (or 0 to go back): ");
        String joinDate = sc.nextLine().trim();
        if (joinDate.equals("0") || joinDate.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        System.out.print("Set Login Password (or 0 to go back): ");
        String password = sc.nextLine().trim();
        if (password.equals("0") || password.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        // Show departments and mentors so user can pick IDs
        Department.viewDepartments();
        System.out.print("Department ID (or 0 to go back): ");
        String deptStr = sc.nextLine().trim();
        if (deptStr.equals("0") || deptStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }
        int deptId = Integer.parseInt(deptStr);

        Mentor.viewMentors();
        System.out.print("Mentor ID (or 0 to go back): ");
        String mentorStr = sc.nextLine().trim();
        if (mentorStr.equals("0") || mentorStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }
        int mentorId = Integer.parseInt(mentorStr);

        String sql = "INSERT INTO interns (full_name, email, phone, college, join_date, password, dept_id, mentor_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, college);
            ps.setDate(5, Date.valueOf(joinDate));
            ps.setString(6, password);
            ps.setInt(7, deptId);
            ps.setInt(8, mentorId);
            ps.executeUpdate();
            System.out.println("Intern added successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 2. View all interns
    public static void viewInterns() {
        String sql = "SELECT i.intern_id, i.full_name, i.college, i.status, d.dept_name, m.mentor_name "
                + "FROM interns i "
                + "LEFT JOIN departments d ON i.dept_id = d.dept_id "
                + "LEFT JOIN mentors m ON i.mentor_id = m.mentor_id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\nID | Name | College | Status | Department | Mentor");
            System.out.println("---------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(rs.getInt("intern_id") + " | "
                        + rs.getString("full_name") + " | "
                        + rs.getString("college") + " | "
                        + rs.getString("status") + " | "
                        + rs.getString("dept_name") + " | "
                        + rs.getString("mentor_name"));
            }
            if (!found) {
                System.out.println("No interns found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 3. Search interns by name (with option to go back)
    public static void searchIntern(Scanner sc) {
        System.out.print("Enter name to search (or 0 to go back): ");
        String keyword = sc.nextLine().trim();
        if (keyword.equals("0") || keyword.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        String sql = "SELECT intern_id, full_name, email, phone FROM interns WHERE full_name LIKE ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            System.out.println("\nID | Name | Email | Phone");
            System.out.println("----------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(rs.getInt("intern_id") + " | "
                        + rs.getString("full_name") + " | "
                        + rs.getString("email") + " | "
                        + rs.getString("phone"));
            }
            if (!found) {
                System.out.println("No interns found matching '" + keyword + "'.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 4. Change intern status (with option to go back)
    public static void updateInternStatus(Scanner sc) {
        System.out.print("Intern ID (or 0 to go back): ");
        String idStr = sc.nextLine().trim();
        if (idStr.equals("0") || idStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        System.out.print("New status (Active/Completed/Left) (or 0 to go back): ");
        String status = sc.nextLine().trim();
        if (status.equals("0") || status.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        String sql = "UPDATE interns SET status = ? WHERE intern_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Status updated successfully!");
            } else {
                System.out.println("No intern found with ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 5. Delete an intern (with option to go back)
    public static void deleteIntern(Scanner sc) {
        System.out.print("Intern ID to delete (or 0 to go back): ");
        String idStr = sc.nextLine().trim();
        if (idStr.equals("0") || idStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        System.out.print("Are you sure you want to delete intern ID " + id + "? (y/n): ");
        String confirm = sc.nextLine().trim();
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        String sql = "DELETE FROM interns WHERE intern_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Intern deleted successfully.");
            } else {
                System.out.println("No intern found with ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
