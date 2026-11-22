import java.util.Scanner;

public class Admin {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        menu(sc);
        sc.close();
    }

    // Admin Portal Menu Loop
    public static void menu(Scanner sc) {
        int choice = -1;
        while (choice != 0) {
            System.out.println("\n=================================");
            System.out.println("          ADMIN PORTAL           ");
            System.out.println("=================================");
            System.out.println("1.  Add Intern");
            System.out.println("2.  View All Interns");
            System.out.println("3.  Search Intern by Name");
            System.out.println("4.  Update Intern Status");
            System.out.println("5.  Delete Intern");
            System.out.println("6.  Assign Task");
            System.out.println("7.  View Tasks of an Intern");
            System.out.println("8.  Update Task Status");
            System.out.println("9.  Mark Attendance (Today)");
            System.out.println("10. Attendance Report");
            System.out.println("0.  Back to Main Menu");
            System.out.print("Enter choice (0-10): ");

            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1:
                    Intern.addIntern(sc);
                    promptBack(sc);
                    break;
                case 2:
                    Intern.viewInterns();
                    promptBack(sc);
                    break;
                case 3:
                    Intern.searchIntern(sc);
                    promptBack(sc);
                    break;
                case 4:
                    Intern.updateInternStatus(sc);
                    promptBack(sc);
                    break;
                case 5:
                    Intern.deleteIntern(sc);
                    promptBack(sc);
                    break;
                case 6:
                    Task.assignTask(sc);
                    promptBack(sc);
                    break;
                case 7:
                    Task.viewTasks(sc);
                    promptBack(sc);
                    break;
                case 8:
                    Task.updateTaskStatus(sc);
                    promptBack(sc);
                    break;
                case 9:
                    Attendance.markAttendance(sc);
                    promptBack(sc);
                    break;
                case 10:
                    Attendance.attendanceReport();
                    promptBack(sc);
                    break;
                case 0:
                    System.out.println("Returning to Main Menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please choose 0 to 10.");
                    break;
            }
        }
    }

    // Helper: allows the user to review the result and choose when to go back
    private static void promptBack(Scanner sc) {
        System.out.print("\nPress Enter to go back to Admin menu...");
        sc.nextLine();
    }
}
