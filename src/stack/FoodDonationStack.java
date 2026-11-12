package stack;

import linkedlist.FoodDonation;

public class FoodDonationStack {

    private FoodDonation[] stack;
    private int top;
    private int capacity;

    public FoodDonationStack(int capacity) {
        this.capacity = capacity;
        stack = new FoodDonation[capacity];
        top = -1;
    }

    public void push(FoodDonation donation) {
        if (isFull()) {
            System.out.println("Stack is full. Cannot add donation.");
            return;
        }

        stack[++top] = donation;
        System.out.println("Donation pushed to stack successfully.");
    }

    public FoodDonation pop() {
        if (isEmpty()) {
            System.out.println("Stack is empty.");
            return null;
        }

        return stack[top--];
    }

    public FoodDonation peek() {
        if (isEmpty()) {
            return null;
        }

        return stack[top];
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public boolean isFull() {
        return top == capacity - 1;
    }

    public void displayStack() {

        if (isEmpty()) {
            System.out.println("No donations in stack.");
            return;
        }

        System.out.println("\n=== Donation Stack ===");

        for (int i = top; i >= 0; i--) {
            System.out.println(stack[i]);
        }
    }
    public java.util.List<FoodDonation> getAllDonations() {

    java.util.List<FoodDonation> donations =
            new java.util.ArrayList<>();

    for (int i = top; i >= 0; i--) {

        donations.add(stack[i]);
    }

    return donations;
}
}