package linkedlist;

public class DonationNode {

    FoodDonation donation;
    DonationNode next;

    // Constructor
    public DonationNode(FoodDonation donation) {
        this.donation = donation;
        this.next = null;
    }
}