import exception.InvalidUserException;
import java.util.Scanner;
import linkedlist.DonationLinkedList;
import linkedlist.FoodDonation;
import model.User;
import queue.FoodRequest;
import queue.FoodRequestQueue;
import service.UserService;
import service.UserServiceImpl;
import stack.FoodDonationStack;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // ==========================================
        // USER REGISTRATION AND AUTHENTICATION
        // ==========================================

        System.out.println("========================================");
        System.out.println("       FOOD SURPLUS OPTIMIZER");
        System.out.println("========================================");

        System.out.println("\n--- USER REGISTRATION ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter role (DONOR/RECIPIENT): ");
        String role = scanner.nextLine();

        User user = new User(username, password, role);

        UserService userService = new UserServiceImpl();

        try {

            if (userService.validateUser(user)) {
                System.out.println("\nUser registration details are valid.");

                System.out.println("\n--- LOGIN ---");

                System.out.print("Enter username: ");
                String loginUsername = scanner.nextLine();

                System.out.print("Enter password: ");
                String loginPassword = scanner.nextLine();

                boolean authenticated =
                        userService.authenticateUser(
                                user,
                                loginUsername,
                                loginPassword
                        );

                if (!authenticated) {
                    System.out.println("\nInvalid username or password.");
                    scanner.close();
                    return;
                }

                System.out.println("\nLogin successful.");

            }

        } catch (InvalidUserException e) {

            System.out.println("\nValidation Error: " + e.getMessage());
            scanner.close();
            return;

        }

        // ==========================================
        // DATA STRUCTURES
        // ==========================================

        DonationLinkedList donationList = new DonationLinkedList();

        FoodDonationStack donationStack =
                new FoodDonationStack(5);

        FoodRequestQueue requestQueue =
                new FoodRequestQueue(5);

        // ==========================================
        // MAIN MENU
        // ==========================================

        int choice;

        do {

            System.out.println("\n========================================");
            System.out.println("              MAIN MENU");
            System.out.println("========================================");

            System.out.println("1. Add Food Donation");
            System.out.println("2. View Food Donations");
            System.out.println("3. View Donation Stack");
            System.out.println("4. Add Food Request");
            System.out.println("5. View Food Requests");
            System.out.println("6. Process Next Food Request");
            System.out.println("0. Exit");

            System.out.println("========================================");

            System.out.print("Enter your choice: ");

            while (!scanner.hasNextInt()) {
                System.out.println("Please enter a valid number.");
                scanner.next();
                System.out.print("Enter your choice: ");
            }

            choice = scanner.nextInt();
            scanner.nextLine();

            // ==========================================
            // OPTION 1 - ADD DONATION
            // ==========================================

            if (choice == 1) {

                System.out.println("\n--- ADD FOOD DONATION ---");

                System.out.print("Enter food name: ");
                String foodName = scanner.nextLine();

                System.out.print("Enter quantity: ");

                while (!scanner.hasNextInt()) {
                    System.out.println("Please enter a valid quantity.");
                    scanner.next();
                    System.out.print("Enter quantity: ");
                }

                int quantity = scanner.nextInt();
                scanner.nextLine();

                System.out.print("Enter donor name: ");
                String donorName = scanner.nextLine();

                String donationId =
                        "D" + System.currentTimeMillis();

                FoodDonation donation =
                        new FoodDonation(
                                donationId,
                                foodName,
                                quantity,
                                donorName
                        );

                donationList.addDonation(donation);

                try {
                    donationStack.push(donation);
                    System.out.println("Donation ID: " + donationId);
                } catch (Exception e) {
                    System.out.println("\nDonation added to linked list.");
                    System.out.println("Stack Error: " + e.getMessage());
                }

            }

            // ==========================================
            // OPTION 2 - VIEW LINKED LIST
            // ==========================================

            else if (choice == 2) {

                System.out.println("\n--- FOOD DONATIONS ---");

                donationList.displayDonations();

            }

            // ==========================================
            // OPTION 3 - VIEW STACK
            // ==========================================

            else if (choice == 3) {

                System.out.println("\n--- DONATION STACK ---");

                donationStack.displayStack();

            }

            // ==========================================
            // OPTION 4 - ADD FOOD REQUEST
            // ==========================================

            else if (choice == 4) {

                System.out.println("\n--- ADD FOOD REQUEST ---");

                System.out.print("Enter recipient ID: ");
                String requestId = scanner.nextLine();

                System.out.print("Enter recipient name: ");
                String recipientName = scanner.nextLine();

                System.out.print("Enter food required: ");
                String foodRequired = scanner.nextLine();

                System.out.print("Enter quantity required: ");

                while (!scanner.hasNextInt()) {
                    System.out.println("Please enter a valid quantity.");
                    scanner.next();
                    System.out.print("Enter quantity required: ");
                }

                int quantityRequired = scanner.nextInt();
                scanner.nextLine();

                FoodRequest request =
                        new FoodRequest(
                                requestId,
                                recipientName,
                                foodRequired,
                                quantityRequired
                        );

                try {

                    requestQueue.enqueue(request);

                    System.out.println(
                            "\nFood request added successfully."
                    );

                } catch (Exception e) {

                    System.out.println(
                            "\nUnable to add request: "
                                    + e.getMessage()
                    );

                }

            }

            // ==========================================
            // OPTION 5 - VIEW REQUESTS
            // ==========================================

            else if (choice == 5) {

                System.out.println("\n--- PENDING FOOD REQUESTS ---");

                requestQueue.displayQueue();

            }

            // ==========================================
            // OPTION 6 - PROCESS NEXT REQUEST
            // ==========================================

            else if (choice == 6) {

                    System.out.println("\n--- PROCESS NEXT REQUEST ---");

                    // First check whether a request exists
                    FoodRequest nextRequest = requestQueue.peek();

                    if (nextRequest == null) {

                        System.out.println("No pending food requests.");

                    } else {

                        System.out.println("Processing request...");
                        System.out.println(nextRequest);

                        requestQueue.dequeue();

                        System.out.println("\nRequest processed successfully.");
                        System.out.println("Request removed from the queue.");

                    }
                }

            // ==========================================
            // OPTION 0 - EXIT
            // ==========================================

            else if (choice == 0) {

                System.out.println(
                        "\nThank you for using Food Surplus Optimizer."
                );

            }

            else {

                System.out.println(
                        "\nInvalid choice. Please try again."
                );

            }

        } while (choice != 0);

        scanner.close();
    }
}