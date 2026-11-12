package queue;

public class FoodRequest {

    private String requestId;
    private String recipientName;
    private String foodRequired;
    private int quantityRequired;

    public FoodRequest(String requestId, String recipientName,
                       String foodRequired, int quantityRequired) {

        this.requestId = requestId;
        this.recipientName = recipientName;
        this.foodRequired = foodRequired;
        this.quantityRequired = quantityRequired;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getFoodRequired() {
        return foodRequired;
    }

    public int getQuantityRequired() {
        return quantityRequired;
    }

    @Override
    public String toString() {
        return "Request ID: " + requestId +
                "\nRecipient: " + recipientName +
                "\nFood Required: " + foodRequired +
                "\nQuantity Required: " + quantityRequired;
    }
}