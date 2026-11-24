import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    // ── Change these to match your MySQL Workbench credentials ──────────────────
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME  = "internship_tracker";
    private static final String URL      = BASE_URL + DB_NAME;
    private static final String USER     = "root";
    private static final String PASSWORD = "Aarush#8950359598";   // your MySQL Workbench root password

    // Standard connection (requires DB to exist)
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Connection without DB name (used only during first-time setup)
    public static Connection getBaseConnection() throws SQLException {
        return DriverManager.getConnection(BASE_URL, USER, PASSWORD);
    }

    /**
     * Called once at startup — creates the database and all required tables
     * if they do not already exist. Safe to call every time (uses IF NOT EXISTS).
     */
    public static void initDatabase() {
        System.out.println("[DB] Initializing database...");
        try {
            // Step 1: Create database if missing
            try (Connection baseCon = getBaseConnection();
                 Statement st = baseCon.createStatement()) {
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                System.out.println("[DB] Database '" + DB_NAME + "' ready.");
            } catch (SQLException e) {
                // Might fail if DB already exists and base URL not supported — continue
            }

            // Step 2: Create all tables
            try (Connection con = getConnection();
                 Statement st = con.createStatement()) {

                // ── 1. Admins ────────────────────────────────────────────────────
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS admin_accounts (" +
                    "  id       INT AUTO_INCREMENT PRIMARY KEY," +
                    "  username VARCHAR(100) NOT NULL UNIQUE," +
                    "  password VARCHAR(100) NOT NULL" +
                    ")"
                );
                // Insert default admin
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM admin_accounts")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        con.createStatement().executeUpdate(
                            "INSERT INTO admin_accounts (username, password) VALUES ('admin', 'admin123')"
                        );
                        System.out.println("[DB] Default admin created → username: admin | password: admin123");
                    }
                }

                // ── 2. Departments ───────────────────────────────────────────────
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS departments (" +
                    "  dept_id   INT AUTO_INCREMENT PRIMARY KEY," +
                    "  dept_name VARCHAR(100) NOT NULL" +
                    ")"
                );
                // Seed departments if empty
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM departments")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        con.createStatement().executeUpdate(
                            "INSERT INTO departments (dept_name) VALUES " +
                            "('Software Engineering'),('Data Science'),('Web Development')," +
                            "('AI & Machine Learning'),('Cybersecurity'),('Cloud Computing')"
                        );
                    }
                }

                // ── 3. Mentors ───────────────────────────────────────────────────
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mentors (" +
                    "  mentor_id   INT AUTO_INCREMENT PRIMARY KEY," +
                    "  mentor_name VARCHAR(100) NOT NULL" +
                    ")"
                );
                // Seed mentors if empty
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mentors")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        con.createStatement().executeUpdate(
                            "INSERT INTO mentors (mentor_name) VALUES " +
                            "('Prof. Sharma'),('Dr. Mehta'),('Ms. Kapoor'),('Mr. Verma')"
                        );
                    }
                }

                // ── 4. Interns ───────────────────────────────────────────────────
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS interns (" +
                    "  intern_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  full_name VARCHAR(100) NOT NULL," +
                    "  email     VARCHAR(100) UNIQUE NOT NULL," +
                    "  phone     VARCHAR(30)," +
                    "  college   VARCHAR(150)," +
                    "  join_date DATE," +
                    "  password  VARCHAR(100) NOT NULL DEFAULT 'intern123'," +
                    "  dept_id   INT," +
                    "  mentor_id INT," +
                    "  status    VARCHAR(30) DEFAULT 'Active'," +
                    "  FOREIGN KEY (dept_id)   REFERENCES departments(dept_id)  ON DELETE SET NULL," +
                    "  FOREIGN KEY (mentor_id) REFERENCES mentors(mentor_id) ON DELETE SET NULL" +
                    ")"
                );

                // ── 5. Tasks ─────────────────────────────────────────────────────
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS tasks (" +
                    "  task_id     INT AUTO_INCREMENT PRIMARY KEY," +
                    "  intern_id   INT NOT NULL," +
                    "  task_title  VARCHAR(200) NOT NULL," +
                    "  due_date    DATE," +
                    "  task_status VARCHAR(30) DEFAULT 'Pending'," +
                    "  FOREIGN KEY (intern_id) REFERENCES interns(intern_id) ON DELETE CASCADE" +
                    ")"
                );

                // ── 6. Attendance ────────────────────────────────────────────────
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS attendance (" +
                    "  attendance_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  intern_id     INT NOT NULL," +
                    "  att_date      DATE NOT NULL," +
                    "  is_present    BOOLEAN DEFAULT TRUE," +
                    "  UNIQUE KEY uq_intern_date (intern_id, att_date)," +
                    "  FOREIGN KEY (intern_id) REFERENCES interns(intern_id) ON DELETE CASCADE" +
                    ")"
                );

                // ── 7. Leaves ────────────────────────────────────────────────────
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS leaves (" +
                    "  leave_id  INT AUTO_INCREMENT PRIMARY KEY," +
                    "  intern_id INT NOT NULL," +
                    "  reason    VARCHAR(255) NOT NULL," +
                    "  from_date DATE NOT NULL," +
                    "  to_date   DATE NOT NULL," +
                    "  status    ENUM('Pending','Approved','Rejected') DEFAULT 'Pending'," +
                    "  FOREIGN KEY (intern_id) REFERENCES interns(intern_id) ON DELETE CASCADE" +
                    ")"
                );

                // ── 8. Companies ─────────────────────────────────────────────────
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS companies (" +
                    "  company_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  name       VARCHAR(100) NOT NULL," +
                    "  email      VARCHAR(100) UNIQUE NOT NULL," +
                    "  password   VARCHAR(100) NOT NULL," +
                    "  domain     VARCHAR(100)," +
                    "  location   VARCHAR(100)," +
                    "  contact    VARCHAR(50)," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );

                // ── 9. Students ──────────────────────────────────────────────────
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS students (" +
                    "  student_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  full_name  VARCHAR(100) NOT NULL," +
                    "  email      VARCHAR(100) UNIQUE NOT NULL," +
                    "  password   VARCHAR(100) NOT NULL," +
                    "  phone      VARCHAR(30)," +
                    "  college    VARCHAR(150)," +
                    "  domain     VARCHAR(100)," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );

                // ── 10. Internships ──────────────────────────────────────────────
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS internships (" +
                    "  internship_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  company_id    INT NOT NULL," +
                    "  title         VARCHAR(150) NOT NULL," +
                    "  domain        VARCHAR(100) NOT NULL," +
                    "  description   TEXT," +
                    "  stipend       VARCHAR(50)," +
                    "  duration      VARCHAR(50)," +
                    "  vacancies     INT DEFAULT 1," +
                    "  status        VARCHAR(20) DEFAULT 'Open'," +
                    "  posted_date   DATE," +
                    "  FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE" +
                    ")"
                );

                // ── 11. Applications ─────────────────────────────────────────────
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS applications (" +
                    "  application_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  student_id     INT NOT NULL," +
                    "  internship_id  INT NOT NULL," +
                    "  status         VARCHAR(30) DEFAULT 'Pending'," +
                    "  applied_date   DATE," +
                    "  remarks        VARCHAR(255)," +
                    "  FOREIGN KEY (student_id)    REFERENCES students(student_id)       ON DELETE CASCADE," +
                    "  FOREIGN KEY (internship_id) REFERENCES internships(internship_id) ON DELETE CASCADE" +
                    ")"
                );

                System.out.println("[DB] All tables ready. System initialised successfully!\n");
            }
        } catch (SQLException e) {
            System.out.println("[DB Warning] Init issue: " + e.getMessage());
            System.out.println("  → Make sure MySQL Workbench is running and credentials in DBConnection.java are correct.\n");
        }
    }
}
