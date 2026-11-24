import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Mentor {
    private int mentorId;
    private String mentorName;

    public Mentor() {}

    public Mentor(int mentorId, String mentorName) {
        this.mentorId = mentorId;
        this.mentorName = mentorName;
    }

    public int getMentorId() {
        return mentorId;
    }

    public void setMentorId(int mentorId) {
        this.mentorId = mentorId;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    // Helper: show all mentors
    public static void viewMentors() {
        String sql = "SELECT mentor_id, mentor_name FROM mentors";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\nMentors:");
            while (rs.next()) {
                System.out.println(rs.getInt("mentor_id") + " - " + rs.getString("mentor_name"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
