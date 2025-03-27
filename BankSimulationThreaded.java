import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.io.*;
import java.text.DecimalFormat;

public class BankSimulationThreaded {
    // Constants
    private static final double SIMULATION_HOURS = 8.0; // 9:00 AM to 5:00 PM
    private static final double ARRIVAL_RATE = 40.0 / 3600.0; // 40 customers per hour in seconds
    private static final double SERVICE_TIME = 4.0 * 60.0; // 4 minutes in seconds
    private static final int[] TELLERS = {2, 4, 3}; // Number of tellers per period
    private static final double[] PERIOD_HOURS = {2.0, 3.0, 3.0}; // Duration of each period in hours

    // Shared resources
    private final Queue<Double> customerQueue = new LinkedList<>(); // Queue of arrival times
    private final List<Double> serviceTimes = Collections.synchronizedList(new ArrayList<>()); // Service completion times
    private volatile double currentTime = 0.0;
    private volatile int numTellers = TELLERS[0];
    private volatile int periodIndex = 0;
    private volatile double periodStartTime = 0.0;
    private volatile boolean simulationRunning = true;

    // Metrics
    private double totalWaitTime = 0.0;
    private double totalSystemTime = 0.0;
    private int totalCustomers = 0;
    private double totalTellerBusyTime = 0.0;
    private int maxQueueLength = 0;
    private int customersServed = 0;

    // Synchronization
    private final ReentrantLock queueLock = new ReentrantLock();
    private final ReentrantLock metricsLock = new ReentrantLock();
    private final Random rand = new Random();
    private final ExecutorService executor = Executors.newFixedThreadPool(2); // Two threads: arrivals, services
    private static final DecimalFormat df = new DecimalFormat("0.00");

    // Thread for customer arrivals
    class ArrivalTask implements Runnable {
        @Override
        public void run() {
            while (simulationRunning && currentTime < SIMULATION_HOURS * 3600.0) {
                double nextArrival = generateArrival();
                if (nextArrival > SIMULATION_HOURS * 3600.0) break;

                queueLock.lock();
                try {
                    currentTime = nextArrival;
                    updatePeriod();
                    processArrival(nextArrival);
                } finally {
                    queueLock.unlock();
                }

                try {
                    Thread.sleep(1); // Small delay to simulate real-time processing
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // Thread for teller services
    class ServiceTask implements Runnable {
        @Override
        public void run() {
            while (simulationRunning && (!customerQueue.isEmpty() || currentTime < SIMULATION_HOURS * 3600.0)) {
                queueLock.lock();
                try {
                    double nextService = findNextServiceCompletion();
                    if (nextService != Double.MAX_VALUE && nextService <= SIMULATION_HOURS * 3600.0) {
                        currentTime = nextService;
                        processServiceCompletion(nextService);
                    }
                } finally {
                    queueLock.unlock();
                }

                try {
                    Thread.sleep(1); // Small delay to simulate real-time processing
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public void runSimulation() {
        // Start arrival and service threads
        executor.submit(new ArrivalTask());
        executor.submit(new ServiceTask());

        // Wait for simulation to complete
        try {
            Thread.sleep((long) (SIMULATION_HOURS * 3600 * 1.1)); // Slightly longer than simulation time
            simulationRunning = false;
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Simulation interrupted: " + e.getMessage());
        }

        calculateMetrics();
        printResults();
    }

    private double generateArrival() {
        return currentTime + (-Math.log(1.0 - rand.nextDouble()) / ARRIVAL_RATE);
    }

    private double generateServiceTime() {
        return -Math.log(1.0 - rand.nextDouble()) * SERVICE_TIME;
    }

    private void processArrival(double arrivalTime) {
        customerQueue.add(arrivalTime);
        if (customerQueue.size() > maxQueueLength) {
            maxQueueLength = customerQueue.size();
        }

        if (serviceTimes.size() < numTellers) {
            serviceTimes.add(arrivalTime + generateServiceTime());
        }

        metricsLock.lock();
        try {
            totalCustomers++;
        } finally {
            metricsLock.unlock();
        }
    }

    private void processServiceCompletion(double completionTime) {
        double waitTime = completionTime - customerQueue.poll();
        serviceTimes.remove(completionTime);

        metricsLock.lock();
        try {
            totalWaitTime += waitTime;
            totalSystemTime += waitTime + (SERVICE_TIME / 60.0); // Convert to minutes
            totalTellerBusyTime += SERVICE_TIME / 60.0;
            customersServed++;
        } finally {
            metricsLock.unlock();
        }

        if (!customerQueue.isEmpty() && serviceTimes.size() < numTellers) {
            serviceTimes.add(currentTime + generateServiceTime());
        }
    }

    private double findNextServiceCompletion() {
        synchronized (serviceTimes) {
            if (serviceTimes.isEmpty()) return Double.MAX_VALUE;
            return Collections.min(serviceTimes);
        }
    }

    private void updatePeriod() {
        double periodTime = periodStartTime + (PERIOD_HOURS[periodIndex] * 3600.0);
        if (currentTime >= periodTime && periodIndex < 2) {
            periodStartTime = periodTime;
            periodIndex++;
            numTellers = TELLERS[periodIndex];
            while (serviceTimes.size() > numTellers) {
                serviceTimes.remove(serviceTimes.size() - 1);
            }
        }
    }

    private void calculateMetrics() {
        metricsLock.lock();
        try {
            double avgWaitTime = totalWaitTime / customersServed;
            double avgSystemTime = totalSystemTime / customersServed;
            double utilization = (totalTellerBusyTime / (SIMULATION_HOURS * 60.0)) / TELLERS[periodIndex];
            double avgQueueLength = totalWaitTime * (ARRIVAL_RATE * 3600.0) / customersServed;
            double probAllBusy = (double) maxQueueLength / totalCustomers;

            System.out.println("\nFinal Metrics for the Day:");
            System.out.println("Average Waiting Time: " + df.format(avgWaitTime) + " minutes");
            System.out.println("Average Time in System: " + df.format(avgSystemTime) + " minutes");
            System.out.println("Teller Utilization Rate: " + df.format(utilization * 100) + "%");
            System.out.println("Average Number in Queue: " + df.format(avgQueueLength));
            System.out.println("Probability of All Tellers Busy: " + df.format(probAllBusy));
        } finally {
            metricsLock.unlock();
        }
    }

    private void printResults() {
        try (PrintWriter out = new PrintWriter("results_threaded.txt")) {
            metricsLock.lock();
            try {
                double avgWaitTime = totalWaitTime / customersServed;
                double avgSystemTime = totalSystemTime / customersServed;
                double utilization = (totalTellerBusyTime / (SIMULATION_HOURS * 60.0)) / TELLERS[periodIndex];
                double avgQueueLength = totalWaitTime * (ARRIVAL_RATE * 3600.0) / customersServed;
                double probAllBusy = (double) maxQueueLength / totalCustomers;

                out.println("Average Waiting Time: " + df.format(avgWaitTime) + " minutes");
                out.println("Average Time in System: " + df.format(avgSystemTime) + " minutes");
                out.println("Teller Utilization Rate: " + df.format(utilization * 100) + "%");
                out.println("Average Number in Queue: " + df.format(avgQueueLength));
                out.println("Probability of All Tellers Busy: " + df.format(probAllBusy));
            } finally {
                metricsLock.unlock();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        BankSimulationThreaded sim = new BankSimulationThreaded();
        sim.runSimulation();
    }
}