package queue;

public class FoodRequestQueue {

    private FoodRequest[] queue;
    private int front;
    private int rear;
    private int capacity;

    public FoodRequestQueue(int capacity) {
        this.capacity = capacity;
        queue = new FoodRequest[capacity];
        front = 0;
        rear = -1;
    }

    // Add a request to the queue
    public void enqueue(FoodRequest request) {

        if (isFull()) {
            System.out.println("Request queue is full.");
            return;
        }

        queue[++rear] = request;

        System.out.println("Food request added successfully.");
    }

    // Process the first request
    public FoodRequest dequeue() {

        if (isEmpty()) {
            System.out.println("No pending food requests.");
            return null;
        }

        FoodRequest request = queue[front];

        // Shift remaining elements forward
        for (int i = front; i < rear; i++) {
            queue[i] = queue[i + 1];
        }

        queue[rear] = null;
        rear--;

        return request;
    }

    // View the next request
    public FoodRequest peek() {

        if (isEmpty()) {
            return null;
        }

        return queue[front];
    }

    public boolean isEmpty() {
        return rear == -1;
    }

    public boolean isFull() {
        return rear == capacity - 1;
    }

    // Display all pending requests
    public void displayQueue() {

        if (isEmpty()) {
            System.out.println("No pending food requests.");
            return;
        }

        System.out.println("\n=== PENDING FOOD REQUESTS ===");

        for (int i = front; i <= rear; i++) {
            System.out.println(queue[i]);
            System.out.println("-----------------------------");
        }
    }
    public java.util.List<FoodRequest> getAllRequests() {

    java.util.List<FoodRequest> requests =
            new java.util.ArrayList<>();

    for (int i = front; i <= rear; i++) {

        requests.add(queue[i]);
    }

    return requests;
}
}