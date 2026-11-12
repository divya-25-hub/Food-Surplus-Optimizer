package linkedlist;

public class FoodDonation {

    private String donationId;
    private String foodName;
    private int quantity;
    private String donorName;

    // Constructor
    public FoodDonation(String donationId, String foodName,
                        int quantity, String donorName) {

        this.donationId = donationId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.donorName = donorName;
    }

    // Getters
    public String getDonationId() {
        return donationId;
    }

    public String getFoodName() {
        return foodName;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDonorName() {
        return donorName;
    }

    // Display donation details
    @Override
    public String toString() {
        return "Donation ID: " + donationId +
                "\nFood Name: " + foodName +
                "\nQuantity: " + quantity +
                "\nDonor: " + donorName;
    }
}