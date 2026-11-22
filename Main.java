import java.util.Scanner;

// Internship Tracker - console app using JDBC + MySQL
// Features role selection (Admin & User/Intern) with Back options throughout
public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        int choice = -1;
        while (choice != 3) {
            System.out.println("\n==============================================");
            System.out.println("       INTERNSHIP TRACKER SYSTEM              ");
            System.out.println("==============================================");
            System.out.println("1. Admin Portal");
            System.out.println("2. User / Intern Portal");
            System.out.println("3. Exit");
            System.out.print("Select role (1-3): ");

            String input = sc.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            if (choice == 1) {
                Admin.menu(sc);
            } else if (choice == 2) {
                User.menu(sc);
            } else if (choice == 3) {
                System.out.println("Thank you for using Internship Tracker. Goodbye!");
            } else {
                System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
        sc.close();
    }
}
