import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Task {
    private int taskId;
    private int internId;
    private String taskTitle;
    private Date dueDate;
    private String taskStatus;

    public Task() {}

    public Task(int taskId, int internId, String taskTitle, Date dueDate, String taskStatus) {
        this.taskId = taskId;
        this.internId = internId;
        this.taskTitle = taskTitle;
        this.dueDate = dueDate;
        this.taskStatus = taskStatus;
    }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public int getInternId() { return internId; }
    public void setInternId(int internId) { this.internId = internId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }

    // View all tasks of a specific intern ID
    public static void viewTasksByInternId(int internId) {
        String sql = "SELECT task_id, task_title, due_date, task_status FROM tasks WHERE intern_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\nTask ID | Title | Due Date | Status");
            System.out.println("--------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(rs.getInt("task_id") + " | "
                        + rs.getString("task_title") + " | "
                        + rs.getDate("due_date") + " | "
                        + rs.getString("task_status"));
            }
            if (!found) {
                System.out.println("No tasks found for Intern ID " + internId + ".");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Direct task update with internId verification
    public static boolean updateTaskStatusDirect(int internId, int taskId, String status) {
        String sql = "UPDATE tasks SET task_status = ? WHERE task_id = ? AND intern_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, taskId);
            ps.setInt(3, internId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    // 6. Give a task to an intern (with option to go back)
    public static void assignTask(Scanner sc) {
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

        System.out.print("Task title (or 0 to go back): ");
        String title = sc.nextLine().trim();
        if (title.equals("0") || title.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        System.out.print("Due date (YYYY-MM-DD) (or 0 to go back): ");
        String dueDate = sc.nextLine().trim();
        if (dueDate.equals("0") || dueDate.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        String sql = "INSERT INTO tasks (intern_id, task_title, due_date) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, internId);
            ps.setString(2, title);
            ps.setDate(3, Date.valueOf(dueDate));
            ps.executeUpdate();
            System.out.println("Task assigned successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 7. View all tasks of one intern (with option to go back)
    public static void viewTasks(Scanner sc) {
        System.out.print("Intern ID (or 0 to go back): ");
        String idStr = sc.nextLine().trim();
        if (idStr.equals("0") || idStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        try {
            int internId = Integer.parseInt(idStr);
            viewTasksByInternId(internId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    // 8. Update task status as Admin (with option to go back)
    public static void updateTaskStatus(Scanner sc) {
        System.out.print("Task ID (or 0 to go back): ");
        String idStr = sc.nextLine().trim();
        if (idStr.equals("0") || idStr.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        int taskId;
        try {
            taskId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Task ID.");
            return;
        }

        System.out.print("New status (Pending/In Progress/Done) (or 0 to go back): ");
        String status = sc.nextLine().trim();
        if (status.equals("0") || status.equalsIgnoreCase("back")) {
            System.out.println("Returning to menu...");
            return;
        }

        String sql = "UPDATE tasks SET task_status = ? WHERE task_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, taskId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Task updated successfully!");
            } else {
                System.out.println("No task found with ID: " + taskId);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
