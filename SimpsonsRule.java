import java.util.ArrayList;
import java.util.List;

class IntegrationThread extends Thread {
    private final int threadId;
    private final int intervalsPerThread;
    private final double a;
    private final double h;
    private final int n;
    private final double[] partialSums;

    public IntegrationThread(int threadId, int intervalsPerThread, double a, double h, int n, double[] partialSums) {
        this.threadId = threadId;
        this.intervalsPerThread = intervalsPerThread;
        this.a = a;
        this.h = h;
        this.n = n;
        this.partialSums = partialSums;
    }

    @Override
    public void run() {
        double localSum = 0;
        int start = threadId * intervalsPerThread + 1;
        int end = (threadId == partialSums.length - 1) ? n - 1 : start + intervalsPerThread - 1;

        for (int i = start; i <= end; i++) {
            double x = a + i * h;
            if (i % 2 == 0) {
                localSum += 2 * SimpsonsRule.f(x); // Even indices
            } else {
                localSum += 4 * SimpsonsRule.f(x); // Odd indices
            }
            // System.out.println("Thread " + threadId + " processing index " + i);
        }

        partialSums[threadId] = localSum;
    }
}

public class SimpsonsRule {

    // Define the function f(x) here
    public static double f(double x) {
        return (1 / Math.sqrt(2 * Math.PI)) * Math.exp(-x * x / 2);
    }

    public static double simpsonsRule(double a, double b, int n, int threads) {

        double h = (b - a) / n; // Step size
        double initialSum = f(a) + f(b); // f(a) and f(b) are the endpoints
        // System.out.println("Initial sum: " + initialSum);

        List<Thread> threadList = new ArrayList<>();
        double[] partialSums = new double[threads];

        int intervalsPerThread = n / threads;
        // System.out.println("Intervals per thread: " + intervalsPerThread);
        long startTime = System.currentTimeMillis();
        for (int t = 0; t < threads; t++) {
            IntegrationThread thread = new IntegrationThread(t, intervalsPerThread, a, h, n, partialSums);
            threadList.add(thread);
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Combine all partial sums
        double totalSum = initialSum;
        for (double partialSum : partialSums) {
            // System.out.println("Partial sum: " + partialSum);
            totalSum += partialSum;
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
        return (h / 3) * totalSum;
    }

    public static void main(String[] args){
        int threads = Integer.parseInt(args[0]); // Number of threads

        if (threads < 1 || threads > 16) {
            System.out.println("Number of threads must be between 4 and 16.");
            return;
        }

       
        double result = simpsonsRule(-1, 1, 100000001, threads);

        System.out.println("The integral is approximately: " + result);
    }
}
