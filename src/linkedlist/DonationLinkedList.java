package linkedlist;

public class DonationLinkedList {

    private DonationNode head;

    // Add a new donation
    public void addDonation(FoodDonation donation) {

        DonationNode newNode = new DonationNode(donation);

        // If list is empty
        if (head == null) {
            head = newNode;
        } else {

            DonationNode current = head;

            // Move to the last node
            while (current.next != null) {
                current = current.next;
            }

            // Add new node at the end
            current.next = newNode;
        }

        System.out.println("Donation added successfully.");
    }

    // Display all donations
    public void displayDonations() {

        if (head == null) {
            System.out.println("No food donations available.");
            return;
        }

        DonationNode current = head;

        System.out.println("\n=== Available Food Donations ===");

        while (current != null) {
            System.out.println(current.donation);
            System.out.println("-----------------------------");

            current = current.next;
        }
    }

    // Search donation by ID
    public FoodDonation searchDonation(String donationId) {

        DonationNode current = head;

        while (current != null) {

            if (current.donation.getDonationId().equalsIgnoreCase(donationId)) {
                return current.donation;
            }

            current = current.next;
        }

        return null;
    }

    // Remove donation by ID
    public boolean removeDonation(String donationId) {

        if (head == null) {
            return false;
        }

        // If first node needs to be removed
        if (head.donation.getDonationId().equalsIgnoreCase(donationId)) {
            head = head.next;
            return true;
        }

        DonationNode current = head;

        while (current.next != null) {

            if (current.next.donation.getDonationId()
                    .equalsIgnoreCase(donationId)) {

                current.next = current.next.next;
                return true;
            }

            current = current.next;
        }

        return false;
    }
    public java.util.List<FoodDonation> getAllDonations() {

    java.util.List<FoodDonation> donations =
            new java.util.ArrayList<>();

    DonationNode current = head;

    while (current != null) {

        donations.add(current.donation);

        current = current.next;
    }

    return donations;
}
}