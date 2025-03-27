import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Task implements Runnable {
    private int taskId;

    public Task(int id) {
        this.taskId = id;
    }

    @Override
    public void run() {
        System.out.println("Executing Task " + taskId + " by " + Thread.currentThread().getName());
        try {
            Thread.sleep(2000); // Simulating work
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Completed Task " + taskId + " by " + Thread.currentThread().getName());
    }
}

public class ThreadPools {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3); // Create a pool of 3 threads

        for (int i = 1; i <= 6; i++) {
            executor.execute(new Task(i)); // Submit tasks to the thread pool
        }

        executor.shutdown(); // Shut down after executing all tasks
    }
}
