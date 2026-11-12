package service;

import java.util.List;
import linkedlist.DonationLinkedList;
import linkedlist.FoodDonation;
import queue.FoodRequest;
import queue.FoodRequestQueue;
import stack.FoodDonationStack;

public class FoodShareService {

    private static final DonationLinkedList donationList =
            new DonationLinkedList();

    private static final FoodDonationStack donationStack =
            new FoodDonationStack(5);

    private static final FoodRequestQueue requestQueue =
            new FoodRequestQueue(5);
        
    private static int requestCounter = 1000;


    // ==========================================
    // ADD FOOD DONATION
    // ==========================================

    public static String addDonation(
            String foodName,
            int quantity,
            String donorName) {

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
        donationStack.push(donation);

        return donationId;
    }


    // Add a donation that has already been saved in MySQL.
    // The in-memory ID mirrors the database ID (e.g. D12 = donation_id 12).
    public static String addSavedDonation(
            int dbId, String foodName, int quantity, String donorName) {

        String donationId = "D" + dbId;
        FoodDonation donation =
                new FoodDonation(donationId, foodName, quantity, donorName);

        donationList.addDonation(donation);
        donationStack.push(donation);

        return donationId;
    }

    public static boolean isDonationStackFull() {
        return donationStack.isFull();
    }

    public static boolean isRequestQueueFull() {
        return requestQueue.isFull();
    }

    // Generate the next request ID (R1001, R1002, ...)
    public static synchronized String nextRequestId() {
        return "R" + (++requestCounter);
    }

    // Used at start-up so new IDs never collide with saved ones
    public static synchronized void setRequestCounter(int value) {
        if (value > requestCounter) {
            requestCounter = value;
        }
    }

    // Add a request that has already been saved in MySQL
    public static boolean enqueueSavedRequest(FoodRequest request) {
        if (requestQueue.isFull()) {
            return false;
        }
        requestQueue.enqueue(request);
        return true;
    }

    // ==========================================
    // ADD FOOD REQUEST
    // REQUEST ID GENERATED AUTOMATICALLY
    // ==========================================

    public static String addRequest(
            String recipientName,
            String foodRequired,
            int quantityRequired) {

        if (requestQueue.isFull()) {
            return null;
        }

        System.out.println("Queue full: " + requestQueue.isFull());

        String requestId = "R" + (++requestCounter);
        System.out.println("Created Request ID: " + requestId);
        
        FoodRequest request =
                new FoodRequest(
                        requestId,
                        recipientName,
                        foodRequired,
                        quantityRequired
                );

        requestQueue.enqueue(request);

        return requestId;
    }


    // ==========================================
    // VIEW DONATIONS
    // ==========================================

    public static List<FoodDonation> getDonations() {
        return donationList.getAllDonations();
    }


    // ==========================================
    // VIEW STACK
    // ==========================================

    public static List<FoodDonation> getStack() {
        return donationStack.getAllDonations();
    }


    // ==========================================
    // VIEW REQUESTS
    // ==========================================

    public static List<FoodRequest> getRequests() {
        return requestQueue.getAllRequests();
    }


    // ==========================================
    // PROCESS NEXT REQUEST
    // ==========================================

    public static FoodRequest processNextRequest() {
        return requestQueue.dequeue();
    }

    // Preview the next request without removing it from the FIFO queue.
    public static FoodRequest getNextRequest() {
        return requestQueue.peek();
    }

    
    // Get the newest donation without removing it
    public static FoodDonation getNextDonation() {
        return donationStack.peek();
    }

    // Remove a donation from both existing data structures
    public static boolean completeDonation(String donationId) {
        FoodDonation top = donationStack.peek();

        if (top == null ||
                !top.getDonationId().equals(donationId)) {
            return false;
        }

        donationStack.pop();
        donationList.removeDonation(donationId);
        return true;
    }

}