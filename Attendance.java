import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Attendance {
    private int attendanceId;
    private int internId;
    private Date attDate;
    private boolean isPresent;

    public Attendance() {}

    public Attendance(int attendanceId, int internId, Date attDate, boolean isPresent) {
        this.attendanceId = attendanceId;
        this.internId = internId;
        this.attDate = attDate;
        this.isPresent = isPresent;
    }

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getInternId() { return internId; }
    public void setInternId(int internId) { this.internId = internId; }

    public Date getAttDate() { return attDate; }
    public void setAttDate(Date attDate) { this.attDate = attDate; }

    public boolean isPresent() { return isPresent; }
    public void setPresent(boolean present) { isPresent = present; }

    // Direct attendance marking
    public static boolean markAttendanceDirect(int internId, boolean present) {
        String sql = "INSERT INTO attendance (intern_id, att_date, is_present) VALUES (?, CURDATE(), ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ps.setBoolean(2, present);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error (maybe attendance already marked for today): " + e.getMessage());
            return false;
        }
    }

    // View personal attendance summary for an intern
    public static void viewMyAttendance(int internId) {
        String sql = "SELECT SUM(is_present) AS days_present, COUNT(attendance_id) AS total_days "
                + "FROM attendance WHERE intern_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int present = rs.getInt("days_present");
                int total = rs.getInt("total_days");
                double percentage = total > 0 ? ((double) present / total) * 100.0 : 0.0;
                System.out.println("\n===== Attendance Summary =====");
                System.out.println("Days Present : " + present);
                System.out.println("Total Days   : " + total);
                System.out.printf("Attendance   : %.2f%%\n", percentage);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 9. Mark attendance as Admin (with option to go back)
    public static void markAttendance(Scanner sc) {
        System.out.print("Intern ID (or 0 to go back): ");
        String idStr = sc.nextLine().trim();
        if (idStr.equals("0") || idStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        int internId;
        try {
            internId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        System.out.print("Present today? (y/n, or 0 to go back): ");
        String answer = sc.nextLine().trim();
        if (answer.equals("0") || answer.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        boolean present = answer.equalsIgnoreCase("y");
        if (markAttendanceDirect(internId, present)) {
            System.out.println("Attendance marked successfully!");
        }
    }

    // 10. Attendance report for all interns
    public static void attendanceReport() {
        String sql = "SELECT i.full_name, "
                + "SUM(a.is_present) AS days_present, COUNT(a.attendance_id) AS total_days "
                + "FROM interns i LEFT JOIN attendance a ON i.intern_id = a.intern_id "
                + "GROUP BY i.intern_id, i.full_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\nName | Days Present | Total Days");
            System.out.println("----------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(rs.getString("full_name") + " | "
                        + rs.getInt("days_present") + " | "
                        + rs.getInt("total_days"));
            }
            if (!found) {
                System.out.println("No attendance records found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
