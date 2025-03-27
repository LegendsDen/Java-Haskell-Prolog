import java.util.concurrent.atomic.AtomicInteger;

class AtomicCounter {
    private AtomicInteger count = new AtomicInteger(0);  // Atomic variable

    public void increment() {
        count.incrementAndGet(); // Atomically increments by 1
    }

    public void decrement() {
        count.decrementAndGet(); // Atomically decrements by 1
    }

    public int getValue() {
        return count.get();  // Retrieve the current value
    }
}

public class AtomicExample {
    public static void main(String[] args) throws InterruptedException {
        AtomicCounter counter = new AtomicCounter();

        // Creating multiple threads to increment the counter
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) counter.increment();
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) counter.decrement();
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Final Counter Value: " + counter.getValue());
    }
}
