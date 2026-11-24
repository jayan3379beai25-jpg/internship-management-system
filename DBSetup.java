import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBSetup {
    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            // 1. Add password column to interns (ignore if already exists)
            try {
                st.executeUpdate("ALTER TABLE interns ADD COLUMN password VARCHAR(100) NOT NULL DEFAULT 'intern123'");
                System.out.println("✓ Added 'password' column to interns table.");
            } catch (SQLException e) {
                System.out.println("⚠ 'password' column already exists (skipped).");
            }

            // 2. Create admin_accounts table
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS admin_accounts (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  username VARCHAR(100) NOT NULL UNIQUE," +
                "  password VARCHAR(100) NOT NULL" +
                ")"
            );
            System.out.println("✓ admin_accounts table ready.");

            // 3. Insert default admin if not exists
            int rows = st.executeUpdate(
                "INSERT IGNORE INTO admin_accounts (username, password) VALUES ('admin', 'admin123')"
            );
            if (rows > 0) System.out.println("✓ Default admin created  →  username: admin  |  password: admin123");
            else          System.out.println("⚠ Default admin already exists (skipped).");

            // 4. Create leaves table
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS leaves (" +
                "  leave_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  intern_id INT NOT NULL," +
                "  reason VARCHAR(255) NOT NULL," +
                "  from_date DATE NOT NULL," +
                "  to_date DATE NOT NULL," +
                "  status ENUM('Pending','Approved','Rejected') DEFAULT 'Pending'," +
                "  FOREIGN KEY (intern_id) REFERENCES interns(intern_id) ON DELETE CASCADE" +
                ")"
            );
            System.out.println("✓ leaves table ready.");

            System.out.println("\n✅ Database setup complete! You can now run the app.");

        } catch (SQLException e) {
            System.out.println("❌ DB Error: " + e.getMessage());
        }
    }
}
