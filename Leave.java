import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Leave {

    // User: Apply for leave
    public static void applyLeave(Scanner sc, int internId) {
        System.out.print("Leave reason (or 0 to go back): ");
        String reason = sc.nextLine().trim();
        if (reason.equals("0") || reason.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        System.out.print("From date (YYYY-MM-DD) (or 0 to go back): ");
        String fromStr = sc.nextLine().trim();
        if (fromStr.equals("0") || fromStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        System.out.print("To date (YYYY-MM-DD) (or 0 to go back): ");
        String toStr = sc.nextLine().trim();
        if (toStr.equals("0") || toStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        String sql = "INSERT INTO leaves (intern_id, reason, from_date, to_date, status) VALUES (?, ?, ?, ?, 'Pending')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ps.setString(2, reason);
            ps.setDate(3, Date.valueOf(fromStr));
            ps.setDate(4, Date.valueOf(toStr));
            ps.executeUpdate();
            System.out.println("Leave application submitted successfully! Status: Pending");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // User: View own leave history
    public static void viewMyLeaves(int internId) {
        String sql = "SELECT leave_id, reason, from_date, to_date, status FROM leaves WHERE intern_id = ? ORDER BY from_date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\nLeave ID | Reason | From | To | Status");
            System.out.println("-----------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(rs.getInt("leave_id") + " | "
                        + rs.getString("reason") + " | "
                        + rs.getDate("from_date") + " | "
                        + rs.getDate("to_date") + " | "
                        + rs.getString("status"));
            }
            if (!found) System.out.println("No leave applications found.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Admin: View all pending leave requests
    public static void viewPendingLeaves() {
        String sql = "SELECT l.leave_id, i.full_name, l.reason, l.from_date, l.to_date, l.status "
                + "FROM leaves l JOIN interns i ON l.intern_id = i.intern_id "
                + "WHERE l.status = 'Pending' ORDER BY l.from_date";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\nLeave ID | Intern Name | Reason | From | To | Status");
            System.out.println("----------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(rs.getInt("leave_id") + " | "
                        + rs.getString("full_name") + " | "
                        + rs.getString("reason") + " | "
                        + rs.getDate("from_date") + " | "
                        + rs.getDate("to_date") + " | "
                        + rs.getString("status"));
            }
            if (!found) System.out.println("No pending leave requests.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Admin: Approve or Reject a leave
    public static void approveRejectLeave(Scanner sc) {
        viewPendingLeaves();
        System.out.print("Enter Leave ID to act on (or 0 to go back): ");
        String idStr = sc.nextLine().trim();
        if (idStr.equals("0") || idStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        int leaveId;
        try {
            leaveId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        System.out.print("Action (approve/reject, or 0 to go back): ");
        String action = sc.nextLine().trim().toLowerCase();
        if (action.equals("0") || action.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        String newStatus;
        if (action.equals("approve")) {
            newStatus = "Approved";
        } else if (action.equals("reject")) {
            newStatus = "Rejected";
        } else {
            System.out.println("Invalid action. Type 'approve' or 'reject'.");
            return;
        }

        String sql = "UPDATE leaves SET status = ? WHERE leave_id = ? AND status = 'Pending'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, leaveId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Leave " + newStatus + " successfully!");
            } else {
                System.out.println("No pending leave found with ID: " + leaveId);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
