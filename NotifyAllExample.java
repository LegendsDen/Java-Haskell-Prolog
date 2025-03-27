class SharedResource {
    private boolean available = false;  // Flag for resource availability

    public synchronized void produce() {
        available = true;
        System.out.println("Produced an item");
        notifyAll();  // Wake up all waiting threads
    }
  
    public synchronized void consume() throws InterruptedException {
        while (!available) {  // Wait until an item is available
            wait();  
        }
        System.out.println("Consumed an item");
        available = false;  // Reset availability
        notifyAll();  // Notify producer that item was consumed
    }
}

public class NotifyAllExample {
    public static void main(String[] args) {
        SharedResource resource = new SharedResource();

        // Consumer threads
        Thread consumer1 = new Thread(() -> {
            try {
                // for (int i = 0; i < 1; i++) {  // Consume multiple times
                    resource.consume();
                    // Thread.sleep(100);
                // }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer2 = new Thread(() -> {
            try {
                // for (int i = 0; i < 1; i++) {
                    resource.consume();
                    // Thread.sleep(100);
                // }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                // for (int i = 0; i < 2; i++) {  // Produce multiple items
                    resource.produce();
                    resource.produce();
                    Thread.sleep(100);
                // }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumer1.start();
        consumer2.start();
        try { Thread.sleep(100); } catch (InterruptedException e) {} // Ensure consumers start first
        producer.start();
    }
}
