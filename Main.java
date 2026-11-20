import java.sql.*;
import java.util.Scanner;

// Internship Tracker - console app using JDBC + MySQL
// Tracks interns, mentors, tasks and attendance
public class Main {

    // ---- Change these to match your MySQL setup ----
    static String URL = "jdbc:mysql://localhost:3306/internship_tracker";
    static String USER = "root";
    static String PASSWORD = "Chit2025kara!";

    static Scanner sc = new Scanner(System.in);

    // Make a connection to the database
    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 1. Add a new intern
    static void addIntern() {
        System.out.print("Full name: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Phone: ");
        String phone = sc.nextLine();
        System.out.print("College: ");
        String college = sc.nextLine();
        System.out.print("Join date (YYYY-MM-DD): ");
        String joinDate = sc.nextLine();

        // Show departments and mentors so the user can pick the IDs
        viewDepartments();
        System.out.print("Department ID: ");
        int deptId = Integer.parseInt(sc.nextLine());
        viewMentors();
        System.out.print("Mentor ID: ");
        int mentorId = Integer.parseInt(sc.nextLine());

        String sql = "INSERT INTO interns (full_name, email, phone, college, join_date, dept_id, mentor_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, college);
            ps.setDate(5, Date.valueOf(joinDate));
            ps.setInt(6, deptId);
            ps.setInt(7, mentorId);
            ps.executeUpdate();
            System.out.println("Intern added!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 2. View all interns with department and mentor names
    static void viewInterns() {
        String sql = "SELECT i.intern_id, i.full_name, i.college, i.status, d.dept_name, m.mentor_name "
                + "FROM interns i "
                + "LEFT JOIN departments d ON i.dept_id = d.dept_id "
                + "LEFT JOIN mentors m ON i.mentor_id = m.mentor_id";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\nID | Name | College | Status | Department | Mentor");
            while (rs.next()) {
                System.out.println(rs.getInt("intern_id") + " | "
                        + rs.getString("full_name") + " | "
                        + rs.getString("college") + " | "
                        + rs.getString("status") + " | "
                        + rs.getString("dept_name") + " | "
                        + rs.getString("mentor_name"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 3. Search interns by name
    static void searchIntern() {
        System.out.print("Enter name to search: ");
        String keyword = sc.nextLine();

        String sql = "SELECT intern_id, full_name, email, phone FROM interns WHERE full_name LIKE ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            System.out.println("\nID | Name | Email | Phone");
            while (rs.next()) {
                System.out.println(rs.getInt("intern_id") + " | "
                        + rs.getString("full_name") + " | "
                        + rs.getString("email") + " | "
                        + rs.getString("phone"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 4. Change intern status (Active / Completed / Left)
    static void updateInternStatus() {
        System.out.print("Intern ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("New status (Active/Completed/Left): ");
        String status = sc.nextLine();

        String sql = "UPDATE interns SET status = ? WHERE intern_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Status updated!");
            } else {
                System.out.println("No intern found with that ID.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 5. Delete an intern (their tasks and attendance get deleted too)
    static void deleteIntern() {
        System.out.print("Intern ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());

        String sql = "DELETE FROM interns WHERE intern_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Intern deleted.");
            } else {
                System.out.println("No intern found with that ID.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 6. Give a task to an intern
    static void assignTask() {
        System.out.print("Intern ID: ");
        int internId = Integer.parseInt(sc.nextLine());
        System.out.print("Task title: ");
        String title = sc.nextLine();
        System.out.print("Due date (YYYY-MM-DD): ");
        String dueDate = sc.nextLine();

        String sql = "INSERT INTO tasks (intern_id, task_title, due_date) VALUES (?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ps.setString(2, title);
            ps.setDate(3, Date.valueOf(dueDate));
            ps.executeUpdate();
            System.out.println("Task assigned!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 7. View all tasks of one intern
    static void viewTasks() {
        System.out.print("Intern ID: ");
        int internId = Integer.parseInt(sc.nextLine());

        String sql = "SELECT task_id, task_title, due_date, task_status FROM tasks WHERE intern_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\nTask ID | Title | Due Date | Status");
            while (rs.next()) {
                System.out.println(rs.getInt("task_id") + " | "
                        + rs.getString("task_title") + " | "
                        + rs.getDate("due_date") + " | "
                        + rs.getString("task_status"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 8. Update the status of a task (Pending / In Progress / Done)
    static void updateTaskStatus() {
        System.out.print("Task ID: ");
        int taskId = Integer.parseInt(sc.nextLine());
        System.out.print("New status (Pending/In Progress/Done): ");
        String status = sc.nextLine();

        String sql = "UPDATE tasks SET task_status = ? WHERE task_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, taskId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Task updated!");
            } else {
                System.out.println("No task found with that ID.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 9. Mark attendance for today
    static void markAttendance() {
        System.out.print("Intern ID: ");
        int internId = Integer.parseInt(sc.nextLine());
        System.out.print("Present today? (y/n): ");
        String answer = sc.nextLine();
        boolean present = answer.equalsIgnoreCase("y");

        String sql = "INSERT INTO attendance (intern_id, att_date, is_present) VALUES (?, CURDATE(), ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ps.setBoolean(2, present);
            ps.executeUpdate();
            System.out.println("Attendance marked!");
        } catch (SQLException e) {
            System.out.println("Error (maybe already marked today): " + e.getMessage());
        }
    }

    // 10. Attendance report: days present and total days for each intern
    static void attendanceReport() {
        String sql = "SELECT i.full_name, "
                + "SUM(a.is_present) AS days_present, COUNT(a.attendance_id) AS total_days "
                + "FROM interns i LEFT JOIN attendance a ON i.intern_id = a.intern_id "
                + "GROUP BY i.intern_id, i.full_name";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\nName | Days Present | Total Days");
            while (rs.next()) {
                System.out.println(rs.getString("full_name") + " | "
                        + rs.getInt("days_present") + " | "
                        + rs.getInt("total_days"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Helper: show all departments
    static void viewDepartments() {
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM departments")) {
            System.out.println("\nDepartments:");
            while (rs.next()) {
                System.out.println(rs.getInt("dept_id") + " - " + rs.getString("dept_name"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Helper: show all mentors
    static void viewMentors() {
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT mentor_id, mentor_name FROM mentors")) {
            System.out.println("\nMentors:");
            while (rs.next()) {
                System.out.println(rs.getInt("mentor_id") + " - " + rs.getString("mentor_name"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Main menu loop
    public static void main(String[] args) {
        int choice = 0;
        while (choice != 11) {
            System.out.println("\n===== Internship Tracker =====");
            System.out.println("1.  Add Intern");
            System.out.println("2.  View All Interns");
            System.out.println("3.  Search Intern by Name");
            System.out.println("4.  Update Intern Status");
            System.out.println("5.  Delete Intern");
            System.out.println("6.  Assign Task");
            System.out.println("7.  View Tasks of an Intern");
            System.out.println("8.  Update Task Status");
            System.out.println("9.  Mark Attendance (today)");
            System.out.println("10. Attendance Report");
            System.out.println("11. Exit");
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
                continue;
            }

            if (choice == 1) addIntern();
            else if (choice == 2) viewInterns();
            else if (choice == 3) searchIntern();
            else if (choice == 4) updateInternStatus();
            else if (choice == 5) deleteIntern();
            else if (choice == 6) assignTask();
            else if (choice == 7) viewTasks();
            else if (choice == 8) updateTaskStatus();
            else if (choice == 9) markAttendance();
            else if (choice == 10) attendanceReport();
            else if (choice == 11) System.out.println("Bye!");
            else System.out.println("Invalid choice.");
        }
    }
}